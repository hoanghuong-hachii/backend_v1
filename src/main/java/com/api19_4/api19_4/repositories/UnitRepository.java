package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit, String> {
    @Query("SELECT COUNT(u) FROM Unit u")
    int countAllUnits();

    List<Unit> findByProductId(String productId);
}
