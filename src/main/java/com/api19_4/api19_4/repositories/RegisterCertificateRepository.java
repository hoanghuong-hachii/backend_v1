package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.model.DigitalCertificate;
import com.api19_4.api19_4.model.RgisterCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegisterCertificateRepository extends JpaRepository<RgisterCertificate, Long> {
    Optional<RgisterCertificate> findFirstByOrderByCertSerialNumberDesc();
    List<RgisterCertificate> findByNameContaining(String name);
    Optional<RgisterCertificate> findById(Long id); // Using Optional
    List<RgisterCertificate> findByStatus(String status);
//Page<RgisterCertificate> findByNameContaining(String name, Pageable pageable);
//    Optional<RgisterCertificate> findById(Long id); // Using Optional
//    Page<RgisterCertificate> findByStatus(String status, Pageable pageable);
}
