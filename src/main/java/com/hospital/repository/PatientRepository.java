package com.hospital.repository;

import com.hospital.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByNationalId(String nationalId);

    List<Patient> findByStatus(Patient.PatientStatus status);

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "p.phoneNumber LIKE CONCAT('%', :keyword, '%') OR " +
           "p.nationalId LIKE CONCAT('%', :keyword, '%')")
    List<Patient> searchPatients(@Param("keyword") String keyword);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.status = 'ACTIVE'")
    Long countActivePatients();
}
