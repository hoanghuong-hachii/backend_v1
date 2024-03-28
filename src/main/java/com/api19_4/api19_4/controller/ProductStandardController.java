package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto.BatchDTO;
import com.api19_4.api19_4.dto.ProductDto;
import com.api19_4.api19_4.dto.ProductStandardDTO;
import com.api19_4.api19_4.dto.UnitDTO;
import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.*;
import com.api19_4.api19_4.repositories.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
//@CrossOrigin(origins = "http://127.0.0.1:5000")
@RequestMapping("/api/v1/productStandard")
public class ProductStandardController {
    @Autowired
    private ProductStandardRepository productStandardRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/add")
    public ResponseEntity<?> createBatch(@RequestBody ProductStandardDTO productStandardDTO) {
        try {
            // Create a new ProductStandard
            int numberOfExistingUnit = unitRepository.countAllUnits();
            if(numberOfExistingUnit == 0){
                numberOfExistingUnit = 1;
            }else {
                numberOfExistingUnit += 1;
            }
            int numberOfExistingProductStandard = productStandardRepository.countAllProductStandards();
            if(numberOfExistingProductStandard == 0){
                numberOfExistingProductStandard = 1;
            }else {
                numberOfExistingProductStandard  += 1;
            }
            IDGenerator idGenerator = new IDGenerator("PS", numberOfExistingProductStandard);
            IDGenerator idGenerator1 = new IDGenerator("UN", numberOfExistingUnit);
            ProductStandard productStandard = new ProductStandard(idGenerator);
            productStandard.setName(productStandardDTO.getName());
            productStandard.setDateImport(productStandardDTO.getDateImport());

            // Fetch the Warehouse based on the provided ID
            Optional<Warehouse> warehouseOptional = warehouseRepository.findById(productStandardDTO.getWarehouse_id());
            if (warehouseOptional.isEmpty()) {
                return new ResponseEntity<>("Invalid Warehouse ID", HttpStatus.BAD_REQUEST);
            }
            // Save the Warehouse to the Batch
            Warehouse warehouse = warehouseOptional.get();
            productStandard.setWarehouse(warehouse);

            // save the supplier to the Batch
            Optional<Supplier> supplierOptional = supplierRepository.findById(productStandardDTO.getSupplier_id());
            if(supplierOptional.isEmpty()){
                return new ResponseEntity<>("Invalid Supplier ID", HttpStatus.BAD_REQUEST);

            }
            Supplier supplier = supplierOptional.get();
            productStandard.setSupplier(supplier);
            double totalPurchasePrice = 0;
            // Create and save the Units associated with the Batch
            List<Unit> units = new ArrayList<>();
            for (UnitDTO unitDTO : productStandardDTO.getUnits()) {
                Unit unit = new Unit(idGenerator1);
                unit.setProductId(unitDTO.getProductId());
                unit.setUnitName(unitDTO.getUnitName());
                unit.setQuantity(unitDTO.getQuantity());
                unit.setQuantityImport(unitDTO.getQuantity());
                unit.setPurchasePrice(unitDTO.getUnitPrice()* unitDTO.getQuantity());
                totalPurchasePrice += unitDTO.getUnitPrice()* unitDTO.getQuantity();
                unit.setUnitPrice(unitDTO.getUnitPrice());
                unit.setProductStandard(productStandard); // Set the Batch for the Unit
                unit.setWarehouse(warehouse);
                units.add(unit);

                Optional<Product> productOptional = productRepository.findById(unit.getProductId());
                if(productOptional.isPresent()){
                    Product product = productOptional.get();
                    product.setQuantityImported(product.getQuantityImported() + unitDTO.getQuantity());
                    product.setQuantity(product.getQuantity() + unitDTO.getQuantity());
                    product.setUnitPrice(unitDTO.getUnitPrice());
                    product.setSupplier(supplier);
//                    product.setWarehouse(warehouse);
                    productRepository.save(product);
                    productStandard.setProduct(product);
                }else {
                    return new ResponseEntity<>("Invalid Product ID", HttpStatus.BAD_REQUEST);
                }
            }
            productStandard.setUnits(units);
            productStandard.setTotalPurchasePrice(totalPurchasePrice);
            productStandard.setStatus(productStandardDTO.getStatus());
            productStandard.setNote(productStandardDTO.getNote());
            // Save the Batch with associated Units to the database
            ProductStandard saved = productStandardRepository.save(productStandard);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
//            return new ResponseEntity<>(savedBatch, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error occurred while creating the ProductStandard", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProductStandard(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String nameSupplier,
            @RequestParam(required = false) String nameWarehouse,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate
    ) {
        List<ProductStandard> productStandards = productStandardRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }

            if(nameSupplier != null && !nameSupplier.isEmpty()){
                Join<ProductStandard, Supplier> supplierJoin = root.join("supplier");
                predicates.add(criteriaBuilder.like(supplierJoin.get("name"), "%" + nameSupplier + "%"));
            }

            if (nameWarehouse != null && !nameWarehouse.isEmpty()) {
                Join<ProductStandard, Warehouse> warehouseJoin = root.join("warehouse"); // Liên kết với bảng Warehouse
                predicates.add(criteriaBuilder.like(warehouseJoin.get("name"), "%" + nameWarehouse + "%"));
            }

            if (startDate != null && endDate != null) {
                // Trường hợp có cả startDate và endDate
                predicates.add(criteriaBuilder.between(root.get("dateImport"), startDate, endDate));
            } else if (startDate != null) {
                // Trường hợp chỉ có startDate
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateImport"), startDate));
            } else if (endDate != null) {
                // Trường hợp chỉ có endDate
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateImport"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        if (productStandards.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Chuyển đổi danh sách sản phẩm tìm thấy thành danh sách DTO hoặc các đối tượng phù hợp khác
        List<ProductStandardDTO> productStandardDTOs = new ArrayList<>();
        for (ProductStandard productStandard : productStandards) {
            // Tạo DTO từ productStandard và thêm vào danh sách
            ProductStandardDTO productStandardDTO = new ProductStandardDTO();
            // Map các trường từ productStandard sang productStandardDTO
            productStandardDTO.setIdProductStandard(productStandard.getIdProductStandard());
            productStandardDTO.setName(productStandard.getName());
            productStandardDTO.setDateImport(productStandard.getDateImport());
            productStandardDTO.setSupplier_id(productStandard.getSupplier().getIdSupplier());
            productStandardDTO.setNameSupplier(productStandard.getSupplier().getName());
            productStandardDTO.setWarehouse_id(productStandard.getWarehouse().getIdWarehouse());
            productStandardDTO.setNameWarehouse(productStandard.getWarehouse().getName());
            productStandardDTO.setTotalPrice(productStandard.getTotalPurchasePrice());
            productStandardDTO.setStatus(productStandard.getStatus());
            productStandardDTO.setNote(productStandard.getNote());
            productStandardDTOs.add(productStandardDTO);
        }

        return ResponseEntity.ok(productStandardDTOs);
    }

    @GetMapping("productInProductStandard")
    public ResponseEntity<?> getProduct(@RequestParam String idProductStandard){
        Optional<ProductStandard> productStandardOptional = productStandardRepository.findById(idProductStandard);
        if(productStandardOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        ProductStandard productStandard = productStandardOptional.get();
        List<Unit> units = productStandard.getUnits();
        List<Product> products = new ArrayList<>();
        for (Unit unit : units){
            Optional<Product> productOptional = productRepository.findById(unit.getProductId());
            Product product = productOptional.get();
            products.add(product);
        }
        List<ProductDto> productDtos = products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

    @GetMapping("/{idProduct}/productStandards")
    public ResponseEntity<List<ProductStandardDTO>> getProductStandardByProductId(@PathVariable String idProduct) {
        Optional<Product> productOptional = productRepository.findById(idProduct);
        if (productOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOptional.get();
        List<ProductStandardDTO> productStandardDTOS = product.getProductStandards().stream()
                .map(productStandard -> {
                    ProductStandardDTO productStandardDTO = new ProductStandardDTO();
                    productStandardDTO.setName(productStandard.getName());
                    productStandardDTO.setDateImport(productStandard.getDateImport());
                    productStandardDTO.setWarehouse_id(productStandard.getWarehouse().getIdWarehouse());
                    productStandardDTO.setSupplier_id(productStandard.getSupplier().getIdSupplier());

                    List<UnitDTO> unitDTOs = productStandard.getUnits().stream()
                            .map(unit -> {
                                UnitDTO unitDTO = new UnitDTO();
                                unitDTO.setProductId(product.getIdProd());
                                unitDTO.setUnitName(unit.getUnitName());
                                unitDTO.setQuantity(unit.getQuantity());
                                unitDTO.setUnitPrice(unit.getUnitPrice());
                                return unitDTO;
                            })
                            .collect(Collectors.toList());

                    productStandardDTO.setUnits(unitDTOs);
                    return productStandardDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(productStandardDTOS);
    }


    @PutMapping("/{idProductStandard}/units/{idUnit}")
    public ResponseEntity<?> updateUnitSoldQuantity(@PathVariable String idProductStandard,
                                                    @PathVariable String idUnit,
                                                    @RequestParam int soldQuantity) {
        Optional<ProductStandard> productStandardOptional = productStandardRepository.findById(idProductStandard);
        if (productStandardOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProductStandard productStandard = productStandardOptional.get();
        Unit unit = productStandard.getUnits().stream()
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

        unit.setSoldQuantity(unit.getQuantity() + soldQuantity);
        unit.setQuantity(unit.getQuantity()-soldQuantity);
        ProductStandard updatedBatch = productStandardRepository.save(productStandard);

        // Return the custom JSON response with the updated product.
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject("success", "Update Product successfully", updatedBatch));
    }


}
