package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.ProductStandard;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ProductStandardRepository extends JpaRepository<ProductStandard, String> {
    Optional<ProductStandard> findByNameContainingIgnoreCase(String name);

    List<ProductStandard> findAll(Specification<ProductStandard> spec);

    @Query("SELECT COUNT(ps) FROM ProductStandard ps")
    int countAllProductStandards();

    Optional<ProductStandard> findById(String idProductStandard);
}
