package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {
    Optional<Warehouse> findByName(String name);

    List<Warehouse> findByNameContainingIgnoreCase(String name);

    Optional<Warehouse> findById(String warehouseId);

    @Query("SELECT COUNT(w) FROM Warehouse w")
    int countAllWareHouses();
}
