package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.Orderr;
import com.api19_4.api19_4.models.Product;
import com.api19_4.api19_4.repositories.OrderRepository;
import com.api19_4.api19_4.repositories.ProductRepository;
import com.api19_4.api19_4.services.IStorageService;
import com.api19_4.api19_4.services.OrderServices;
import com.api19_4.api19_4.services.ProductServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

//@CrossOrigin(origins = "http://127.0.0.1:5000")
//@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping(path = "/api/v3/order")
public class OrderController {

    @Autowired
    private OrderRepository repository;
    @Autowired
    private OrderServices orderServices;

    @Autowired
    private IStorageService storageService;
    @Autowired
    private ProductRepository Prodrepository;
    @Autowired
    private ProductServices productServices;
    @GetMapping("")
    List<Orderr> getAllProducts(){
        return repository.findAll();
    }
    @PostMapping
    public ResponseEntity<Orderr> createOrder(
            @RequestParam("idUser") String idUser,
            @RequestParam("idProd") String idProd,
            @RequestParam("quantity") int quantity,
            @RequestParam("dateOrder") LocalDateTime dateOrder,
            @RequestParam("address") String address
    ) {
        int numberOfExistingOrder = repository.countAllOrderrs();
        if(numberOfExistingOrder == 0){
            numberOfExistingOrder = 1;
        }else {
            numberOfExistingOrder += 1;
        }
        IDGenerator ODIDGenerator = new IDGenerator("OD", numberOfExistingOrder);
        Orderr orderr = new Orderr(ODIDGenerator);
        orderr.setIdUser(idUser);
        orderr.setIdProd(idProd);
        orderr.setQuantity(quantity);
        orderr.setDateOrder( dateOrder);
        orderr.setAddress(address);

        Orderr createdOrder = orderServices.createOrder(orderr);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }
}
