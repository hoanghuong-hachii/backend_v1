package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Bill;
import com.api19_4.api19_4.models.ProductBill;
import com.api19_4.api19_4.models.ProductStandard;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
@Transactional
public interface BillRepository  extends JpaRepository<Bill, Long>{
    @Query("SELECT COUNT(b) FROM Bill b")
    int countAllBills();
    List<Bill> findAll(Specification<Bill> spec);

    List<Bill> findByDateTimeOrderBetween(LocalDateTime hourStart, LocalDateTime hourEnd);
}
