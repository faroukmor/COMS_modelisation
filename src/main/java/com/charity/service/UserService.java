package com.charity.service;

import com.charity.dao.BeneficiaryDAO;
import com.charity.dao.DonorDAO;
import com.charity.dao.UserDAO;
import com.charity.model.Beneficiary;
import com.charity.model.CharityAssociation;
import com.charity.model.Donor;
import com.charity.model.Individual;
import com.charity.model.Role;
import com.charity.model.User;
import com.charity.util.PasswordUtil;
import com.charity.util.Validation;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Creates user accounts (public sign-up and administrator "add user") and links non-admin
 * accounts to their donor / beneficiary profile.
 * <p>
 * A donor, beneficiary or association account always gets a matching profile row created at the
 * same time. The account and its profile are tied together by e-mail address, which is why an
 * e-mail that already belongs to an existing profile cannot be re-used for a new account (otherwise
 * anybody could sign up with somebody else's e-mail and see that person's donations or requests).
 */
public class UserService {

    /** Roles a visitor may choose on the public sign-up form. ADMIN can only be granted by an admin. */
    public static final Set<Role> SELF_REGISTRATION_ROLES =
            EnumSet.of(Role.DONOR, Role.BENEFICIARY, Role.ASSOCIATION);

    /** Everything needed to create an account. Role-specific fields are ignored for other roles. */
    public record NewUser(String username, String password, String fullName, String email, String phone,
                          Role role, String nationalId, String registrationNumber,
                          String representativeName, String address) {
    }

    private final UserDAO userDAO;
    private final DonorDAO donorDAO;
    private final BeneficiaryDAO beneficiaryDAO;

    public UserService(UserDAO userDAO, DonorDAO donorDAO, BeneficiaryDAO beneficiaryDAO) {
        this.userDAO = userDAO;
        this.donorDAO = donorDAO;
        this.beneficiaryDAO = beneficiaryDAO;
    }

    /**
     * Public sign-up. Cannot create administrators.
     *
     * @return the created user
     * @throws IllegalArgumentException with a user-readable message when validation fails
     */
    public User register(NewUser data) {
        if (data.role() == null || !SELF_REGISTRATION_ROLES.contains(data.role())) {
            throw new IllegalArgumentException("Please choose Donor, Beneficiary or Association");
        }
        return create(data);
    }

    /**
     * Account creation by an administrator; any role is allowed.
     *
     * @param actor the user performing the action (must be an admin)
     */
    public User createByAdmin(User actor, NewUser data) {
        if (actor == null || actor.getRole() != Role.ADMIN) {
            throw new SecurityException("Administrator access required");
        }
        return create(data);
    }

    public List<User> findAll() {
        return userDAO.findAll();
    }

    /** The donor profile belonging to this account (matched by e-mail), if any. */
    public Optional<Donor> findDonorProfile(User user) {
        if (user == null || Validation.isBlank(user.getEmail())) {
            return Optional.empty();
        }
        return donorDAO.search(user.getEmail()).stream()
                .filter(d -> user.getEmail().equalsIgnoreCase(d.getEmail()))
                .findFirst();
    }

    /** The beneficiary profile belonging to this account (matched by e-mail), if any. */
    public Optional<Beneficiary> findBeneficiaryProfile(User user) {
        if (user == null || Validation.isBlank(user.getEmail())) {
            return Optional.empty();
        }
        return beneficiaryDAO.search(user.getEmail()).stream()
                .filter(b -> user.getEmail().equalsIgnoreCase(b.getEmail()))
                .findFirst();
    }

    // ------------------------------------------------------------------ internals

    private User create(NewUser d) {
        String username = Validation.clean(d.username());
        if (!Validation.isValidUsername(username)) {
            throw new IllegalArgumentException(
                    "Username must be 3-30 characters: letters, digits, dot, dash or underscore");
        }
        username = username.toLowerCase();
        String passwordProblem = Validation.passwordProblem(d.password());
        if (passwordProblem != null) {
            throw new IllegalArgumentException(passwordProblem);
        }
        String fullName = Validation.clean(d.fullName());
        if (fullName == null || fullName.length() < 2) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (d.role() == null) {
            throw new IllegalArgumentException("Role is required");
        }
        boolean needsProfile = d.role() != Role.ADMIN;
        String email = Validation.clean(d.email());
        if (email == null && needsProfile) {
            throw new IllegalArgumentException("Email is required");
        }
        if (email != null && !Validation.isValidEmail(email)) {
            throw new IllegalArgumentException("Email address is not valid");
        }
        String phone = Validation.clean(d.phone());
        if (phone != null && !Validation.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone number is not valid");
        }

        if (userDAO.exists(username)) {
            throw new IllegalArgumentException("This username is already taken");
        }
        if (email != null && userDAO.emailExists(email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        if (needsProfile && profileEmailTaken(d.role(), email)) {
            throw new IllegalArgumentException(
                    "This email is already registered in the system. Please contact an administrator.");
        }

        // Profile first: if it fails (e.g. duplicate national ID) nothing else has been written.
        if (needsProfile) {
            createProfile(d.role(), fullName, email, phone, d);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hash(d.password()));
        user.setRole(d.role());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setId(userDAO.save(user));
        return user;
    }

    private boolean profileEmailTaken(Role role, String email) {
        if (role == Role.DONOR) {
            return donorDAO.search(email).stream().anyMatch(x -> email.equalsIgnoreCase(x.getEmail()));
        }
        return beneficiaryDAO.search(email).stream().anyMatch(x -> email.equalsIgnoreCase(x.getEmail()));
    }

    private void createProfile(Role role, String fullName, String email, String phone, NewUser d) {
        String address = Validation.clean(d.address());
        switch (role) {
            case DONOR -> {
                Donor donor = new Donor(null, fullName, email, phone, address);
                donorDAO.save(donor);
            }
            case BENEFICIARY -> {
                String nationalId = Validation.clean(d.nationalId());
                if (nationalId == null) {
                    throw new IllegalArgumentException("National ID is required for beneficiaries");
                }
                beneficiaryDAO.save(new Individual(null, fullName, nationalId, email, phone, address));
            }
            case ASSOCIATION -> {
                String reg = Validation.clean(d.registrationNumber());
                if (reg == null) {
                    throw new IllegalArgumentException("Registration number is required for associations");
                }
                String rep = Validation.clean(d.representativeName());
                beneficiaryDAO.save(new CharityAssociation(null, fullName, reg,
                        rep == null ? fullName : rep, email, phone, address));
            }
            default -> throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }
}
