package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Batch;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface BatchRepository extends JpaRepository<Batch, String> {
    List<Batch> findByName(String name);

    Optional<Batch> findById(String idBatch);

    @Query("SELECT COUNT(b) FROM Batch b")
    int countAllBatchs();
}
