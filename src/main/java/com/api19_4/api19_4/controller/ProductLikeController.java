package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.models.ProductLike;
import com.api19_4.api19_4.models.ResponseObject;
import com.api19_4.api19_4.repositories.ProductLikeRepository;
import com.api19_4.api19_4.services.ProductLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//@CrossOrigin(origins = "http://127.0.0.1:5000")
//@CrossOrigin(origins = "http://192.168.60.5:8080")
@RestController
@RequestMapping(path = "/api/v6/ProdLike")
@SpringBootApplication
@ComponentScan(basePackages = "com.api19_4.api19_4")
public class ProductLikeController {

    @Autowired
    private ProductLikeRepository repository;
    @Autowired
    private ProductLikeService prodLikeService;
    @GetMapping("")
    public ResponseEntity<List<Map<String, Object>>> getAllProdLike() {
        try {
            List<ProductLike> productLikes = repository.findAll();
            List<Map<String, Object>> desiredResponse = new ArrayList<>();

            for (ProductLike productLike : productLikes) {
                Map<String, Object> prodLikeMap = new HashMap<>();
                prodLikeMap.put("idProdLike", productLike.getIdProdLike());
                prodLikeMap.put("idUser", productLike.getIdUser());
                prodLikeMap.put("idProd", productLike.getIdProd());

                // Construct the 'product' object
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("idProd", productLike.getProduct().getIdProd());
                productInfo.put("productName", productLike.getProduct().getProductName());
                productInfo.put("retailPrice", productLike.getProduct().getRetailPrice());
                productInfo.put("unitName", productLike.getProduct().getUnitName());
                productInfo.put("quantityImported", productLike.getProduct().getQuantityImported());
                productInfo.put("orderQuantity", productLike.getProduct().getOrderQuantity());
                productInfo.put("quantity", productLike.getProduct().getQuantity());
                productInfo.put("categoryName", productLike.getProduct().getCategoryName());
                productInfo.put("coupons", productLike.getProduct().getCoupons());
                productInfo.put("brand", productLike.getProduct().getBrand());
                productInfo.put("origin", productLike.getProduct().getOrigin());
                productInfo.put("detail", productLike.getProduct().getDetail());
                productInfo.put("unitPrice", productLike.getProduct().getUnitPrice());

                productInfo.put("imageAvatar", productLike.getProduct().getImageAvatar());
                productInfo.put("imageQR", productLike.getProduct().getImageQR());

                prodLikeMap.put("product", productInfo);

                desiredResponse.add(prodLikeMap);
            }

            return ResponseEntity.ok(desiredResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createProductLike(
            @RequestParam String idUser,
            @RequestParam String idProd
    ) {
        try {

            Optional<ProductLike> optionalProductLike = repository.findByIdUserAndIdProd(idUser, idProd);

            if (optionalProductLike.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Product like already exists for this user and product.");
            }
            ProductLike productLike = new ProductLike();
            productLike.setIdUser(idUser);
            productLike.setIdProd(idProd);

            ProductLike createdProductLike = repository.save(productLike);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdProductLike);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the product like.");
        }
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<Map<String, Object>>> getProdLikeByUserId(@PathVariable String idUser) {
        try {
            List<ProductLike> productLikes = repository.findByIdUser(idUser);
            List<Map<String, Object>> desiredResponse = new ArrayList<>();

            for (ProductLike productLike : productLikes) {
                Map<String, Object> prodLikeMap = new HashMap<>();
                prodLikeMap.put("idProdLike", productLike.getIdProdLike());
                prodLikeMap.put("idUser", productLike.getIdUser());
                prodLikeMap.put("idProd", productLike.getIdProd());

                // Construct the 'product' object
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("idProd", productLike.getProduct().getIdProd());
                productInfo.put("productName", productLike.getProduct().getProductName());
                productInfo.put("retailPrice", productLike.getProduct().getRetailPrice());
                productInfo.put("unitName", productLike.getProduct().getUnitName());
                productInfo.put("quantityImported", productLike.getProduct().getQuantityImported());
                productInfo.put("orderQuantity", productLike.getProduct().getOrderQuantity());
                productInfo.put("quantity", productLike.getProduct().getQuantity());
                productInfo.put("categoryName", productLike.getProduct().getCategoryName());
                productInfo.put("coupons", productLike.getProduct().getCoupons());
                productInfo.put("brand", productLike.getProduct().getBrand());
                productInfo.put("origin", productLike.getProduct().getOrigin());
                productInfo.put("detail", productLike.getProduct().getDetail());
                productInfo.put("unitPrice", productLike.getProduct().getUnitPrice());
                productInfo.put("imageAvatar", productLike.getProduct().getImageAvatar());
                productInfo.put("imageQR", productLike.getProduct().getImageQR());

                prodLikeMap.put("product", productInfo);

                desiredResponse.add(prodLikeMap);
            }

            return ResponseEntity.ok(desiredResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @DeleteMapping("")
    ResponseEntity<ResponseObject> deleteProductLike(@RequestParam String idUser, @RequestParam String idProd){
        Optional<ProductLike> exists = repository.findByIdUserAndIdProd(idUser, idProd);
        if (exists.isPresent()) {
            ProductLike productLike = exists.get();
            repository.delete(productLike);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Delete product successfully", "")
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject("failed", "Cannot find product to delete", "")
            );
        }
    }

    @DeleteMapping("all")
    ResponseEntity<ResponseObject> deleteAllShoppingCart(){
        List<ProductLike> productLikeList = repository.findAll();
        if (!productLikeList.isEmpty()) {
            for(ProductLike productLike : productLikeList){
                repository.delete(productLike);
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Delete product successfully", "")
            );


        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("failed", "No products found to delete", ""));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkProductLike(
            @RequestParam String idUser,
            @RequestParam String idProd
    ) {
        try {
            Optional<ProductLike> optionalProductLike = repository.findByIdUserAndIdProd(idUser, idProd);

            // If the product like exists, return true
            return ResponseEntity.ok(optionalProductLike.isPresent());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }





    @GetMapping("/user/{idUser}/product-likes-with-coupons")
    public ResponseEntity<List<Map<String, Object>>> getProductLikesByUserIdAndCouponsNotZero(@PathVariable String idUser) {
        try {
            List<ProductLike> productLikesWithCoupons = repository.findProductLikesByUserIdAndCouponsNotZero(idUser);
            List<Map<String, Object>> desiredResponse = new ArrayList<>();

            for (ProductLike productLike : productLikesWithCoupons) {
                Map<String, Object> prodLikeMap = new HashMap<>();
                prodLikeMap.put("idProdLike", productLike.getIdProdLike());
                prodLikeMap.put("idUser", productLike.getIdUser());
                prodLikeMap.put("idProd", productLike.getIdProd());

                // Construct the 'product' object
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("idProd", productLike.getProduct().getIdProd());
                productInfo.put("productName", productLike.getProduct().getProductName());
                productInfo.put("retailPrice", productLike.getProduct().getRetailPrice());
                productInfo.put("unitName", productLike.getProduct().getUnitName());
                productInfo.put("quantityImported", productLike.getProduct().getQuantityImported());
                productInfo.put("orderQuantity", productLike.getProduct().getOrderQuantity());
                productInfo.put("quantity", productLike.getProduct().getQuantity());
                productInfo.put("categoryName", productLike.getProduct().getCategoryName());
                productInfo.put("coupons", productLike.getProduct().getCoupons());
                productInfo.put("brand", productLike.getProduct().getBrand());
                productInfo.put("origin", productLike.getProduct().getOrigin());
                productInfo.put("detail", productLike.getProduct().getDetail());
                productInfo.put("unitPrice", productLike.getProduct().getUnitPrice());
                productInfo.put("imageAvatar", productLike.getProduct().getImageAvatar());
                productInfo.put("imageQR", productLike.getProduct().getImageQR());

                prodLikeMap.put("product", productInfo);

                desiredResponse.add(prodLikeMap);
            }

            return ResponseEntity.ok(desiredResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/user/{idUser}/product-likes-by-categories")
    public ResponseEntity<List<Map<String, Object>>> getProductLikesByUserAndCategories(
            @PathVariable String idUser,
            @RequestParam List<String> categories
    ) {
        try {
            List<ProductLike> productLikes = repository.findProductLikesByUserIdAndCategories(idUser, categories);
            List<Map<String, Object>> desiredResponse = new ArrayList<>();

            for (ProductLike productLike : productLikes) {
                Map<String, Object> prodLikeMap = new HashMap<>();
                prodLikeMap.put("idProdLike", productLike.getIdProdLike());
                prodLikeMap.put("idUser", productLike.getIdUser());
                prodLikeMap.put("idProd", productLike.getIdProd());

                // Construct the 'product' object
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("idProd", productLike.getProduct().getIdProd());
                productInfo.put("productName", productLike.getProduct().getProductName());
                productInfo.put("retailPrice", productLike.getProduct().getRetailPrice());
                productInfo.put("unitName", productLike.getProduct().getUnitName());
                productInfo.put("quantityImported", productLike.getProduct().getQuantityImported());
                productInfo.put("orderQuantity", productLike.getProduct().getOrderQuantity());
                productInfo.put("quantity", productLike.getProduct().getQuantity());
                productInfo.put("categoryName", productLike.getProduct().getCategoryName());
                productInfo.put("coupons", productLike.getProduct().getCoupons());
                productInfo.put("brand", productLike.getProduct().getBrand());
                productInfo.put("origin", productLike.getProduct().getOrigin());
                productInfo.put("detail", productLike.getProduct().getDetail());
                productInfo.put("unitPrice", productLike.getProduct().getUnitPrice());
                productInfo.put("imageAvatar", productLike.getProduct().getImageAvatar());
                productInfo.put("imageQR", productLike.getProduct().getImageQR());

                prodLikeMap.put("product", productInfo);

                desiredResponse.add(prodLikeMap);
            }

            return ResponseEntity.ok(desiredResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}
