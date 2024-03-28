package com.api19_4.api19_4.services;

import com.api19_4.api19_4.models.Product;
import com.api19_4.api19_4.models.ProductLike;
import com.api19_4.api19_4.repositories.ProductLikeRepository;
import com.api19_4.api19_4.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductLikeServiceImpl implements ProductLikeService {

    @Autowired
    private ProductLikeRepository productLikeRepository;
    private ProductRepository productRepository;

    @Override
    public List<ProductLike> getAllProdLike() {
        return productLikeRepository.findAll();
    }

    // Inside ProductLikeServiceImpl
    @Override
    public List<Product> getProductsFromProductLikeWithCoupons() {
        List<ProductLike> productLikes = productLikeRepository.findAll();
        List<Product> productsWithCoupons = new ArrayList<>();

        for (ProductLike productLike : productLikes) {
            String idProd = productLike.getIdProd();

            Optional<Product> optionalProduct = productRepository.findById(idProd);

            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                // Check if the product has coupons
                if (product.getCoupons() != 0.0) {
                    productsWithCoupons.add(product);
                }
            }
        }

        return productsWithCoupons;
    }


    @Override
    public Optional<ProductLike> findById(String idUser) {
        return productLikeRepository.findById(idUser);
    }

    @Override
    public ProductLike createProdLike(ProductLike productLike) {
        return productLikeRepository.save(productLike);
    }

    @Override
    public Optional<ProductLike> getProdLikeById(String id) {
        return productLikeRepository.findById(id);
    }
}
