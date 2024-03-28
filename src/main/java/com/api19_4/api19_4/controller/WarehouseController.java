package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto.*;
import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.ProductStandard;
import com.api19_4.api19_4.models.Supplier;
import com.api19_4.api19_4.models.Warehouse;
import com.api19_4.api19_4.repositories.WarehouseRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
//@CrossOrigin(origins = "http://127.0.0.1:5000")
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private ModelMapper modelMapper;
//==================tạo kho moi===============================
    @PostMapping("/warehouses")
    public Warehouse createWarehouse(@RequestBody WarehouseDTO warehouseDTO) {
        // Set batches to null to ensure batches are not sent in the request
        int numberOfExistingWarehouse = warehouseRepository.countAllWareHouses();
        if(numberOfExistingWarehouse == 0){
            numberOfExistingWarehouse = 1;
        }else {
            numberOfExistingWarehouse += 1;
        }
        IDGenerator idGenerator = new IDGenerator("WH", numberOfExistingWarehouse);
        Warehouse warehouse = new Warehouse(idGenerator);
        warehouse.setName(warehouseDTO.getName());
        warehouse.setAddress(warehouseDTO.getAddress());
        warehouse.setInformation(warehouseDTO.getInformation());
        warehouse.setBatches(null);

       return warehouseRepository.save(warehouse);
       // return new ResponseEntity<>(createdWarehouse, HttpStatus.CREATED);
    }

    @PostMapping("/upload-excel")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Warehouse> createWarehouseFromExcel(@RequestParam("excelFile") MultipartFile excelFile) throws IOException {
        // Đọc dữ liệu từ tệp Excel vào danh sách sản phẩm
        List<Warehouse> warehouseList = new ArrayList<>();
        int numberOfExistingWarehouse = warehouseRepository.countAllWareHouses();
        if(numberOfExistingWarehouse == 0){
            numberOfExistingWarehouse = 1;
        }else {
            numberOfExistingWarehouse += 1;
        }
        IDGenerator idGenerator = new IDGenerator("WH", numberOfExistingWarehouse);

        try (InputStream is = excelFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0); // Giả sử dữ liệu nằm trên sheet đầu tiên

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Bỏ qua hàng tiêu đề (nếu có)
                }
                Warehouse warehouse = new Warehouse(idGenerator);
                warehouse.setName(row.getCell(0).getStringCellValue());
                warehouse.setAddress(row.getCell(1).getStringCellValue());
                warehouse.setInformation(row.getCell(2).getStringCellValue());

                // Lưu sản phẩm vào danh sách
                warehouseList.add(warehouse);
            }
            return warehouseRepository.saveAll(warehouseList);
        }
    }


    @GetMapping("/searchWareHouse")
    public ResponseEntity<List<WarehouseDTO>> searchWarehouse(
            @RequestParam(required = false) String name
    ){
        List<Warehouse> warehouses;
        if(name != null && !name.isEmpty()){
            warehouses = warehouseRepository.findByNameContainingIgnoreCase(name);
        }else{
            warehouses = warehouseRepository.findAll();
        }
        List<WarehouseDTO> warehouseDTOS = warehouses.stream()
                .map(warehouse -> modelMapper.map(warehouse, WarehouseDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(warehouseDTOS, HttpStatus.OK);
    }

//    @GetMapping("/{idWarehouse}")
//    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable Long idWarehouse) {
//        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(idWarehouse);
//        if (warehouseOptional.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Warehouse warehouse = warehouseOptional.get();
//        return ResponseEntity.ok(warehouse);
//    }



//    @GetMapping("/{idWarehouse}/products")
//    public ResponseEntity<List<ProductDto>> getProductsByWarehouseId(@PathVariable Long idWarehouse) {
//        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(idWarehouse);
//        if (warehouseOptional.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Warehouse warehouse = warehouseOptional.get();
//        List<ProductDto> productDTOs = new ArrayList<>();
//        if (warehouse.getProducts() != null) {
//            productDTOs = warehouse.getProducts().stream()
//                    .map(product -> modelMapper.map(product, ProductDto.class))
//                    .collect(Collectors.toList());
//        }
//
//        return ResponseEntity.ok(productDTOs);
//    }


    @GetMapping("/{idWarehouse}/batches")
    public ResponseEntity<List<BatchDTO>> getAllBatchesInWarehouse(@PathVariable String idWarehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(idWarehouse);
        if (warehouseOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Warehouse warehouse = warehouseOptional.get();

        List<BatchDTO> batchDTOs = warehouse.getBatches().stream()
                .map(batch -> {
                    BatchDTO batchDTO = new BatchDTO();
                    batchDTO.setName(batch.getName());
                    batchDTO.setDateImport(batch.getDateImport());
                    batchDTO.setManufacturingDate(batch.getManufacturingDate());
                    batchDTO.setExpirationDate(batch.getExpirationDate());
                    batchDTO.setWarehouse_id(warehouse.getIdWarehouse());
                    batchDTO.setSupplier_id(batch.getSupplier().getIdSupplier());

                    List<UnitDTO> unitDTOs = batch.getUnits().stream()
                            .map(unit -> {
                                UnitDTO unitDTO = modelMapper.map(unit, UnitDTO.class);
                                unitDTO.setStockQuantity(unit.getStockQuantity());

                                return unitDTO;
                            })
                            .collect(Collectors.toList());

                    batchDTO.setUnits(unitDTOs);
                    return batchDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(batchDTOs);
    }

    @GetMapping("/{idWarehouse}/productStandardById")
    public ResponseEntity<List<ProductStandardDTO>> getAllProductStandardInWarehouse(@PathVariable String idWarehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(idWarehouse);
        if (warehouseOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Warehouse warehouse = warehouseOptional.get();

        List<ProductStandardDTO> productStandardDTOS = warehouse.getProductStandards().stream()
                .map(productStandard -> {
                    ProductStandardDTO productStandardDTO = new ProductStandardDTO();
                    productStandardDTO.setName(productStandard.getName());
                    productStandardDTO.setDateImport(productStandard.getDateImport());
                    productStandardDTO.setWarehouse_id(warehouse.getIdWarehouse());
                    productStandardDTO.setSupplier_id(productStandard.getSupplier().getIdSupplier());

                    List<UnitDTO> unitDTOs = productStandard.getUnits().stream()
                            .map(unit -> {
                                UnitDTO unitDTO = modelMapper.map(unit, UnitDTO.class);
                                unitDTO.setStockQuantity(unit.getStockQuantity());

                                return unitDTO;
                            })
                            .collect(Collectors.toList());

                    productStandardDTO.setUnits(unitDTOs);
                    return productStandardDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(productStandardDTOS);
    }

    @GetMapping("/{nameWarehouse}/productStandardByName")
    public ResponseEntity<List<ProductStandardDTO>> getAllProductStandardInWarehouseByName(@PathVariable String nameWarehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findByName(nameWarehouse);
        if (warehouseOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Warehouse warehouse = warehouseOptional.get();

        List<ProductStandardDTO> productStandardDTOS = warehouse.getProductStandards().stream()
                .map(productStandard -> {
                    ProductStandardDTO productStandardDTO = new ProductStandardDTO();
                    productStandardDTO.setName(productStandard.getName());
                    productStandardDTO.setDateImport(productStandard.getDateImport());
                    productStandardDTO.setWarehouse_id(warehouse.getIdWarehouse());
                    productStandardDTO.setSupplier_id(productStandard.getSupplier().getIdSupplier());

                    List<UnitDTO> unitDTOs = productStandard.getUnits().stream()
                            .map(unit -> {
                                UnitDTO unitDTO = modelMapper.map(unit, UnitDTO.class);
                                unitDTO.setStockQuantity(unit.getStockQuantity());

                                return unitDTO;
                            })
                            .collect(Collectors.toList());

                    productStandardDTO.setUnits(unitDTOs);
                    return productStandardDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(productStandardDTOS);
    }

    @GetMapping("/{idWarehouse}/allProductsById")
    public ResponseEntity<List<ProductDto>> getAllProductsInWarehouse(@PathVariable String idWarehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(idWarehouse);
        if (warehouseOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Warehouse warehouse = warehouseOptional.get();
        List<ProductDto> allProducts = new ArrayList<>();

        // Add products from normal batches
        if (warehouse.getProductStandards() != null) {
            List<ProductDto> productStandardProducts = warehouse.getProductStandards().stream()
                    .flatMap(productStandard -> productStandard.getUnits().stream())
                    .map(unit -> modelMapper.map(unit.getProductStandard().getProduct(), ProductDto.class))
                    .distinct()
                    .collect(Collectors.toList());
            allProducts.addAll(productStandardProducts);
        }

        // Add products from batches
        if (warehouse.getBatches() != null) {
            List<ProductDto> batchProducts = warehouse.getBatches().stream()
                    .flatMap(batch -> batch.getUnits().stream())
                    .map(unit -> modelMapper.map(unit.getBatch().getProduct(), ProductDto.class))
                    .distinct()
                    .collect(Collectors.toList());
            allProducts.addAll(batchProducts);
        }

        return ResponseEntity.ok(allProducts);
    }
    @GetMapping("/{nameWarehouse}/allProductsByName")
    public ResponseEntity<List<ProductDto>> getAllProductsInWarehouseByName(@PathVariable String nameWarehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findByName(nameWarehouse);
        if (warehouseOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Warehouse warehouse = warehouseOptional.get();
        List<ProductDto> allProducts = new ArrayList<>();

        // Add products from normal batches
        if (warehouse.getProductStandards() != null) {
            List<ProductDto> productStandardProducts = warehouse.getProductStandards().stream()
                    .flatMap(productStandard -> productStandard.getUnits().stream())
                    .map(unit -> modelMapper.map(unit.getProductStandard().getProduct(), ProductDto.class))
                    .distinct()
                    .collect(Collectors.toList());
            allProducts.addAll(productStandardProducts);
        }

        // Add products from batches
        if (warehouse.getBatches() != null) {
            List<ProductDto> batchProducts = warehouse.getBatches().stream()
                    .flatMap(batch -> batch.getUnits().stream())
                    .map(unit -> modelMapper.map(unit.getBatch().getProduct(), ProductDto.class))
                    .distinct()
                    .collect(Collectors.toList());
            allProducts.addAll(batchProducts);
        }

        return ResponseEntity.ok(allProducts);
    }

}
