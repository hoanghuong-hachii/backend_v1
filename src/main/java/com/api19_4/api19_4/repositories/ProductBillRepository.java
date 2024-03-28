package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.ProductBill;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ProductBillRepository extends JpaRepository<ProductBill, Long>{
    @Query("SELECT COUNT(pb) FROM ProductBill pb")
    int countAllProductBills();

    ProductBill findByProductId(String productId);

//    List<ProductBill> findByIdBill1AndIdUser(Long idBill1, Long idUser);

}
