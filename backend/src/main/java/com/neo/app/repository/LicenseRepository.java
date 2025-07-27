package com.neo.app.repository;

import com.neo.app.documents.LicenseEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends MongoRepository<LicenseEntity, String> {

    // Recherche par clé de licence
    Optional<LicenseEntity> findByLicenseKey(String licenseKey);

    // Recherche par nom de licence
    List<LicenseEntity> findByLicenseName(String licenseName);

    // Recherche par type de licence
    List<LicenseEntity> findByLicenseType(String licenseType);

    // Recherche par nom de produit
    List<LicenseEntity> findByProductName(String productName);

    // Recherche par client
    List<LicenseEntity> findByCustomerName(String customerName);

    // Recherche par email client
    List<LicenseEntity> findByCustomerEmail(String customerEmail);

    // Recherche par organisation
    List<LicenseEntity> findByOrganization(String organization);

    // Recherche des licences actives
    List<LicenseEntity> findByIsActive(Boolean isActive);

    // Recherche des licences d'essai
    List<LicenseEntity> findByIsTrial(Boolean isTrial);

    // Recherche par statut de validation
    List<LicenseEntity> findByValidationStatus(String validationStatus);

    // Recherche des licences expirées
    @Query("{ 'expiryDate' : { $lt: ?0 } }")
    List<LicenseEntity> findExpiredLicenses(Date currentDate);

    // Recherche des licences qui expirent bientôt
    @Query("{ 'expiryDate' : { $gte: ?0, $lte: ?1 } }")
    List<LicenseEntity> findLicensesExpiringBetween(Date startDate, Date endDate);

    // Recherche des licences créées par un utilisateur
    List<LicenseEntity> findByCreatedBy(String createdBy);

    // Recherche des licences créées dans une période
    List<LicenseEntity> findByCreatedAtBetween(Date startDate, Date endDate);

    // Recherche des licences mises à jour dans une période
    List<LicenseEntity> findByUpdatedAtBetween(Date startDate, Date endDate);

    // Recherche des licences par produit et version
    List<LicenseEntity> findByProductNameAndProductVersion(String productName, String productVersion);

    // Recherche des licences actives par type
    List<LicenseEntity> findByLicenseTypeAndIsActive(String licenseType, Boolean isActive);

    // Recherche des licences par client et statut actif
    List<LicenseEntity> findByCustomerNameAndIsActive(String customerName, Boolean isActive);

    // Recherche des licences avec usage restant
    @Query("{ $or: [ { 'maxUsage': null }, { $expr: { $lt: ['$usageCount', '$maxUsage'] } } ] }")
    List<LicenseEntity> findLicensesWithUsageLeft();

    // Recherche des licences par empreinte matérielle
    Optional<LicenseEntity> findByHardwareFingerprint(String hardwareFingerprint);

    // Compter les licences par type
    long countByLicenseType(String licenseType);

    // Compter les licences actives
    long countByIsActive(Boolean isActive);

    // Compter les licences d'essai
    long countByIsTrial(Boolean isTrial);

    // Compter les licences expirées
    @Query(value = "{ 'expiryDate' : { $lt: ?0 } }", count = true)
    long countExpiredLicenses(Date currentDate);

    // Compter les licences par client
    long countByCustomerName(String customerName);

    // Compter les licences par organisation
    long countByOrganization(String organization);

    // Recherche des licences par nom contenant (recherche partielle)
    List<LicenseEntity> findByLicenseNameContainingIgnoreCase(String licenseName);

    // Recherche des licences par client contenant (recherche partielle)
    List<LicenseEntity> findByCustomerNameContainingIgnoreCase(String customerName);

    // Recherche des licences par organisation contenant (recherche partielle)
    List<LicenseEntity> findByOrganizationContainingIgnoreCase(String organization);

    // Recherche des licences avec des fonctionnalités spécifiques
    @Query("{ 'features' : { $in: [?0] } }")
    List<LicenseEntity> findByFeaturesContaining(String feature);

    // Recherche des licences avec restrictions IP
    @Query("{ 'ipRestrictions' : { $exists: true, $ne: [] } }")
    List<LicenseEntity> findLicensesWithIpRestrictions();

    // Recherche des licences avec restrictions de domaine
    @Query("{ 'domainRestrictions' : { $exists: true, $ne: [] } }")
    List<LicenseEntity> findLicensesWithDomainRestrictions();

    // Recherche des licences validées récemment
    List<LicenseEntity> findByLastValidationAfter(Date date);

    // Recherche des licences par nombre maximum d'utilisateurs
    List<LicenseEntity> findByMaxUsersGreaterThanEqual(Integer maxUsers);

    // Recherche des licences par nombre maximum de projets
    List<LicenseEntity> findByMaxProjectsGreaterThanEqual(Integer maxProjects);

    // Recherche complexe : licences actives, non expirées et avec usage restant
    @Query("{ 'isActive': true, 'expiryDate': { $gte: ?0 }, $or: [ { 'maxUsage': null }, { $expr: { $lt: ['$usageCount', '$maxUsage'] } } ] }")
    List<LicenseEntity> findValidUsableLicenses(Date currentDate);

    // Recherche des licences nécessitant une attention (expiration proche ou usage élevé)
    @Query("{ $or: [ " +
           "{ 'expiryDate': { $gte: ?0, $lte: ?1 } }, " +
           "{ $expr: { $gte: [{ $divide: ['$usageCount', '$maxUsage'] }, 0.8] } } " +
           "] }")
    List<LicenseEntity> findLicensesNeedingAttention(Date currentDate, Date warningDate);

    // Méthodes de comptage pour les statistiques du dashboard
    @Query(value = "{ 'expiryDate': { $gte: ?0, $lte: ?1 }, 'isActive': ?2 }", count = true)
    long countByExpiryDateBetweenAndIsActive(Date startDate, Date endDate, Boolean isActive);
    
    @Query(value = "{ 'expiryDate': { $lt: ?0 }, 'isActive': ?1 }", count = true)
    long countByExpiryDateBeforeAndIsActive(Date date, Boolean isActive);
}

