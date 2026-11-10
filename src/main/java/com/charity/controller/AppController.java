package com.charity.controller;

import com.charity.dao.AidRequestDAO;
import com.charity.dao.BeneficiaryDAO;
import com.charity.dao.DistributionDAO;
import com.charity.dao.DonationDAO;
import com.charity.dao.DonorDAO;
import com.charity.dao.OracleAidRequestDAO;
import com.charity.dao.OracleBeneficiaryDAO;
import com.charity.dao.OracleDistributionDAO;
import com.charity.dao.OracleDonationDAO;
import com.charity.dao.OracleDonorDAO;
import com.charity.dao.OracleUserDAO;
import com.charity.dao.UserDAO;
import com.charity.model.Role;
import com.charity.model.User;
import com.charity.service.AuthenticationService;
import com.charity.service.UserService;
import com.charity.service.BeneficiaryService;
import com.charity.service.DonationService;
import com.charity.service.DonorService;
import com.charity.service.FinancialService;
import com.charity.service.DistributionService;
import com.charity.view.ReportView;
import com.charity.service.AidRequestService;
import java.util.function.Supplier;
import javax.swing.JComponent;

import com.charity.view.DashboardView;
import com.charity.view.DistributionView;
import com.charity.view.DonorView;
import com.charity.view.LoginView;
import com.charity.view.MainFrame;
import com.charity.view.AidRequestView;
import com.charity.view.BeneficiaryView;
import com.charity.view.PortalView;
import com.charity.view.UsersView;
import com.charity.view.DonationView;

/**
 * Central application controller. Holds services/DAOs, manages the current user,
 * and coordinates navigation between views.
 */
public class AppController {
    public static final AppController INSTANCE = new AppController();

    // DAOs
    private final UserDAO userDAO = new OracleUserDAO();
    private final DonorDAO donorDAO = new OracleDonorDAO();
    private final BeneficiaryDAO beneficiaryDAO = new OracleBeneficiaryDAO();
    private final DonationDAO donationDAO = new OracleDonationDAO();
    private final AidRequestDAO requestDAO = new OracleAidRequestDAO();
    private final DistributionDAO distributionDAO = new OracleDistributionDAO();

    // Services
    public final AuthenticationService authService = new AuthenticationService(userDAO);
    public final UserService userService = new UserService(userDAO, donorDAO, beneficiaryDAO);
    public final DonorService donorService = new DonorService(donorDAO);
    public final BeneficiaryService beneficiaryService = new BeneficiaryService(beneficiaryDAO);
    public final DonationService donationService = new DonationService(donationDAO, donorDAO);
    public final AidRequestService requestService = new AidRequestService(requestDAO, beneficiaryDAO);
    public final DistributionService distributionService = new DistributionService(requestDAO, distributionDAO, donationDAO);
    public final FinancialService financialService = new FinancialService(donationDAO, distributionDAO);

    private MainFrame mainFrame;
    private User currentUser;

    public AppController() {
    }

    public void init(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public User authenticate(String username, String password) {
        return authService.authenticate(username, password);
    }

    /** Synchronous login (blocks on hashing and the database); the login screen uses a background thread. */
    public boolean login(String username, String password) {
        User user = authenticate(username, password);
        if (user == null) {
            return false;
        }
        onLoggedIn(user);
        return true;
    }

    /** Called once credentials were verified: opens the right home page for the user's role. */
    public void onLoggedIn(User user) {
        currentUser = user;
        if (mainFrame != null) {
            mainFrame.setUser(user);
        }
        if (authService.isAdmin(user)) {
            showDashboard();
        } else {
            showPortal();
        }
    }

    public void logout() {
        currentUser = null;
        if (mainFrame != null) {
            mainFrame.setUser(null);
        }
        showLogin();
    }

    public void showLogin() {
        if (mainFrame != null) {
            mainFrame.showView("login", new LoginView(this));
        }
    }

    // ---- navigation (administrator pages are refused for everybody else) ----

    private void adminPage(String id, Supplier<JComponent> factory) {
        if (mainFrame == null || currentUser == null || !authService.isAdmin(currentUser)) {
            return;
        }
        mainFrame.showView(id, factory.get());
    }

    public void showDashboard() {
        adminPage("dashboard", () -> new DashboardView(this));
    }

    public void showDonors() {
        adminPage("donors", () -> new DonorView(this));
    }

    public void showDonations() {
        adminPage("donations", () -> new DonationView(this));
    }

    public void showBeneficiaries() {
        adminPage("beneficiaries", () -> new BeneficiaryView(this));
    }

    public void showAidRequests() {
        adminPage("aidrequests", () -> new AidRequestView(this));
    }

    public void showDistributions() {
        adminPage("distributions", () -> new DistributionView(this));
    }

    public void showReports() {
        adminPage("reports", () -> new ReportView(this));
    }

    public void showUsers() {
        adminPage("users", () -> new UsersView(this));
    }

    /** Home page for donor / beneficiary / association accounts. */
    public void showPortal() {
        if (mainFrame == null || currentUser == null || currentUser.getRole() == Role.ADMIN) {
            return;
        }
        mainFrame.showView("portal", new PortalView(this));
    }

    // DAO access for views
    public DonationDAO getDonationDAO() { return donationDAO; }
    public DistributionDAO getDistributionDAO() { return distributionDAO; }
    public AidRequestDAO getRequestDAO() { return requestDAO; }
    public DonorDAO getDonorDAO() { return donorDAO; }
    public BeneficiaryDAO getBeneficiaryDAO() { return beneficiaryDAO; }
}
