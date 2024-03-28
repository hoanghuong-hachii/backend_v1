//package com.api19_4.api19_4.controller;
//
//import com.api19_4.api19_4.dto.ProductWithImageDto;
//import com.api19_4.api19_4.services.ProductServicee;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/products")
//public class ProductControllerr {
//    private final ProductServicee productService;
//
//    public ProductControllerr(ProductServicee productService) {
//        this.productService = productService;
//    }
//
//    @GetMapping("/{productId}")
//    public ResponseEntity<ProductWithImageDto> getProductWithImageById(@PathVariable Long productId) {
//        ProductWithImageDto productWithImageDTO = productService.getProductWithImageById(productId);
//        if (productWithImageDTO != null) {
//            return ResponseEntity.ok(productWithImageDTO);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//}
