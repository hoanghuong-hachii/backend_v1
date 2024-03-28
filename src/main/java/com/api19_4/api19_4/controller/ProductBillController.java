package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.models.ProductBill;
import com.api19_4.api19_4.repositories.ProductBillRepository;
import com.api19_4.api19_4.services.ProductBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

//@CrossOrigin(origins = "http://127.0.0.1:5000")
//@CrossOrigin(origins = "http://192.168.60.5:8080")
@RestController
@RequestMapping(path = "/api/v6/ProdBill")
@SpringBootApplication
@ComponentScan(basePackages = "com.api19_4.api19_4") // Thay thế bằng package chứa các bean của ứng dụng của bạn
public class ProductBillController {
    @Autowired
    private ProductBillRepository repository;
    @Autowired
    private ProductBillService prodBillService;
    @GetMapping("")
    List<ProductBill> getAllProdBills(){
        return repository.findAll();
    }
//===========================search============================================
//    @GetMapping("/search")
//    public ResponseEntity<List<ProductBill>> getProdBill(@RequestParam Long idBill1, @RequestParam Long idUser){
//        List<ProductBill> productBills;
//        productBills = repository.findByIdBill1AndIdUser(idBill1, idUser);
//        if(!productBills.isEmpty()){
//            return ResponseEntity.ok(productBills);
//        }else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        }
//
//
//    }


}
