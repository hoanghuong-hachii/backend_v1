package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto.BatchDTO;
import com.api19_4.api19_4.dto.UnitDTO;
import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.*;
import com.api19_4.api19_4.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/batches")
public class BatchController {
    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @PostMapping("/add")
    public ResponseEntity<?> createBatch(@RequestBody BatchDTO batchDTO) {
        try {
            int numberOfExistingBatch = batchRepository.countAllBatchs();
            if(numberOfExistingBatch == 0){
                numberOfExistingBatch = 1;
            }else {
                numberOfExistingBatch += 1;
            }
            int numberOfExistingUnit = unitRepository.countAllUnits();
            if(numberOfExistingUnit == 0){
                numberOfExistingUnit = 1;
            }else {
                numberOfExistingUnit += 1;
            }
            IDGenerator hhIDGenerator = new IDGenerator("BB", (int) numberOfExistingBatch);
            IDGenerator UNIDGenerator = new IDGenerator("UN", numberOfExistingUnit);
            // Create a new Batch
            Batch batch = new Batch(hhIDGenerator);
            batch.setName(batchDTO.getName());
            batch.setManufacturingDate(batchDTO.getManufacturingDate());
            batch.setExpirationDate(batchDTO.getExpirationDate());
            batch.setDateImport(batchDTO.getDateImport());
            // Fetch the Warehouse based on the provided ID
            Optional<Warehouse> warehouseOptional = warehouseRepository.findById(batchDTO.getWarehouse_id());
            if (warehouseOptional.isEmpty()) {
                return new ResponseEntity<>("Invalid Warehouse ID", HttpStatus.BAD_REQUEST);
            }

            // Save the Warehouse to the Batch
            Warehouse warehouse = warehouseOptional.get();
            batch.setWarehouse(warehouse);

            // save the supplier to the Batch
            Optional<Supplier> supplierOptional = supplierRepository.findById(batchDTO.getSupplier_id());
            if(supplierOptional.isEmpty()){
                return new ResponseEntity<>("Invalid Supplier ID", HttpStatus.BAD_REQUEST);

            }
            Supplier supplier = supplierOptional.get();
            batch.setSupplier(supplier);

            // Create and save the Units associated with the Batch
            List<Unit> units = new ArrayList<>();
            for (UnitDTO unitDTO : batchDTO.getUnits()) {
                Unit unit = new Unit(UNIDGenerator);
                unit.setProductId(unitDTO.getProductId());
                unit.setUnitName(unitDTO.getUnitName());
                unit.setQuantity(unitDTO.getQuantity());
                unit.setQuantityImport(unitDTO.getQuantity());
                unit.setPurchasePrice(unitDTO.getUnitPrice()*unitDTO.getQuantity());
                unit.setUnitPrice(unitDTO.getUnitPrice());
                unit.setBatch(batch); // Set the Batch for the Unit
                unit.setWarehouse(warehouse);
                units.add(unit);

                Optional<Product> productOptional = productRepository.findById(unit.getProductId());
                if(productOptional.isPresent()){
                    Product product = productOptional.get();
                    product.setQuantityImported(product.getQuantityImported() + unitDTO.getQuantity());
                    product.setQuantity(product.getQuantity() + unitDTO.getQuantity());
                    product.setUnitPrice(unitDTO.getUnitPrice());
                    product.setSupplier(supplier);
                    //product.setWarehouse(warehouse);
                    productRepository.save(product);
                    batch.setProduct(product);
                }else {
                    return new ResponseEntity<>("Invalid Product ID", HttpStatus.BAD_REQUEST);
                }
            }
            batch.setUnits(units);

            // Save the Batch with associated Units to the database
            Batch savedBatch = batchRepository.save(batch);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBatch);
//            return new ResponseEntity<>(savedBatch, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error occurred while creating the Batch", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{idProduct}/batches")
    public ResponseEntity<List<BatchDTO>> getBatchesByProductId(@PathVariable String idProduct) {
        Optional<Product> productOptional = productRepository.findById(idProduct);
        if (productOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOptional.get();
        List<BatchDTO> batchDTOs = product.getBatches().stream()
                .map(batch -> {
                    BatchDTO batchDTO = new BatchDTO();
                    batchDTO.setIdBatch(batch.getIdBatch());
                    batchDTO.setName(batch.getName());
                    batchDTO.setDateImport(batch.getDateImport());
                    batchDTO.setManufacturingDate(batch.getManufacturingDate());
                    batchDTO.setExpirationDate(batch.getExpirationDate());
                    batchDTO.setWarehouse_id(batch.getWarehouse().getIdWarehouse());
                    batchDTO.setSupplier_id(batch.getSupplier().getIdSupplier());

                    List<UnitDTO> unitDTOs = batch.getUnits().stream()
                            .map(unit -> {
                                UnitDTO unitDTO = new UnitDTO();
                                unitDTO.setIdUnit(unit.getIdUnit());
                                unitDTO.setUnitName(unit.getUnitName());
                                unitDTO.setQuantity(unit.getQuantity());
                                unitDTO.setUnitPrice(unit.getUnitPrice());
                                unitDTO.setProductId(product.getIdProd());
                                return unitDTO;
                            })
                            .collect(Collectors.toList());

                    batchDTO.setUnits(unitDTOs);
                    return batchDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(batchDTOs);
    }

    @PutMapping("/{idBatch}/units/{idUnit}")
    public ResponseEntity<?> updateUnitSoldQuantity(@PathVariable String idBatch,
                                                    @PathVariable String idUnit,
                                                    @RequestParam int soldQuantity) {
        Optional<Batch> batchOptional = batchRepository.findById(idBatch);
        if (batchOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Batch batch = batchOptional.get();
        Unit unit = batch.getUnits().stream()
                .filter(u -> u.getIdUnit().equals(idUnit))
                .findFirst()
                .orElse(null);

        if (unit == null) {
            return ResponseEntity.notFound().build();
        }

        if (unit.getQuantity() < soldQuantity) {
            // If the requested sold quantity exceeds the available quantity, return a custom error response.
            String errorMessage = "The requested sold quantity (" + soldQuantity + ") exceeds the available quantity (" + unit.getQuantity() + ") in the unit.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject("error", errorMessage, null));
        }

        Product product = unit.getBatch().getProduct();
        product.setOrderQuantity(product.getOrderQuantity() - soldQuantity);
        product.setQuantity(product.getQuantity() - soldQuantity);
        unit.setSoldQuantity(unit.getSoldQuantity() + soldQuantity);
        unit.setQuantity(unit.getQuantity()-soldQuantity);
        Batch updatedBatch = batchRepository.save(batch);

        // Return the custom JSON response with the updated product.
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject("success", "Update Product successfully", updatedBatch));
    }

//    @PostMapping("/add")
//    public Batch createBatch(@RequestBody BatchDTO batchDTO) {
//        // Set batches to null to ensure batches are not sent in the request
//        Batch batch = new Batch();
//        batch.setName(batch.getName());
//        batch.setExpirationDate(batchDTO.getExpirationDate());
//        batch.setExpirationDate(batchDTO.getExpirationDate());
//        List<Unit> unitList = batch.getUnits();
//        for(Unit unit : unitList){
//            Optional<Product> product =  productRepository.findById(unit.getIdProduct());
//            if(product.isPresent()){
//                Product product1 = product.get();
//                product1.setPurchasePrice(unit.getPurchasePrice());
//                product1.setQuantityImported(unit.getQuantity());
//                unit.setIdProduct(unit.getIdProduct());
//            }
//        }
//
//        batch.setUnits(unitList);
//        return batchRepository.save(batch);
//        // return new ResponseEntity<>(createdWarehouse, HttpStatus.CREATED);
//    }



//    @PostMapping
//    public ResponseEntity<Batch> createBatch(@RequestBody Batch batch) {
//        // Save the batch along with its products
//        for (Product product : batch.getProducts()) {
//            product.setBatch(batch);
//        }
//        Batch savedBatch = batchRepository.save(batch);
//
//        // Now, we need to update the product_id in the Batch table
//        for (Product product : batch.getProducts()) {
//            product.setBatch(savedBatch);
//        }
//
//        // Save the updated products
//        productRepository.saveAll(batch.getProducts());
//
//        return new ResponseEntity<>(savedBatch, HttpStatus.CREATED);
//    }
}
