package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto.InventoryCheckDto;
import com.api19_4.api19_4.dto.ProductCheckDto;
import com.api19_4.api19_4.dto.ProductDto;
import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.*;
import com.api19_4.api19_4.repositories.InventoryCheckRepository;
import com.api19_4.api19_4.repositories.ProductCheckRepository;
import com.api19_4.api19_4.repositories.ProductRepository;
import com.api19_4.api19_4.repositories.WarehouseRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//@CrossOrigin(origins = "http://127.0.0.1:5000")
//@CrossOrigin(origins = "http://192.168.60.5:8080")
@RestController
@RequestMapping(path = "/api/v5/InventoryCheck")
@SpringBootApplication
@ComponentScan(basePackages = "com.api19_4.api19_4") // Thay thế bằng package chứa các bean của ứng dụng của bạn
public class InventoryCheckController {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private InventoryCheckRepository inventoryCheckRepository;

    @Autowired
    private ProductCheckRepository productCheckRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    protected ProductRepository productRepository;

    @PostMapping("/add")
    public ResponseEntity<?> postInventoryCheck(@RequestBody InventoryCheckDto inventoryCheckDto){

        int numberOfExistInventoryCheck = inventoryCheckRepository.countAllInventoryChecks();
        if(numberOfExistInventoryCheck == 0){
            numberOfExistInventoryCheck = 1;
        }else{
            numberOfExistInventoryCheck += 1;
        }
        IDGenerator idGenerator = new IDGenerator("IC" , numberOfExistInventoryCheck);
        InventoryCheck inventoryCheck = new InventoryCheck(idGenerator);
        // Chuyển đổi định dạng từ String sang LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        LocalDateTime dateImport = LocalDateTime.parse(inventoryCheckDto.getTimeCheck(), formatter);
        inventoryCheck.setTimeCheck(dateImport);
        inventoryCheck.setTotalQuantityDeviation(inventoryCheckDto.getTotalQuantityDeviation());
        List<ProductCheckDto> productCheckDtoList = inventoryCheckDto.getProductCheckDtoList();
        if(productCheckDtoList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Empty Product list", "")
            );
        }
        int decreasedDeviation = 0;
        int increasedDeviation = 0;
        int totalQuantityDeviation = 0;

        int numberOfExistProductCheck = productCheckRepository.countAllProductChecks();
        if(numberOfExistProductCheck == 0){
            numberOfExistProductCheck = 1;
        }
        else {
            numberOfExistProductCheck += 1;
        }
        IDGenerator idGenerator1 = new IDGenerator("PC", numberOfExistProductCheck);

        int totalQuantityActual = 0;
        double totalValueActual = 0;
        List<ProductCheck> productCheckList = new ArrayList<>();
        for (ProductCheckDto productCheckDto : productCheckDtoList){
            ProductCheck productCheck = new ProductCheck(idGenerator1);

            Optional<Product> productOptional = productRepository.findById(productCheckDto.getIdProd());
            if(productOptional.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty Product", "")
                );
            }
            Product product = productOptional.get();

