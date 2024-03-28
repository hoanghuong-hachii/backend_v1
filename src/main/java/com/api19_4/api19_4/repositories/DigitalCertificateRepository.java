package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.model.DigitalCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DigitalCertificateRepository extends JpaRepository<DigitalCertificate, Long> {

    Optional<DigitalCertificate> findFirstByOrderByCertSerialNumberDesc();
}
