package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.ResponseObject;
import com.api19_4.api19_4.models.ShoppingCart;
import com.api19_4.api19_4.repositories.ShoppingCartRepository;
import com.api19_4.api19_4.services.ShoppingCartServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

//@CrossOrigin(origins = "http://127.0.0.1:5000")
//@CrossOrigin(origins = "http://192.168.60.5:8080")
@RestController
@RequestMapping(path = "/api/v4/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartRepository repository;
    @Autowired
    private ShoppingCartServices shoppingCartServices;

    @GetMapping("")
    List<ShoppingCart> getAllShoppingCart(){
        return shoppingCartServices.getAllShoppingCarts();
    }


    @PostMapping("")
    public ResponseEntity<?> createShoppingCart(
            @RequestParam String idUser,
            @RequestParam String idProd,
            @RequestParam Double price) {

        Optional<ShoppingCart> optionalShoppingCart = repository.findByIdUserAndIdProd(idUser, idProd);
        ShoppingCart shoppingCart;




        int numberOfExistShoppingCart = repository.countAllShoppingCart();
        if(numberOfExistShoppingCart == 0){
            numberOfExistShoppingCart = 1;
        }
        else {
            // Tìm ShoppingCart cuối cùng trong cơ sở dữ liệu
            ShoppingCart lastShoppingCart = repository.findTopByOrderByIdShCartDesc();

            // Lấy số từ ID của ShoppingCart cuối cùng
            String lastShoppingCartId = lastShoppingCart != null ? lastShoppingCart.getIdShCart() : "SC0";
            int lastNumber = Integer.parseInt(lastShoppingCartId.replace("SC", ""));

            // Tăng số lượng lên 1 để tạo ID mới
            int newNumber = lastNumber + 1;
            numberOfExistShoppingCart = newNumber;
        }
        IDGenerator idGenerator = new IDGenerator( "SC", numberOfExistShoppingCart);
        if (optionalShoppingCart.isPresent()) {
            shoppingCart = optionalShoppingCart.get();
            // Cập nhật số lượng và tính lại totalPrice
            int newQuantityProd = shoppingCart.getQuantityProd() + 1;
            shoppingCart.setQuantityProd(newQuantityProd);
            shoppingCart.setTotalPrice(newQuantityProd * shoppingCart.getPrice());
        } else {
            shoppingCart = new ShoppingCart(idGenerator);
            shoppingCart.setIdUser(idUser);
            shoppingCart.setIdProd(idProd);
            shoppingCart.setQuantityProd(1);
            shoppingCart.setPrice(price);
            shoppingCart.setTotalPrice(1 * price);
        }

        ShoppingCart createdShoppingCart = shoppingCartServices.createShoppingCart(shoppingCart);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShoppingCart);
    }
    @PutMapping("")
    public ResponseEntity<?> updateShoppingCart(
            @RequestParam String idUser,
            @RequestParam String idProd,
            @RequestParam int quantityProd,
            @RequestParam Double price) {

        Optional<ShoppingCart> optionalShoppingCart = repository.findByIdUserAndIdProd(idUser, idProd);

        ShoppingCart shoppingCart;
        if(quantityProd <= 0){
            Optional<ShoppingCart> exists = repository.findByIdUserAndIdProd(idUser, idProd);
            if (exists.isPresent()) {
                shoppingCart = exists.get();
                repository.delete(shoppingCart);
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject("ok", "Delete product successfully", "")
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ResponseObject("failed", "Cannot find product", "")
                );
            }
        }else {
            if (optionalShoppingCart.isPresent()) {
                shoppingCart = optionalShoppingCart.get();
                // Cập nhật số lượng và tính lại totalPrice
                shoppingCart.setQuantityProd(quantityProd);
                shoppingCart.setTotalPrice(quantityProd * shoppingCart.getPrice());
            } else {
                shoppingCart = new ShoppingCart();
                shoppingCart.setIdUser(idUser);
                shoppingCart.setIdProd(idProd);
                shoppingCart.setQuantityProd(quantityProd);
                shoppingCart.setPrice(price);
                shoppingCart.setTotalPrice(quantityProd * price);
            }
        }


        ShoppingCart createdShoppingCart = shoppingCartServices.createShoppingCart(shoppingCart);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShoppingCart);
    }


    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<ShoppingCart>> getShoppingCartsByUserId(@PathVariable String idUser) {
        List<ShoppingCart> shoppingCarts = repository.findByIdUser(idUser);
        return ResponseEntity.ok(shoppingCarts);
    }

    @DeleteMapping("")
    ResponseEntity<ResponseObject> deleteShoppingCart(@RequestParam String idUser, @RequestParam String idProd){
        Optional<ShoppingCart> exists = repository.findByIdUserAndIdProd(idUser, idProd);
        if (exists.isPresent()) {
            ShoppingCart shoppingCart = exists.get();
            repository.delete(shoppingCart);
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
        List<ShoppingCart> shoppingCartList = repository.findAll();
        if (!shoppingCartList.isEmpty()) {
            for(ShoppingCart shoppingCart : shoppingCartList){
                repository.delete(shoppingCart);
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Delete product successfully", "")
            );


        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
             new ResponseObject("failed", "No products found to delete", ""));
    }

    @DeleteMapping("/user/{idUser}")
    public ResponseEntity<ResponseObject> deleteShoppingCartByUserId(@PathVariable String idUser) {
        List<ShoppingCart> shoppingCartList = repository.findByIdUser(idUser);

        if (!shoppingCartList.isEmpty()) {
            for (ShoppingCart shoppingCart : shoppingCartList) {
                repository.delete(shoppingCart);
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Delete shopping cart items for user successfully", "")
            );
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("failed", "No shopping cart items found for the user to delete", "")
        );
    }



}
