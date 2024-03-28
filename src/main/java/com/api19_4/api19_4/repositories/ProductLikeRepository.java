package com.api19_4.api19_4.repositories;
import com.api19_4.api19_4.models.ProductLike;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ProductLikeRepository extends JpaRepository<ProductLike, String>, JpaSpecificationExecutor<ProductLike> {
    @Override
    List<ProductLike> findAll();
    @Override
    Optional<ProductLike> findById(String id);
    @Query("SELECT p FROM ProductLike p JOIN p.product WHERE p.idUser = :idUser AND p.product.coupons <> 0")
    List<ProductLike> findProductLikesByUserIdAndCouponsNotZero(@Param("idUser") String idUser);

    @Query("SELECT pl FROM ProductLike pl WHERE pl.idUser = :idUser AND pl.product.categoryName IN :categories")
    List<ProductLike> findProductLikesByUserIdAndCategories(@Param("idUser") String idUser, @Param("categories") List<String> categories);





    Optional<ProductLike> findByIdUserAndIdProd(String idUser, String idProd);

    List<ProductLike> findByIdUser(String idUser);



}
