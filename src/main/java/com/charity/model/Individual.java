package com.charity.model;

/**
 * Individual beneficiary: a person who needs financial assistance.
 */
public class Individual extends Beneficiary {
    private String fullName;
    private String nationalId;

    public Individual() {
        super();
        setType(BeneficiaryType.INDIVIDUAL);
    }

    public Individual(Long id, String fullName, String nationalId, String email, String phone, String address) {
        super(id, BeneficiaryType.INDIVIDUAL, email, phone, address);
        this.fullName = fullName;
        this.nationalId = nationalId;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    @Override
    public String getDisplayName() {
        return fullName != null ? fullName : "Individual";
    }

    @Override
    public String toString() {
        return "Individual{id=" + getId() + ", fullName='" + fullName + "', nationalId='" + nationalId + "'}";
    }
}
