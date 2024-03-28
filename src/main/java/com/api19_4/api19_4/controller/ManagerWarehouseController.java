package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto.CheckWarehouseDto;
import com.api19_4.api19_4.dto.ProductCheckDto;
import com.api19_4.api19_4.models.*;
import com.api19_4.api19_4.repositories.*;
import com.api19_4.api19_4.services.IStorageService;
import com.api19_4.api19_4.services.ProductServices;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//@CrossOrigin(origins = "http://127.0.0.1:5000")
//@CrossOrigin(origins = "http://192.168.60.5:8080")
@RestController
@RequestMapping(path = "/api/v1/ManagerWH")

public class ManagerWarehouseController {
    @Autowired
    private IStorageService storageService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductServices productServices;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired
    private ProductBillRepository productBillRepository;
    @Autowired
    private ProductCheckRepository productCheckRepository;
    @Autowired
    private  BillRepository billRepository;
    @Autowired
    private ModelMapper modelMapper;

//    ===============================Tổng vốn tồn kho==========================
    @GetMapping("/totalInventoryCost")
    public ResponseEntity<?> getTotalInventoryCost(){
        List<Unit> unitList = unitRepository.findAll();
        if(unitList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Empty Unit list", "")
            );
        }
        double costOfGoodsSold = 0;
        for(Unit unit : unitList){
            costOfGoodsSold += unit.getQuantity() * unit.getUnitPrice();
        }
        return ResponseEntity.ok()
                .body(new ResponseObject("ok", "Total inventory quantity",costOfGoodsSold));
    }


//    =====================================Dosnh thu===============================
@GetMapping("/totalRevenue")
public ResponseEntity<?> getTotalRevenue(){
    List<Bill> billList = billRepository.findAll();
    if(billList.isEmpty()){
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                new ResponseObject("failed", "Empty Bill list", "")
        );
    }
    double costOfGoodsSold = 0;
    for(Bill bill : billList){
        costOfGoodsSold += bill.getTotalPayment();
    }
    return ResponseEntity.ok()
            .body(new ResponseObject("ok", "Total inventory quantity",costOfGoodsSold));
}

    //    ===============================Tổng giá trị vốn==========================
    @GetMapping("/totalInventoryValue")
    public ResponseEntity<?> getTotalInventoryCostByWH(){
        List<Unit> unitList = unitRepository.findAll();
        if(unitList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Empty Unit list", "")
            );
        }
        double costOfGoodsSold = 0;
        for(Unit unit : unitList){
            Optional<Product> productOptional = productRepository.findById(unit.getProductId());
            if(productOptional.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                        new ResponseObject("failed", "Empty Product", unit.getProductId())
                );
            }
            Product product = productOptional.get();
            costOfGoodsSold += unit.getQuantity() * product.getRetailPrice();
        }
        return ResponseEntity.ok()
                .body(new ResponseObject("ok", "Total inventory quantity",costOfGoodsSold));
    }



