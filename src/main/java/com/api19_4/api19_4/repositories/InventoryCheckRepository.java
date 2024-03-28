package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Bill;
import com.api19_4.api19_4.models.InventoryCheck;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface InventoryCheckRepository extends JpaRepository<InventoryCheck, String > {
    @Query("SELECT COUNT(ic) FROM InventoryCheck ic")
    int countAllInventoryChecks();
    List<InventoryCheck> findAll(Specification<Bill> spec);
}
