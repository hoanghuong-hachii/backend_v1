package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Supplier;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface SupplierRepository extends JpaRepository<Supplier, String> {
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Supplier> findByName(@Param("keyword")String name);

    @Query("SELECT s FROM Supplier s WHERE s.name = :name")
    Supplier findByNameSupplier(@Param("name") String name);

    Optional<Supplier> findById(String supplierId);
    @Query("SELECT COUNT(s) FROM Supplier s")
    int countAllSuppliers();
}
