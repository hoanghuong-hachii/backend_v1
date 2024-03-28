package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.ProductBill;
import com.api19_4.api19_4.models.ProductCheck;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface ProductCheckRepository extends JpaRepository<ProductCheck, String> {
    @Query("SELECT COUNT(pc) FROM ProductCheck pc")
    int countAllProductChecks();
}
