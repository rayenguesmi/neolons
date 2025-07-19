package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Document(collection = "licenses")
public class LicenseEntity {

    @Id
    private String id;

    @Field("license_name")
    private String licenseName;

    @Field("license_key")
    private String licenseKey;

    @Field("license_type")
    private String licenseType; // STANDARD, PREMIUM, ENTERPRISE, TRIAL

    @Field("product_name")
    private String productName;

    @Field("product_version")
    private String productVersion;

    @Field("customer_name")
    private String customerName;

    @Field("customer_email")
    private String customerEmail;

    @Field("organization")
    private String organization;

    @Field("max_users")
    private Integer maxUsers;

    @Field("max_projects")
    private Integer maxProjects;

    @Field("features")
    private List<String> features; // Liste des fonctionnalités activées

    @Field("issue_date")
    private Date issueDate;

    @Field("expiry_date")
    private Date expiryDate;

    @Field("activation_date")
    private Date activationDate;

    @Field("is_active")
    private Boolean isActive;

    @Field("is_trial")
    private Boolean isTrial;

    @Field("usage_count")
    private Integer usageCount; // Nombre d'utilisations

    @Field("max_usage")
    private Integer maxUsage; // Limite d'utilisation

    @Field("hardware_fingerprint")
    private String hardwareFingerprint;

    @Field("ip_restrictions")
    private List<String> ipRestrictions;

    @Field("domain_restrictions")
    private List<String> domainRestrictions;

    @Field("notes")
    private String notes;

    @Field("created_by")
    private String createdBy;

    @Field("created_at")
    private Date createdAt;

    @Field("updated_by")
    private String updatedBy;

    @Field("updated_at")
    private Date updatedAt;

    @Field("last_validation")
    private Date lastValidation;

    @Field("validation_status")
    private String validationStatus; // VALID, EXPIRED, INVALID, SUSPENDED

    // Constructeurs
    public LicenseEntity() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
        this.isTrial = false;
        this.usageCount = 0;
        this.validationStatus = "VALID";
    }

    public LicenseEntity(String licenseName, String licenseType, String productName, 
                        String customerName, String customerEmail, Date expiryDate) {
        this();
        this.licenseName = licenseName;
        this.licenseType = licenseType;
        this.productName = productName;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.expiryDate = expiryDate;
        this.issueDate = new Date();
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Integer getMaxProjects() {
        return maxProjects;
    }

    public void setMaxProjects(Integer maxProjects) {
        this.maxProjects = maxProjects;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Date getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsTrial() {
        return isTrial;
    }

    public void setIsTrial(Boolean isTrial) {
        this.isTrial = isTrial;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public Integer getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(Integer maxUsage) {
        this.maxUsage = maxUsage;
    }

    public String getHardwareFingerprint() {
        return hardwareFingerprint;
    }

    public void setHardwareFingerprint(String hardwareFingerprint) {
        this.hardwareFingerprint = hardwareFingerprint;
    }

    public List<String> getIpRestrictions() {
        return ipRestrictions;
    }

    public void setIpRestrictions(List<String> ipRestrictions) {
        this.ipRestrictions = ipRestrictions;
    }

    public List<String> getDomainRestrictions() {
        return domainRestrictions;
    }

    public void setDomainRestrictions(List<String> domainRestrictions) {
        this.domainRestrictions = domainRestrictions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getLastValidation() {
        return lastValidation;
    }

    public void setLastValidation(Date lastValidation) {
        this.lastValidation = lastValidation;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    // Méthodes utilitaires
    public boolean isExpired() {
        return expiryDate != null && expiryDate.before(new Date());
    }

    public boolean isValid() {
        return isActive && !isExpired() && "VALID".equals(validationStatus);
    }

    public long getDaysUntilExpiry() {
        if (expiryDate == null) return -1;
        long diff = expiryDate.getTime() - new Date().getTime();
        return diff / (24 * 60 * 60 * 1000);
    }

    public void incrementUsage() {
        if (usageCount == null) usageCount = 0;
        usageCount++;
        this.updatedAt = new Date();
    }

    public boolean hasUsageLeft() {
        if (maxUsage == null) return true;
        return usageCount == null || usageCount < maxUsage;
    }

    @Override
    public String toString() {
        return "LicenseEntity{" +
                "id='" + id + '\'' +
                ", licenseName='" + licenseName + '\'' +
                ", licenseType='" + licenseType + '\'' +
                ", productName='" + productName + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", expiryDate=" + expiryDate +
                ", isActive=" + isActive +
                ", validationStatus='" + validationStatus + '\'' +
                '}';
    }
}

