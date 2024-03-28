package com.api19_4.api19_4.repositories;
import com.api19_4.api19_4.models.PurchaseOrder;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface PurchaseOrderRepository  extends JpaRepository<PurchaseOrder, String> {
    List<PurchaseOrder> findAll(Specification<PurchaseOrder> spec);
    @Query("SELECT COUNT(po) FROM PurchaseOrder po")
    int countAllPurchaseOrders();


    Optional<PurchaseOrder> findById(String idPurchaseOrder);
}
