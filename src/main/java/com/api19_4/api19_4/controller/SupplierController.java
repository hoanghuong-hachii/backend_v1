package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto.*;
import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.*;
import com.api19_4.api19_4.repositories.ProductRepository;
import com.api19_4.api19_4.repositories.SupplierRepository;
import com.api19_4.api19_4.repositories.UnitRepository;
import com.api19_4.api19_4.repositories.WarehouseRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
@RequestMapping("/api/v1/supplier")
public class SupplierController {
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private ModelMapper modelMapper;
    @PostMapping("/add")
    public Supplier createWarehouse(@RequestBody SupplierDTO supplierDTO) {
        // Set batches to null to ensure batches are not sent in the request
        int numberOfExistingSupplier = supplierRepository.countAllSuppliers();
        if(numberOfExistingSupplier == 0){
            numberOfExistingSupplier = 1;
        }else {
            numberOfExistingSupplier += 1;
        }
        IDGenerator idGenerator = new IDGenerator("SP", numberOfExistingSupplier);
        Supplier supplier = new Supplier(idGenerator);
        supplier.setName(supplierDTO.getName());
        supplier.setAddress(supplierDTO.getAddress());
        supplier.setNumberPhone(supplierDTO.getNumberPhone());
        return supplierRepository.save(supplier);
        // return new ResponseEntity<>(createdWarehouse, HttpStatus.CREATED);
    }

    @PostMapping("/upload-excel")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Supplier> createSupplierFromExcel(@RequestParam("excelFile") MultipartFile excelFile) throws IOException {
        // Đọc dữ liệu từ tệp Excel vào danh sách sản phẩm
        List<Supplier> supplierList = new ArrayList<>();
        int numberOfExistingSupplier = supplierRepository.countAllSuppliers();
        if(numberOfExistingSupplier == 0){
            numberOfExistingSupplier = 1;
        }else{
            numberOfExistingSupplier += 1;
        }
        IDGenerator idGenerator = new IDGenerator("SP", numberOfExistingSupplier);
        try (InputStream is = excelFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0); // Giả sử dữ liệu nằm trên sheet đầu tiên

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Bỏ qua hàng tiêu đề (nếu có)
                }

                Supplier supplier = new Supplier(idGenerator);
                supplier.setName(row.getCell(0).getStringCellValue());
                supplier.setNumberPhone(row.getCell(1).getStringCellValue());
                supplier.setAddress(row.getCell(2).getStringCellValue());

                // Lưu sản phẩm vào danh sách
                supplierList.add(supplier);
            }

            return supplierRepository.saveAll(supplierList);
        }
    }


    @GetMapping("/{idSupplier}/products")
    public ResponseEntity<List<ProductDto>> getProductsBySupplierId(@PathVariable String idSupplier) {
        Optional<Supplier> supplierOptional = supplierRepository.findById(idSupplier);
        if (supplierOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Supplier supplier = supplierOptional.get();
        List<ProductDto> productDTOs = new ArrayList<>();
        if (supplier.getProducts() != null) {
            productDTOs = supplier.getProducts().stream()
                    .map(product -> modelMapper.map(product, ProductDto.class))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("searchSupplier")
    public ResponseEntity<List<SupplierDTO>> searchSupplier(
            @RequestParam(required = false) String name
    ){
        List<Supplier> suppliers;
        if(name != null && !name.isEmpty()){
            suppliers = supplierRepository.findByName(name);
        }else{
            suppliers = supplierRepository.findAll();
        }
        List<SupplierDTO> supplierDTOS = suppliers.stream()
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(supplierDTOS, HttpStatus.OK);
    }

}
