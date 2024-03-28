package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto.ProductDto;
import com.api19_4.api19_4.dto.PurchaseOrderDto;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
//@CrossOrigin(origins = "http://127.0.0.1:5000")
@RequestMapping("/api/v1/purchaseOrder")
public class PurchaseOrderController {
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

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @PostMapping("/add")
    public ResponseEntity<?> create(@RequestBody PurchaseOrderDto purchaseOrderDto) {

            int numberOfExistingPurchaseOrder = purchaseOrderRepository.countAllPurchaseOrders();
            int numberOfExistingUnit = unitRepository.countAllUnits();
            if(numberOfExistingPurchaseOrder == 0){
                numberOfExistingPurchaseOrder = 1;
            }else {
                numberOfExistingPurchaseOrder += 1;
            }
            if(numberOfExistingUnit == 0){
                numberOfExistingUnit = 1;
            }else {
                numberOfExistingUnit += 1;
            }
            IDGenerator POIDGenerator = new IDGenerator("PO", numberOfExistingPurchaseOrder);
            IDGenerator UNIDGenerator = new IDGenerator("UN", numberOfExistingUnit);
            // Create a new ProductStandard
            PurchaseOrder purchaseOrder = new PurchaseOrder(POIDGenerator);
            // Chuyển đổi định dạng từ String sang LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            LocalDateTime dateImport = LocalDateTime.parse(purchaseOrderDto.getDateImport(), formatter);
            purchaseOrder.setDateImport(dateImport);
            Optional<Warehouse> warehouseOptional;
            warehouseOptional = warehouseRepository.findById(purchaseOrderDto.getWarehouse_id());
            if (warehouseOptional.isEmpty()) {
                return new ResponseEntity<>("Invalid Warehouse ID", HttpStatus.BAD_REQUEST);
            }
            // Save the Warehouse to the Batch
            Warehouse warehouse = warehouseOptional.get();
            purchaseOrder.setWarehouse(warehouse);

            Optional<Supplier> supplierOptional;
            // save the supplier to the Batch{
                supplierOptional = supplierRepository.findById(purchaseOrderDto.getSupplier_id());
                if(supplierOptional.isEmpty()){
                    return new ResponseEntity<>("Invalid Supplier ID", HttpStatus.BAD_REQUEST);
                }


            Supplier supplier = supplierOptional.get();
            purchaseOrder.setSupplier(supplier);

            double totalPurchasePrice = 0;
            long quantityPurchaseOrder = 0;
            // Create and save the Units associated with the Batch
            List<Unit> units = new ArrayList<>();
            for (UnitDTO unitDTO : purchaseOrderDto.getUnits()) {
                Unit unit = new Unit(UNIDGenerator);
                String idPord = unitDTO.getProductId();


                unit.setProductId(idPord);
                unit.setUnitName(unitDTO.getUnitName());
                unit.setQuantity(unitDTO.getQuantity());
                unit.setQuantityImport(unitDTO.getQuantity());
                unit.setPurchasePrice(unitDTO.getUnitPrice()* unitDTO.getQuantity());
                totalPurchasePrice += unitDTO.getUnitPrice()* unitDTO.getQuantity();
                unit.setUnitPrice(unitDTO.getUnitPrice());
                unit.setPurchaseOrder(purchaseOrder); // Set the Batch for the Unit
                unit.setWarehouse(warehouse);
                units.add(unit);

                Optional<Product> product1 = productRepository.findById(idPord);
                if(product1.isPresent()){
                    Product product = product1.get();
                    product.setQuantityImported(product.getQuantityImported() + unitDTO.getQuantity());
                    product.setQuantity(product.getQuantity() + unitDTO.getQuantity());
                    product.setUnitPrice((unitDTO.getUnitPrice() + product.getUnitPrice())/2);
                    product.setSupplier(supplier);
                    quantityPurchaseOrder ++;
//                    product.setWarehouse(warehouse);
                    productRepository.save(product);
                    purchaseOrder.setProduct(product);
                }else {
                    return new ResponseEntity<>("Invalid Product ID", HttpStatus.BAD_REQUEST);
                }
            }
            double payableAmount = totalPurchasePrice - (totalPurchasePrice * purchaseOrderDto.getDiscount())/100;
            purchaseOrder.setUnits(units);
            purchaseOrder.setTotalPurchaseCost(totalPurchasePrice);
            purchaseOrder.setTotalQuantity(quantityPurchaseOrder);
            purchaseOrder.setDiscount(purchaseOrderDto.getDiscount());
            purchaseOrder.setPayableAmount(payableAmount);
            purchaseOrder.setPaidAmount(purchaseOrderDto.getPaidAmount());
            purchaseOrder.setNote(purchaseOrderDto.getNote());
            purchaseOrder.setStatus(purchaseOrderDto.getStatus());
            purchaseOrder.setOrderInitiator(purchaseOrderDto.getOrderInitiator());
            // Save the Batch with associated Units to the database
            PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
//            return new ResponseEntity<>(savedBatch, HttpStatus.CREATED);

    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> create(@PathVariable String id, @RequestBody PurchaseOrderDto purchaseOrderDto) {

        // Create a new ProductStandard
        Optional<PurchaseOrder> purchaseOrderOptional = purchaseOrderRepository.findById(id);
        if(purchaseOrderOptional.isEmpty()){
            return new ResponseEntity<>("Invalid PurchaseOrder", HttpStatus.BAD_REQUEST);
        }
        PurchaseOrder purchaseOrder = purchaseOrderOptional.get();
        // Chuyển đổi định dạng từ String sang LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        LocalDateTime dateImport = LocalDateTime.parse(purchaseOrderDto.getDateImport(), formatter);
        purchaseOrder.setDateImport(dateImport);
        Optional<Warehouse> warehouseOptional;
 // Save the Warehouse to the Batch

        double totalPurchasePrice = 0;
        long quantityPurchaseOrder = 0;
        // Create and save the Units associated with the Batch
        List<Unit> units = purchaseOrder.getUnits();
        for (UnitDTO unitDTO : purchaseOrderDto.getUnits()) {
            if(!IsExist(unitDTO, units)){
                continue;
            }
            Optional<Unit> unitOptional = unitRepository.findById(unitDTO.getIdUnit());
            if(unitOptional.isEmpty()){
                continue;
            }
            Unit unit = unitOptional.get();
            String idProd = unitDTO.getProductId();
            unit.setQuantity(unitDTO.getQuantity());
            unit.setQuantityImport(unitDTO.getQuantity());
            unit.setPurchasePrice(unitDTO.getUnitPrice()* unitDTO.getQuantity());
            totalPurchasePrice += unitDTO.getUnitPrice()* unitDTO.getQuantity();
            unit.setUnitPrice(unitDTO.getUnitPrice());
            unit.setPurchaseOrder(purchaseOrder); // Set the Batch for the Unit
            units.add(unit);

            Optional<Product> product1 = productRepository.findById(idProd);
            if(product1.isPresent()){
                Product product = product1.get();
                product.setQuantityImported(product.getQuantityImported() + unitDTO.getQuantity());
                product.setQuantity(product.getQuantity() + unitDTO.getQuantity());
                product.setUnitPrice((unitDTO.getUnitPrice() + product.getUnitPrice())/2);
                quantityPurchaseOrder ++;
//                    product.setWarehouse(warehouse);
                productRepository.save(product);
                purchaseOrder.setProduct(product);
            }else {
                return new ResponseEntity<>("Invalid Product ID", HttpStatus.BAD_REQUEST);
            }
        }
        double payableAmount = totalPurchasePrice - (totalPurchasePrice * purchaseOrderDto.getDiscount())/100;
        purchaseOrder.setUnits(units);
        purchaseOrder.setTotalPurchaseCost(totalPurchasePrice);
        purchaseOrder.setTotalQuantity(quantityPurchaseOrder);
        purchaseOrder.setDiscount(purchaseOrderDto.getDiscount());
        purchaseOrder.setPayableAmount(payableAmount);
        purchaseOrder.setPaidAmount(purchaseOrderDto.getPaidAmount());
        purchaseOrder.setNote(purchaseOrderDto.getNote());
        purchaseOrder.setStatus(purchaseOrderDto.getStatus());
        purchaseOrder.setOrderInitiator(purchaseOrderDto.getOrderInitiator());
        // Save the Batch with associated Units to the database
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
//            return new ResponseEntity<>(savedBatch, HttpStatus.CREATED);

    }

    boolean IsExist(UnitDTO unitDTO, List<Unit> units){
        for(Unit unit1 : units){
            if(unit1.getProductId().equals(unitDTO.getProductId())){
                return true;
            }
        }
        return false;
    }



    @GetMapping("/search")
    public ResponseEntity<?> searchPurchaseOrder(
            @RequestParam(required = false) String orderInitiator,
            @RequestParam(required = false) String nameSupplier,
            @RequestParam(required = false) String nameWarehouse,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (orderInitiator != null && !orderInitiator.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("orderInitiator"), "%" + orderInitiator + "%"));
            }