//    ========================kiểm kho theo từng kho========================================
@GetMapping("/inventoryCheck")
public ResponseEntity<?> getInventoryCheck() {
    List<CheckWarehouseDto> checkWarehouseDtos = new ArrayList<>();
    List<PurchaseOrder> purchaseOrderList = purchaseOrderRepository.findAll();

    if (purchaseOrderList.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                new ResponseObject("failed", "Empty PurchaseOrder list", "")
        );
    }

    for (PurchaseOrder purchaseOrder : purchaseOrderList) {
        boolean warehouseExists = false;
        for (CheckWarehouseDto checkWarehouseDto : checkWarehouseDtos) {
            if (Objects.equals(checkWarehouseDto.getIdWarehouse(), purchaseOrder.getWarehouse().getIdWarehouse())) {
                // Warehouse already exists in the list, update the totals
                checkWarehouseDto.setTotalInventoryCost(checkWarehouseDto.getTotalInventoryCost() +
                        calculateTotalInventoryCost(purchaseOrder));
                checkWarehouseDto.setTotalInventoryValue(checkWarehouseDto.getTotalInventoryValue() +
                        calculateTotalInventoryValue(purchaseOrder));
                checkWarehouseDto.setTotalQuantityInventory(checkWarehouseDto.getTotalQuantityInventory() +
                        calculateTotalQuantityInventory(purchaseOrder));
                warehouseExists = true;
                break;
            }
        }

        if (!warehouseExists) {
            // Warehouse does not exist in the list, create a new CheckWarehouseDto
            CheckWarehouseDto checkWarehouseDto = new CheckWarehouseDto();
            checkWarehouseDto.setIdWarehouse(purchaseOrder.getWarehouse().getIdWarehouse());
            checkWarehouseDto.setName(purchaseOrder.getWarehouse().getName());
            checkWarehouseDto.setTotalInventoryCost(calculateTotalInventoryCost(purchaseOrder));
            checkWarehouseDto.setTotalInventoryValue(calculateTotalInventoryValue(purchaseOrder));
            checkWarehouseDto.setTotalQuantityInventory(calculateTotalQuantityInventory(purchaseOrder));
            checkWarehouseDtos.add(checkWarehouseDto);
        }
    }
    return ResponseEntity.ok()
            .body(new ResponseObject("ok", "Total inventory quantity",checkWarehouseDtos));

}

    // Helper methods to calculate totals
    private double calculateTotalInventoryCost(PurchaseOrder purchaseOrder) {
        double totalCost = 0;
        for (Unit unit : purchaseOrder.getUnits()) {
            totalCost += unit.getQuantity() * unit.getUnitPrice();
        }
        return totalCost;
    }

    private double calculateTotalInventoryValue(PurchaseOrder purchaseOrder) {
        double totalValue = 0;
        for (Unit unit : purchaseOrder.getUnits()) {
            Optional<Product> productOptional = productRepository.findById(unit.getProductId());
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                totalValue += unit.getQuantity() * product.getRetailPrice();
            }
        }
        return totalValue;
    }

    private int calculateTotalQuantityInventory(PurchaseOrder purchaseOrder) {
        int totalQuantity = 0;
        for (Unit unit : purchaseOrder.getUnits()) {
            totalQuantity += unit.getQuantity();
        }
        return totalQuantity;
    }

    @GetMapping("/inventoryCheckSearchNameWH")
    public ResponseEntity<?> getInventoryCheck(@RequestParam("warehouseName") String warehouseName) {
        List<CheckWarehouseDto> checkWarehouseDtos = new ArrayList<>();
        List<PurchaseOrder> purchaseOrderList = purchaseOrderRepository.findAll();

        if (purchaseOrderList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Empty PurchaseOrder list", "")
            );
        }

        for (PurchaseOrder purchaseOrder : purchaseOrderList) {
            if (purchaseOrder.getWarehouse().getName().equalsIgnoreCase(warehouseName)) {
                boolean warehouseExists = false;
                for (CheckWarehouseDto checkWarehouseDto : checkWarehouseDtos) {
                    if (Objects.equals(checkWarehouseDto.getIdWarehouse(), purchaseOrder.getWarehouse().getIdWarehouse())) {
                        // Warehouse already exists in the list, update the totals
                        checkWarehouseDto.setTotalInventoryCost(checkWarehouseDto.getTotalInventoryCost() +
                                calculateTotalInventoryCost(purchaseOrder));
                        checkWarehouseDto.setTotalInventoryValue(checkWarehouseDto.getTotalInventoryValue() +
                                calculateTotalInventoryValue(purchaseOrder));
                        checkWarehouseDto.setTotalQuantityInventory(checkWarehouseDto.getTotalQuantityInventory() +
                                calculateTotalQuantityInventory(purchaseOrder));
                        warehouseExists = true;
                        break;
                    }
                }

                if (!warehouseExists) {
                    // Warehouse does not exist in the list, create a new CheckWarehouseDto
                    CheckWarehouseDto checkWarehouseDto = new CheckWarehouseDto();
                    checkWarehouseDto.setIdWarehouse(purchaseOrder.getWarehouse().getIdWarehouse());
                    checkWarehouseDto.setName(purchaseOrder.getWarehouse().getName());
                    checkWarehouseDto.setTotalInventoryCost(calculateTotalInventoryCost(purchaseOrder));
                    checkWarehouseDto.setTotalInventoryValue(calculateTotalInventoryValue(purchaseOrder));
                    checkWarehouseDto.setTotalQuantityInventory(calculateTotalQuantityInventory(purchaseOrder));
                    checkWarehouseDtos.add(checkWarehouseDto);
                }
            }
        }

        if (checkWarehouseDtos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject("failed", "Warehouse not found", warehouseName)
            );
        }

        return ResponseEntity.ok()
                .body(new ResponseObject("ok", "Total inventory quantity for warehouse " + warehouseName, checkWarehouseDtos));
    }

    @GetMapping("/inventoryProduct")
    public ResponseEntity<?> getInventoryProduct(@RequestParam("productName") String productName) {
        Optional<Product> productOptional = productRepository.findByProductNameIgnoreCase(productName);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject("failed", "Product not found", "")
            );
        }
        Product product = productOptional.get();
        List<Unit> unitList = unitRepository.findByProductId(product.getIdProd());
        if (unitList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Empty Unit list", "")
            );
        }
        List<ProductCheckDto> productCheckDtoList = new ArrayList<>();
        for (Unit unit : unitList) {
            ProductCheckDto productCheckDto = new ProductCheckDto();
            if(existProductCheck(unit.getProductId(), unit.getWarehouse().getName(), productCheckDtoList) == false || productCheckDtoList.isEmpty()){
                Optional<Product> productOptional1 = productRepository.findById(unit.getProductId());
                if(productOptional1.isEmpty()){
                    continue;
                }
                Product product1 = productOptional1.get();
                productCheckDto.setIdProd(product1.getIdProd());
                productCheckDto.setProductName(product1.getProductName());
                productCheckDto.setUnitName(product1.getUnitName());
                productCheckDto.setQuantityInventory(unit.getQuantity());
                productCheckDto.setNameWarehouse(unit.getWarehouse().getName());
                productCheckDto.setUnitPrice(unit.getUnitPrice());
                productCheckDtoList.add(productCheckDto);
            }
            else {
                for(ProductCheckDto productCheckDto1 : productCheckDtoList){
                    if(productCheckDto1.getNameWarehouse().equals(unit.getWarehouse().getName())){
                        Optional<Product> productOptional1 = productRepository.findById(unit.getProductId());
                        if(productOptional1.isEmpty()){
                            continue;
                        }
                        Product product1 = productOptional1.get();
                        productCheckDto1.setQuantityInventory(unit.getQuantity() + productCheckDto1.getQuantityInventory());
                    }
                }
            }
        }
        return ResponseEntity.ok().body(new ResponseObject("success", "Product inventory information", productCheckDtoList));

    }

    private boolean existProductCheck(String idProd, String nameWH, List<ProductCheckDto> productCheckDtoList){
        for(ProductCheckDto productCheckDto : productCheckDtoList){
            if(productCheckDto.getIdProd().equals(idProd) && productCheckDto.getNameWarehouse().equals(nameWH)){
                return true;
            }
        }
        return false;
    }
}
