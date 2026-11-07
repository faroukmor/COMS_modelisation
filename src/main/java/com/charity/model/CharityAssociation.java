package com.charity.model;

/**
 * Charity association beneficiary: an external charitable association requesting support.
 */
public class CharityAssociation extends Beneficiary {
    private String organizationName;
    private String registrationNumber;
    private String representativeName;

    public CharityAssociation() {
        super();
        setType(BeneficiaryType.ASSOCIATION);
    }

    public CharityAssociation(Long id, String organizationName, String registrationNumber,
                              String representativeName, String email, String phone, String address) {
        super(id, BeneficiaryType.ASSOCIATION, email, phone, address);
        this.organizationName = organizationName;
        this.registrationNumber = registrationNumber;
        this.representativeName = representativeName;
    }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getRepresentativeName() { return representativeName; }
    public void setRepresentativeName(String representativeName) { this.representativeName = representativeName; }

    @Override
    public String getDisplayName() {
        return organizationName != null ? organizationName : "Association";
    }

    @Override
    public String toString() {
        return "CharityAssociation{id=" + getId() + ", name='" + organizationName + "'}";
    }
}