            if(nameSupplier != null && !nameSupplier.isEmpty()){
                Join<PurchaseOrder, Supplier> supplierJoin = root.join("supplier");
                predicates.add(criteriaBuilder.like(supplierJoin.get("name"), "%" + nameSupplier + "%"));
            }

            if (nameWarehouse != null && !nameWarehouse.isEmpty()) {
                Join<PurchaseOrder, Warehouse> warehouseJoin = root.join("warehouse"); // Liên kết với bảng Warehouse
                predicates.add(criteriaBuilder.like(warehouseJoin.get("name"), "%" + nameWarehouse + "%"));
            }

            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("dateImport").as(LocalDate.class), startDate, endDate.plusDays(1)));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateImport").as(LocalDate.class), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateImport").as(LocalDate.class), endDate.plusDays(1)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        if (purchaseOrders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Chuyển đổi danh sách sản phẩm tìm thấy thành danh sách DTO hoặc các đối tượng phù hợp khác
        List<PurchaseOrderDto> purchaseOrderDtos = new ArrayList<>();
        for (PurchaseOrder purchaseOrder : purchaseOrders) {
            // Tạo DTO từ productStandard và thêm vào danh sách
            PurchaseOrderDto purchaseOrderDto = new PurchaseOrderDto();
            // Map các trường từ productStandard sang productStandardDTO
            purchaseOrderDto.setIdPurchaseOrder(purchaseOrder.getIdPurchaseOrder());
            // Trong phương thức create
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            String formattedDate = purchaseOrder.getDateImport().format(formatter);
            purchaseOrderDto.setDateImport(formattedDate);
            purchaseOrderDto.setTotalQuantity(purchaseOrder.getTotalQuantity());
            purchaseOrderDto.setTotalPurchaseCost(purchaseOrder.getTotalPurchaseCost());
            purchaseOrderDto.setDiscount(purchaseOrder.getDiscount());
            purchaseOrderDto.setPayableAmount(purchaseOrder.getPayableAmount());
            purchaseOrderDto.setPaidAmount(purchaseOrder.getPaidAmount());
            purchaseOrderDto.setNote(purchaseOrder.getNote());
            purchaseOrderDto.setStatus(purchaseOrder.getStatus());
            purchaseOrderDto.setOrderInitiator(purchaseOrder.getOrderInitiator());
            purchaseOrderDto.setWarehouse_id(purchaseOrder.getWarehouse().getIdWarehouse());
            purchaseOrderDto.setNameWarehouse(purchaseOrder.getWarehouse().getName());
            purchaseOrderDto.setSupplier_id(purchaseOrder.getSupplier().getIdSupplier());
            purchaseOrderDto.setNameSupplier(purchaseOrder.getSupplier().getName());

            purchaseOrderDtos.add(purchaseOrderDto);
        }

        return ResponseEntity.ok(purchaseOrderDtos);
    }

    @GetMapping("productInPurchaseOrder")
    public ResponseEntity<?> getProduct(@RequestParam String idPurchaseOrder){
        Optional<PurchaseOrder> purchaseOrderOptional = purchaseOrderRepository.findById(idPurchaseOrder);
        if(purchaseOrderOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        PurchaseOrder purchaseOrder = purchaseOrderOptional.get();
        List<Unit> units = purchaseOrder.getUnits();
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

    @GetMapping("/getPurchasedOrder")
    public ResponseEntity<?> getPurchaseOrder(@RequestParam String idPurchaseOrder){
        Optional<PurchaseOrder> purchaseOrderOptional = purchaseOrderRepository.findById(idPurchaseOrder);
        if(purchaseOrderOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        PurchaseOrder purchaseOrder = purchaseOrderOptional.get();
        PurchaseOrderDto purchaseOrderDto = new PurchaseOrderDto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        String formattedDate = purchaseOrder.getDateImport().format(formatter);
        purchaseOrderDto.setDateImport(formattedDate);
        purchaseOrderDto.setNameSupplier(purchaseOrder.getSupplier().getName());
        purchaseOrderDto.setNameWarehouse(purchaseOrder.getWarehouse().getName());
        purchaseOrderDto.setStatus(purchaseOrder.getStatus());
        purchaseOrderDto.setTotalPurchaseCost(purchaseOrder.getTotalPurchaseCost());
        purchaseOrderDto.setDiscount(purchaseOrder.getDiscount());
        purchaseOrderDto.setPayableAmount(purchaseOrder.getPayableAmount());
        purchaseOrderDto.setPaidAmount(purchaseOrder.getPaidAmount());
        purchaseOrderDto.setDebt(purchaseOrder.getPaidAmount() - purchaseOrder.getPayableAmount());
        purchaseOrderDto.setNote(purchaseOrder.getNote());
        List<Unit> units = purchaseOrder.getUnits();
        List<UnitDTO> unitDTOS = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        for (Unit unit : units){
            Optional<Product> productOptional = productRepository.findById(unit.getProductId());
            if(productOptional.isEmpty()){
                continue;
            }
            Product product = productOptional.get();
            UnitDTO unitDTO = new UnitDTO();
            unitDTO.setIdUnit(unit.getIdUnit());
            unitDTO.setProductId(unit.getProductId());
            unitDTO.setUnitPrice(unit.getUnitPrice());
            unitDTO.setQuantity(unit.getQuantity());
            unitDTO.setUnitName(unit.getUnitName());
            unitDTO.setPurchasePrice(unit.getPurchasePrice());
            unitDTO.setProductName(product.getProductName());
            unitDTOS.add(unitDTO);
        }
        purchaseOrderDto.setUnits(unitDTOS);

        return new ResponseEntity<>(purchaseOrderDto, HttpStatus.OK);
    }

    @GetMapping("/{idProduct}/purchaseOrder")
    public ResponseEntity<List<PurchaseOrderDto>> getPurchaseOrderByProductId(@PathVariable String idProduct) {
        Optional<Product> productOptional = productRepository.findById(idProduct);
        if (productOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOptional.get();
        List<PurchaseOrderDto> purchaseOrderDtos = product.getPurchaseOrders().stream()
                .map(purchaseOrder -> {
                    PurchaseOrderDto purchaseOrderDto = new PurchaseOrderDto();
                    purchaseOrderDto.setIdPurchaseOrder(purchaseOrder.getIdPurchaseOrder());
                    // Trong phương thức create
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
                    String formattedDate = purchaseOrder.getDateImport().format(formatter);
                    purchaseOrderDto.setDateImport(formattedDate);
                    purchaseOrderDto.setTotalQuantity(purchaseOrder.getTotalQuantity());
                    purchaseOrderDto.setTotalPurchaseCost(purchaseOrder.getTotalPurchaseCost());
                    purchaseOrderDto.setDiscount(purchaseOrder.getDiscount());
                    purchaseOrderDto.setPayableAmount(purchaseOrder.getPayableAmount());
                    purchaseOrderDto.setPaidAmount(purchaseOrder.getPaidAmount());
                    purchaseOrderDto.setNote(purchaseOrder.getNote());
                    purchaseOrderDto.setStatus(purchaseOrder.getStatus());
                    purchaseOrderDto.setOrderInitiator(purchaseOrder.getOrderInitiator());
                    purchaseOrderDto.setWarehouse_id(purchaseOrder.getWarehouse().getIdWarehouse());
                    purchaseOrderDto.setNameWarehouse(purchaseOrder.getWarehouse().getName());
                    purchaseOrderDto.setSupplier_id(purchaseOrder.getSupplier().getIdSupplier());
                    purchaseOrderDto.setNameSupplier(purchaseOrder.getSupplier().getName());

                    List<UnitDTO> unitDTOs = purchaseOrderDto.getUnits().stream()
                            .map(unit -> {
                                UnitDTO unitDTO = new UnitDTO();
                                unitDTO.setProductId(product.getIdProd());
                                unitDTO.setUnitName(unit.getUnitName());
                                unitDTO.setQuantity(unit.getQuantity());
                                unitDTO.setUnitPrice(unit.getUnitPrice());
                                return unitDTO;
                            })
                            .collect(Collectors.toList());

                    purchaseOrderDto.setUnits(unitDTOs);
                    return purchaseOrderDto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(purchaseOrderDtos);
    }


//    @PutMapping("/{idProductStandard}/units/{idUnit}")
//    public ResponseEntity<?> updateUnitSoldQuantity(@PathVariable Long idProductStandard,
//                                                    @PathVariable Long idUnit,
//                                                    @RequestParam int soldQuantity) {
//        Optional<ProductStandard> productStandardOptional = productStandardRepository.findById(idProductStandard);
//        if (productStandardOptional.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        ProductStandard productStandard = productStandardOptional.get();
//        Unit unit = productStandard.getUnits().stream()
//                .filter(u -> u.getIdUnit().equals(idUnit))
//                .findFirst()
//                .orElse(null);
//
//        if (unit == null) {
//            return ResponseEntity.notFound().build();
//        }
//
//        if (unit.getQuantity() < soldQuantity) {
//            // If the requested sold quantity exceeds the available quantity, return a custom error response.
//            String errorMessage = "The requested sold quantity (" + soldQuantity + ") exceeds the available quantity (" + unit.getQuantity() + ") in the unit.";
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject("error", errorMessage, null));
//        }
//
//        unit.setSoldQuantity(unit.getQuantity() + soldQuantity);
//        unit.setQuantity(unit.getQuantity()-soldQuantity);
//        ProductStandard updatedBatch = productStandardRepository.save(productStandard);
//
//        // Return the custom JSON response with the updated product.
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(new ResponseObject("success", "Update Product successfully", updatedBatch));
//    }

}
