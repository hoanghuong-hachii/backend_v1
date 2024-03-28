package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.dto.ProductWithImageDto;
import com.api19_4.api19_4.models.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional

public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    List<Product> findByCategoryName(String categoryName);
    List<Product> findByProductNameContainingIgnoreCase(String productName);

    List<Product> findByRetailPriceBetween(Float startPrice, Float endPrice);

    List<Product> findByCouponsNot(float coupons);

    List<Product> findByRetailPriceBetweenAndProductNameContainingIgnoreCase(
            Float startPrice, Float endPrice, String productName);

    List<Product> findByCategoryNameAndProductName(String categoryName, String productName);

    List<Product> findByCategoryNameAndProductNameContainingIgnoreCase(String categoryName, String productName);

    List<Product> findByProductName(String productName);

    Optional<Product> findById(String idProd);
    Optional<Product> findByProductNameIgnoreCase(String productName);
    boolean existsById(String id);

    void deleteById(String id);
    @Query("SELECT COUNT(p) FROM Product p")
    int countAllProducts();

    Product findTopByOrderByIdProdDesc();


//    @Query("SELECT new com.api19_4.api19_4.models.ProductWithImageDTO(p.idProd, p.brand, p.origin, p.detail, p.productName, p.price, p.categoryName, p.coupons, p.initialorderquantity, p.weight, i.imageAvatar, i.imageDetail) FROM Product p JOIN ImageProductAvatar i ON p.idProd = i.idProd WHERE p.idProd = :productId")
//    ProductWithImageDto findProductWithImageById(@Param("productId") Long productId);



}