            Optional<Warehouse> warehouseOptional = warehouseRepository.findByName(productCheckDto.getNameWarehouse());
            if(warehouseOptional.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty Warehouse", "")
                );
            }
            Warehouse warehouse = warehouseOptional.get();
            productCheck.setWarehouse(warehouse);
            productCheck.setProduct(product);
            productCheck.setQuantityActual(productCheckDto.getQuantityActual());
            totalQuantityActual += productCheckDto.getQuantityActual();
            totalValueActual += productCheckDto.getQuantityActual()*product.getUnitPrice();
            productCheck.setInventoryCheck(inventoryCheck);
            productCheck.setQuantityInventory(productCheckDto.getQuantityInventory());
            productCheck.setQuantityDeviation(productCheckDto.getQuantityDeviation());
            productCheck.setValueDeviation(productCheckDto.getQuantityDeviation()*product.getUnitPrice());
            productCheckList.add(productCheck);
            if(productCheckDto.getQuantityDeviation() > 0){
                increasedDeviation += productCheckDto.getQuantityDeviation();
            }
            if(productCheckDto.getQuantityDeviation() < 0){
                decreasedDeviation += productCheckDto.getQuantityDeviation();
            }
            totalQuantityDeviation += productCheckDto.getQuantityDeviation();
        }
        inventoryCheck.setTotalQuantityDeviation(totalQuantityDeviation);
        inventoryCheck.setActualQuantity(totalQuantityActual);
        inventoryCheck.setTotalActualValue(totalValueActual);
        inventoryCheck.setIncreasedDeviation(increasedDeviation);
        inventoryCheck.setDecreasedDeviation(decreasedDeviation);
        inventoryCheck.setProductChecks(productCheckList);
        inventoryCheck.setStatus(inventoryCheckDto.getStatus());
        inventoryCheck.setNote(inventoryCheckDto.getNote());
        InventoryCheck save = inventoryCheckRepository.save(inventoryCheck);
        return ResponseEntity.ok()
                .body(new ResponseObject("ok", "Successful",save));

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInventoryCheck(@PathVariable("id") String id){
        Optional<InventoryCheck> inventoryCheckOptional = inventoryCheckRepository.findById(id);
        if(inventoryCheckOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Invalid InventoryID is", id)
            );
        }
       InventoryCheck inventoryCheck = inventoryCheckOptional.get();
        InventoryCheckDto inventoryCheckDto  = new InventoryCheckDto();
        inventoryCheckDto.setIdInventoryCheck(inventoryCheck.getIdInventoryCheck());
        LocalDateTime dateTime = inventoryCheck.getTimeCheck();
        LocalDateTime BalanceDate = inventoryCheck.getBalanceDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        String formattedDateTime = dateTime.format(formatter);
        if(BalanceDate != null){
            String formattedBalanceDate = BalanceDate.format(formatter);
            inventoryCheckDto.setBalanceDate(formattedBalanceDate);
        }


        inventoryCheckDto.setTimeCheck(formattedDateTime);
        inventoryCheckDto.setStatus(inventoryCheck.getStatus());
        inventoryCheckDto.setActualQuantity(inventoryCheck.getActualQuantity());
        inventoryCheckDto.setTotalActualValue(inventoryCheck.getTotalActualValue());
        inventoryCheckDto.setTotalQuantityDeviation(inventoryCheck.getTotalQuantityDeviation());
        inventoryCheckDto.setIncreasedDeviation(inventoryCheck.getIncreasedDeviation());
        inventoryCheckDto.setDecreasedDeviation(inventoryCheck.getDecreasedDeviation());
        inventoryCheckDto.setNote(inventoryCheck.getNote());
        inventoryCheckDto.setStatus(inventoryCheck.getStatus());

        return new ResponseEntity<>(inventoryCheckDto, HttpStatus.OK);

    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateInventoryCheck(@PathVariable("id") String id, @RequestBody InventoryCheckDto inventoryCheckDto) {
        Optional<InventoryCheck> inventoryCheckOptional = inventoryCheckRepository.findById(id);
        if(inventoryCheckOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Invalid InventoryID is", id)
            );
        }

        InventoryCheck inventoryCheck = inventoryCheckOptional.get();

        // Chuyển đổi định dạng từ String sang LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        LocalDateTime dateImport = LocalDateTime.parse(inventoryCheckDto.getTimeCheck(), formatter);
        inventoryCheck.setBalanceDate(dateImport);
        inventoryCheck.setTotalQuantityDeviation(inventoryCheckDto.getTotalQuantityDeviation());
        inventoryCheck.setTotalActualValue(inventoryCheckDto.getTotalActualValue());
        List<ProductCheckDto> productCheckDtoList = inventoryCheckDto.getProductCheckDtoList();
        if(productCheckDtoList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Empty Product list", "")
            );
        }
        int decreasedDeviation = 0;
        int increasedDeviation = 0;
        double totalValueActual = 0;
        int actualQuantity = 0;
        List<ProductCheck> productCheckListCopy = new ArrayList<>(inventoryCheck.getProductChecks());

        for(ProductCheck productCheck : productCheckListCopy){

            for (ProductCheckDto productCheckDto : productCheckDtoList){
                if(productCheck.getProduct().getIdProd().equals(productCheckDto.getIdProd()) && productCheck.getWarehouse().getName().equals(productCheckDto.getNameWarehouse())){
                    Optional<Product> productOptional = productRepository.findById(productCheckDto.getIdProd());
                    if(productOptional.isEmpty()){
                        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                                new ResponseObject("failed", "Empty Product", "")
                        );
                    }
                    Product product = productOptional.get();
                    productCheck.setQuantityActual(productCheckDto.getQuantityActual());
                    totalValueActual += productCheckDto.getQuantityActual()*product.getUnitPrice();
                    actualQuantity += productCheckDto.getQuantityActual();
                    productCheck.setQuantityDeviation(productCheckDto.getQuantityDeviation());
                    productCheck.setValueDeviation(productCheckDto.getQuantityDeviation()*product.getUnitPrice());
                      // Break the inner loop since we found a match and processed it
                    if(productCheckDto.getQuantityDeviation() > 0){
                        increasedDeviation += productCheckDto.getQuantityDeviation();
                    }
                    if(productCheckDto.getQuantityDeviation() < 0){
                        decreasedDeviation += productCheckDto.getQuantityDeviation();
                    }

                    break;
                }
            }


        }
        inventoryCheck.setTotalActualValue(totalValueActual);
        inventoryCheck.setIncreasedDeviation(increasedDeviation);
        inventoryCheck.setDecreasedDeviation(decreasedDeviation);
        inventoryCheck.setStatus(inventoryCheckDto.getStatus());
        inventoryCheck.setActualQuantity(actualQuantity);
        InventoryCheck save = inventoryCheckRepository.save(inventoryCheck);
        return ResponseEntity.ok()
                .body(new ResponseObject("ok", "Successful",save));

    }


    @GetMapping("/searchByNameWH")
    public ResponseEntity<?> getInventory(
            @RequestParam(required = false) String idInventoryCheck,
            @RequestParam(required = false) String nameWarehouse,
            @RequestParam(required = false) String nameProduct
    ){
        List<InventoryCheck> inventoryChecks = new ArrayList<>();

        if(idInventoryCheck != null && nameWarehouse == null && nameWarehouse == null){
            Optional<InventoryCheck> inventoryCheckOptional = inventoryCheckRepository.findById(idInventoryCheck);
            if(inventoryCheckOptional.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Invalid InventoryID is", idInventoryCheck)
                );
            }
            InventoryCheck inventoryCheck = inventoryCheckOptional.get();
            inventoryChecks.add(inventoryCheck);
        } else if (idInventoryCheck == null && nameWarehouse != null && nameProduct == null) {
            List<InventoryCheck> inventoryCheckList = inventoryCheckRepository.findAll();
            if(inventoryCheckList.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty InventoryCheck List", "")
                );
            }
            for(InventoryCheck inventoryCheck : inventoryCheckList){
                List<ProductCheck> productCheckList = inventoryCheck.getProductChecks();
                if(productCheckList.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                            new ResponseObject("failed", "Empty ProductCheck List", "")
                    );
                }
                for(ProductCheck productCheck : productCheckList){
                    if(productCheck.getWarehouse().getName().equals(nameWarehouse)){
                        inventoryChecks.add(inventoryCheck);
                        continue;
                    }
                }
            }

        } else if (idInventoryCheck == null && nameWarehouse == null && nameProduct != null) {
            List<InventoryCheck> inventoryCheckList = inventoryCheckRepository.findAll();
            if(inventoryCheckList.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty InventoryCheck List", "")
                );
            }
            for(InventoryCheck inventoryCheck : inventoryCheckList){
                List<ProductCheck> productCheckList = inventoryCheck.getProductChecks();
                if(productCheckList.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                            new ResponseObject("failed", "Empty ProductCheck List", "")
                    );
                }
                for(ProductCheck productCheck : productCheckList){
                    if(productCheck.getProduct().getProductName().equals(nameProduct)){
                        inventoryChecks.add(inventoryCheck);
                        continue;
                    }
                }
            }

        } else if(idInventoryCheck != null && nameWarehouse != null && nameProduct == null){
            Optional<InventoryCheck> inventoryCheckOptional = inventoryCheckRepository.findById(idInventoryCheck);
            if(inventoryCheckOptional.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Invalid InventoryID is", idInventoryCheck)
                );
            }
            InventoryCheck inventoryCheck = inventoryCheckOptional.get();
            List<ProductCheck> productCheckList = inventoryCheck.getProductChecks();
            if(productCheckList.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty ProductCheck List", "")
                );
            }
            for(ProductCheck productCheck : productCheckList){
                if(productCheck.getWarehouse().getName().equals(nameWarehouse)){
                    inventoryChecks.add(inventoryCheck);
                    continue;
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Not found", "")
            );
        } else if (idInventoryCheck != null && nameWarehouse == null && nameProduct != null) {
            Optional<InventoryCheck> inventoryCheckOptional = inventoryCheckRepository.findById(idInventoryCheck);
            if(inventoryCheckOptional.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Invalid InventoryID is", idInventoryCheck)
                );
            }
            InventoryCheck inventoryCheck = inventoryCheckOptional.get();
            List<ProductCheck> productCheckList = inventoryCheck.getProductChecks();
            if(productCheckList.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty ProductCheck List", "")
                );
            }
            for(ProductCheck productCheck : productCheckList){
                if(productCheck.getProduct().getProductName().equals(nameProduct)){
                    inventoryChecks.add(inventoryCheck);
                    continue;
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Not found", "")
            );
        } else if (idInventoryCheck == null && nameWarehouse != null && nameProduct != null) {
            List<InventoryCheck> inventoryCheckList = inventoryCheckRepository.findAll();
            if(inventoryCheckList.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty InventoryCheck List ", "")
                );
            }
           for(InventoryCheck inventoryCheck : inventoryCheckList){
               List<ProductCheck> productCheckList = inventoryCheck.getProductChecks();
               if(productCheckList.isEmpty()){
                   return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                           new ResponseObject("failed", "Empty ProductCheck List ", "")
                   );
               }
               for(ProductCheck productCheck : productCheckList){
                   if(productCheck.getWarehouse().getName().equals(nameWarehouse) && productCheck.getProduct().getProductName().equals(nameProduct)){
                       inventoryChecks.add(inventoryCheck);
                       continue;
                   }
               }
           }

        } else if (idInventoryCheck != null && nameWarehouse != null && nameProduct != null) {
            Optional<InventoryCheck> inventoryCheckOptional = inventoryCheckRepository.findById(idInventoryCheck);
            if(inventoryCheckOptional.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty Inventory List ", "")
                );
            }
            InventoryCheck inventoryCheck = inventoryCheckOptional.get();
            List<ProductCheck> productCheckList = inventoryCheck.getProductChecks();
            if(productCheckList.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty ProductCheck List ", "")
                );
            }
            for(ProductCheck productCheck : productCheckList){
                if(productCheck.getWarehouse().getName().equals(nameWarehouse) && productCheck.getProduct().getProductName().equals(nameProduct)){
                    inventoryChecks.add(inventoryCheck);
                }
            }

        } else{
            List<InventoryCheck> inventoryCheckList = inventoryCheckRepository.findAll();
            if(inventoryCheckList.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty Inventory List ", "")
                );
            }
            inventoryChecks = inventoryCheckList;
        }
        List<InventoryCheckDto> inventoryCheckDtoList = inventoryChecks.stream()
                .map(product -> modelMapper.map(product, InventoryCheckDto.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(inventoryCheckDtoList, HttpStatus.OK);
    }

    @GetMapping("/getProductCheckByIdInventory")
    public ResponseEntity<?> getProductCheck(@RequestParam String idInventoryCheck){
        Optional<InventoryCheck> inventoryCheckOptional = inventoryCheckRepository.findById(idInventoryCheck);
        if(inventoryCheckOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Invalid InventoryID is", idInventoryCheck)
            );
        }
        InventoryCheck inventoryCheck = inventoryCheckOptional.get();
        List<ProductCheck> productCheckList = inventoryCheck.getProductChecks();
        if(productCheckList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Invalid ProductCheck is", idInventoryCheck)
            );
        }
       List<ProductCheckDto> productCheckDtoList = new ArrayList<>();
        for(ProductCheck productCheck : productCheckList){
            ProductCheckDto productCheckDto = new ProductCheckDto();
            productCheckDto.setIdProd(productCheck.getProduct().getIdProd());
            productCheckDto.setProductName(productCheck.getProduct().getProductName());
            productCheckDto.setUnitPrice(productCheck.getProduct().getUnitPrice());
            productCheckDto.setUnitName(productCheck.getProduct().getUnitName());
            productCheckDto.setQuantityActual(productCheck.getQuantityActual());
            productCheckDto.setQuantityInventory(productCheck.getQuantityInventory());
            productCheckDto.setQuantityDeviation(productCheck.getQuantityDeviation());
            productCheckDto.setNameWarehouse(productCheck.getWarehouse().getName());
            productCheckDtoList.add(productCheckDto);
        }
        return new ResponseEntity<>(productCheckDtoList, HttpStatus.OK);
    }


}

