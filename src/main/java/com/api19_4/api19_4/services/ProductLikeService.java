package com.api19_4.api19_4.services;

import com.api19_4.api19_4.models.Product;
import com.api19_4.api19_4.models.ProductLike;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ProductLikeService {
    List<ProductLike> getAllProdLike();

    List<Product> getProductsFromProductLikeWithCoupons();

    Optional<ProductLike> findById(String idUser);

    ProductLike createProdLike(ProductLike productLike);

    Optional<ProductLike> getProdLikeById(String id);
}
