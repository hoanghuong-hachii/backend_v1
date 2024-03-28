package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto.*;
import com.api19_4.api19_4.enums.SortOrderEnum;
import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.*;
import com.api19_4.api19_4.repositories.ProductRepository;
import com.api19_4.api19_4.repositories.SupplierRepository;
import com.api19_4.api19_4.repositories.UnitRepository;
import com.api19_4.api19_4.repositories.WarehouseRepository;
import com.api19_4.api19_4.services.IStorageService;
import com.api19_4.api19_4.services.ProductServices;
import com.api19_4.api19_4.util.SearchUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
//@SecurityRequirement(name = "basicAuth")
@CrossOrigin(origins = "http://127.0.0.1:5000")
//@CrossOrigin(origins = "http://192.168.60.5:8080")
@RestController
@RequestMapping(path = "/api/v1/Products")

public class ProductController {
    private static final Path CURRENT_FOLDER = Paths.get(System.getProperty("user.dir"));
    @Autowired
    private IStorageService storageService;
    @Autowired
    private ProductRepository repository;
    @Autowired
    private ProductServices productServices;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ModelMapper modelMapper;

    //==================================== Thêm sản phẩm  ===========================

    @PostMapping("/upload-excel")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Product> createProductsFromExcel(@RequestParam("excelFile") MultipartFile excelFile) throws IOException {
        // Đọc dữ liệu từ tệp Excel vào danh sách sản phẩm
        List<Product> productList = new ArrayList<>();
        int numberOfExistingProducts = repository.countAllProducts();
        int numberOfExistingUnits = unitRepository.countAllUnits();
        if(numberOfExistingProducts == 0){
            numberOfExistingProducts = 1;
        }else {
            // Tìm ShoppingCart cuối cùng trong cơ sở dữ liệu
            Product lastProduct = repository.findTopByOrderByIdProdDesc();

            // Lấy số từ ID của ShoppingCart cuối cùng
            String lastProductId = lastProduct != null ? lastProduct.getIdProd() : "HH0";
            int lastNumber = Integer.parseInt(lastProductId.replace("HH", ""));

            // Tăng số lượng lên 1 để tạo ID mới
            int newNumber = lastNumber + 1;
            numberOfExistingProducts = newNumber;
        }

        try (InputStream is = excelFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0); // Giả sử dữ liệu nằm trên sheet đầu tiên
            IDGenerator hhIDGenerator = new IDGenerator("HH", numberOfExistingProducts);
            IDGenerator idGenerator = new IDGenerator("UN", numberOfExistingUnits);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Bỏ qua hàng tiêu đề (nếu có)
                }
                // Tạo IDGenerator cho sản phẩm HH

                Product product = new Product(hhIDGenerator);
                product.setBrand(row.getCell(0).getStringCellValue());
                product.setOrigin(row.getCell(1).getStringCellValue());
                product.setDetail(row.getCell(2).getStringCellValue());
                product.setProductName(row.getCell(3).getStringCellValue());
                product.setRetailPrice((float) row.getCell(4).getNumericCellValue());
                product.setUnitName(row.getCell(5).getStringCellValue());
                product.setCategoryName(row.getCell(6).getStringCellValue());
                product.setCoupons((int) row.getCell(7).getNumericCellValue());
                String name = row.getCell(8).getStringCellValue();
                Supplier supplier = supplierRepository.findByNameSupplier(name);
                product.setSupplier(supplier);

                String avatarImageUrl = row.getCell(9).getStringCellValue(); // Ở đây giả sử cột URL ảnh avatar là cột thứ 10
                String qrImageUrl = row.getCell(10).getStringCellValue(); // Ở đây giả sử cột URL ảnh QR là cột thứ 11

                Path staticPath = Paths.get("static");
                Path imagePath = Paths.get("images");
                if (!Files.exists(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath))) {
                    Files.createDirectories(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath));
                }

                String Image = imagePath.resolve(product.getIdProd() + "_avatar.jpg").toString();
                String ImageQR = imagePath.resolve(product.getIdProd() + "_qr.jpg").toString();

                // Đường dẫn lưu ảnh vào thư mục local
                String localAvatarImagePath = staticPath.resolve(imagePath).resolve(product.getIdProd() + "_avatar.jpg").toString();
                String localQrImagePath = staticPath.resolve(imagePath).resolve(product.getIdProd() + "_qr.jpg").toString();

                // Lưu ảnh vào thư mục local
                // Note: Hãy chắc chắn rằng bạn đã đảm bảo giữ tên file độc nhất cho từng sản phẩm
                // Để tránh ghi đè lên ảnh của sản phẩm khác cùng tên
                Path localAvatarImageFile = CURRENT_FOLDER.resolve(localAvatarImagePath);
                try (InputStream avatarImageInputStream = new URL(avatarImageUrl).openStream()) {
                    Files.copy(avatarImageInputStream, localAvatarImageFile, StandardCopyOption.REPLACE_EXISTING);
                }

                Path localQrImageFile = CURRENT_FOLDER.resolve(localQrImagePath);
                try (InputStream qrImageInputStream = new URL(qrImageUrl).openStream()) {
                    Files.copy(qrImageInputStream, localQrImageFile, StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageAvatar(Image);
                product.setImageQR(ImageQR);

                // Lưu sản phẩm vào danh sách
                productList.add(product);


            }

            return repository.saveAll(productList);
        }
    }

    @PostMapping("/addProduct")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestParam("imageAvatar") MultipartFile image,

                                    @RequestParam("brand") String brand,
                                    @RequestParam("origin") String origin,
                                    @RequestParam("detail") String detail,
                                    @RequestParam("productName") String productName,
                                    @RequestParam("unitName") String unitName,
                                    @RequestParam("categoryName") String categoryName,
                                    @RequestParam("nameSupplier") String nameSupplier,
                                    @RequestParam("retailPrice") double retailPrice,
                                    @RequestParam("coupons") int coupons)throws IOException {
        int numberOfExistingProducts = repository.countAllProducts();
        if(numberOfExistingProducts == 0){
            numberOfExistingProducts = 1;
        }else {
            // Tìm ShoppingCart cuối cùng trong cơ sở dữ liệu
            Product lastProduct = repository.findTopByOrderByIdProdDesc();

            // Lấy số từ ID của ShoppingCart cuối cùng
            String lastProductId = lastProduct != null ? lastProduct.getIdProd() : "HH0";
            int lastNumber = Integer.parseInt(lastProductId.replace("HH", ""));

            // Tăng số lượng lên 1 để tạo ID mới
            int newNumber = lastNumber + 1;
            numberOfExistingProducts = newNumber;
        }
        IDGenerator hhIDGenerator = new IDGenerator("HH", numberOfExistingProducts);
        Product product = new Product(hhIDGenerator);

        // Lưu ảnh avatar
        Path staticPath = Paths.get("static");
        Path imagePath = Paths.get("images");
        if (!Files.exists(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath))) {
            Files.createDirectories(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath));
        }
        Path file = CURRENT_FOLDER.resolve(staticPath).resolve(imagePath).resolve(image.getOriginalFilename());
        try (OutputStream os = Files.newOutputStream(file)) {
            os.write(image.getBytes());
        }
        String Image = product.getIdProd() + "_avatar.jpg";
        String ImageQR = product.getIdProd() + "_qr.jpg";
        // Lưu ảnh QR
        Path qrImagePath = Paths.get("images/qr");
        if (!Files.exists(CURRENT_FOLDER.resolve(staticPath).resolve(qrImagePath))) {
            Files.createDirectories(CURRENT_FOLDER.resolve(staticPath).resolve(qrImagePath));
        }
//        Path qrFile = CURRENT_FOLDER.resolve(staticPath).resolve(qrImagePath).resolve(qrImage.getOriginalFilename());
//        try (OutputStream os = Files.newOutputStream(qrFile)) {
//            os.write(qrImage.getBytes());
//        }

        Supplier supplier;
        supplier = supplierRepository.findByNameSupplier(nameSupplier);
        if(supplier == null){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Supplier name not exit", "")
            );
        }
        product.setSupplier(supplier);
        product.setBrand(brand);
        product.setOrigin(origin);
        product.setDetail(detail);
        product.setProductName(productName);
        product.setRetailPrice(retailPrice);
        product.setUnitName(unitName);
        product.setCategoryName(categoryName);
        product.setCoupons(coupons);
        product.setImageAvatar(imagePath.resolve(image.getOriginalFilename()).toString());
//        product.setImageQR(qrImagePath.resolve(qrImage.getOriginalFilename()).toString());
        Product product1 = repository.save(product);
        return new ResponseEntity<>(product1, HttpStatus.OK);
    }


    //====================================insert new Product with POST method=============================================
    @PostMapping("/insert")
    ResponseEntity<ResponseObject> insertProduct(@RequestBody Product newProduct) {
        // 2 products must not have the same name
        List<Product> foundProducts = repository.findByProductName(newProduct.getProductName());
        if (foundProducts.size() > 0) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Product name already taken", "")
            );
        }
        // Save the Product and its associated Units
        Product savedProduct = repository.save(newProduct);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "Insert Product successfully", savedProduct)
        );
    }


    //==================================== lấy các sản phẩm ===========================

    @GetMapping("/roleUser")
    List<ProductDto> getAllProducts() {
        List<Product> products = repository.findAll();

        // Convert the list of Product entities to a list of ProductDTO objects using ModelMapper
        List<ProductDto> productDtos = products.stream()
                .map(product -> {
                    ProductDto productDto = modelMapper.map(product, ProductDto.class);
                    productDto.setImageAvatar("http://localhost:8080/images/" + product.getImageAvatar());
                    productDto.setImageQR("http://localhost:8080/images/" + product.getImageQR());
                    return productDto;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<>(productDtos, HttpStatus.OK).getBody();
        //return productDTOs;
    }

    //==================================== lấy sản phẩm theo id===========================
    @GetMapping("/roleUser/{id}")
//    let's return an object with: data, message, status
    Object findById(@PathVariable String id){
        Optional<Product> productOptional = repository.findById(id);
        if(productOptional.isEmpty()){
            new ResponseObject("false", "Cannot find product with id = " + id, "");
        }
        Product product = productOptional.get();
        ProductDto productDto = new ProductDto();
        productDto.setIdProd(product.getIdProd());
        productDto.setProductName(product.getProductName());
        productDto.setRetailPrice(product.getRetailPrice());
        productDto.setQuantity(product.getQuantity());
        productDto.setCoupons(product.getCoupons());
        productDto.setDetail(product.getDetail());
        productDto.setUnitName(product.getUnitName());
        productDto.setOrigin(product.getOrigin());
        productDto.setBrand(product.getBrand());
        productDto.setCategoryName(product.getCategoryName());
        productDto.setImageQR(product.getImageQR());
        productDto.setImageAvatar(product.getImageAvatar());
        productDto.setOrderQuantity(product.getOrderQuantity());
        productDto.setQuantitySold(product.getSoldQuantity());
        // productDto.setSupplierName(product.getSupplier().getName());
        return productDto;

    }


    //==================================== lấy các sản phẩm tồn kho===========================
    @GetMapping("/roleUser/productStock")
    List<ProductDto> getProductStock(){
        List<Product> products = repository.findAll();
        List<Product> productList = new ArrayList<>();
        for(Product product : products){
            if(product.getQuantity() > 0){
                productList.add(product);
            }
        }
        List<ProductDto> productDtos = productList.stream()
                .map(product -> {
                    ProductDto productDto = modelMapper.map(product, ProductDto.class);
                    productDto.setImageAvatar( product.getImageAvatar());
                    productDto.setImageQR( product.getImageQR());
                    return productDto;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(productDtos, HttpStatus.OK).getBody();
    }

    //===================================lấy các sản phẩm hết hàng============================
    @GetMapping("/roleUser/productOutOfStock")
    List<ProductDto> getProductOutOfStock(){
        List<Product> products = repository.findAll();
        List<Product> productList = new ArrayList<>();
        for (Product product : products){
            if(product.getQuantity() == 0){
                productList.add(product);
            }
        }
        List<ProductDto> productDtos = productList.stream()
                .map(product -> {
                    ProductDto productDto = modelMapper.map(product, ProductDto.class);
                    productDto.setImageAvatar("http://localhost:8080/" + product.getImageAvatar());
                    productDto.setImageQR("http://localhost:8080/" + product.getImageQR());
                    return productDto;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<>(productDtos, HttpStatus.OK).getBody();
    }

    //==================================== Update sản phẩm ===========================
    @PutMapping("/updateProduct/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("productId") String productId,
            @RequestParam(value = "imageAvatar") MultipartFile image,

            @RequestParam("brand") String brand,
            @RequestParam("origin") String origin,
            @RequestParam("detail") String detail,
            @RequestParam("productName") String productName,
            @RequestParam("unitName") String unitName,
            @RequestParam("categoryName") String categoryName,
            @RequestParam("nameSupplier") String nameSupplier,
            @RequestParam("retailPrice") double retailPrice,
            @RequestParam("coupons") int coupons) throws IOException {

        Optional<Product> optionalProduct = repository.findById(productId);
        if (!optionalProduct.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject("error", "Product not found", "")
            );
        }

        Product product = optionalProduct.get();

        // Update product details
        product.setBrand(brand);
        product.setOrigin(origin);
        product.setDetail(detail);
        product.setProductName(productName);
        product.setRetailPrice(retailPrice);
        product.setUnitName(unitName);
        product.setCategoryName(categoryName);
        product.setCoupons(coupons);

        // Update images if provided
        if (image != null && !image.isEmpty()) {
            // Save new avatar image
            String avatarImageFileName = product.getIdProd() + "_avatar.jpg";
            saveImage(image, "images", avatarImageFileName);
            product.setImageAvatar("images/" + avatarImageFileName);
        }

//        if (qrImage != null && !qrImage.isEmpty()) {
//            // Save new QR image
//            String qrImageFileName = product.getIdProd() + "_qr.jpg";
//            saveImage(qrImage, "images/qr", qrImageFileName);
//            product.setImageQR("images/qr/" + qrImageFileName);
//        }

        // Update supplier and save the product
        Supplier supplier = supplierRepository.findByNameSupplier(nameSupplier);
        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Supplier name not exist", "")
            );
        }
        product.setSupplier(supplier);

        Product updatedProduct = repository.save(product);

        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    private void saveImage(MultipartFile image, String subdirectory, String fileName) throws IOException {
        Path staticPath = Paths.get("static");
        Path imagePath = Paths.get(subdirectory);

        if (!Files.exists(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath))) {
            Files.createDirectories(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath));
        }

        Path file = CURRENT_FOLDER.resolve(staticPath).resolve(imagePath).resolve(fileName);
        try (OutputStream os = Files.newOutputStream(file)) {
            os.write(image.getBytes());
        }
    }


//    update, upsert = update if found, otherwise insert
    @PutMapping("/{id}")
    ResponseEntity<ResponseObject> updateProduct(@RequestBody Product newProduct, @PathVariable String id){
        Product updateProduct = repository.findById(id)
                .map(product -> {
                   product.setProductName(newProduct.getProductName());
                   product.setRetailPrice(newProduct.getRetailPrice());
                   product.setUnitName(newProduct.getUnitName());
                   product.setCategoryName(newProduct.getCategoryName());
                   product.setCoupons(newProduct.getCoupons());
                   product.setDetail(newProduct.getDetail());
                   product.setBrand(newProduct.getBrand());
                   product.setOrigin(newProduct.getOrigin());


                    return repository.save(product);
                }).orElseGet(()->{
                    newProduct.setIdProd(id);
                    return repository.save(newProduct);
                });
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "Update Product successfully", updateProduct)
        );
    }
    @PutMapping("update/{id}")
    ResponseEntity<ResponseObject> updateProduct(
            @PathVariable String id,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) double retailPrice,
            @RequestParam(required = false) String unitName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) int coupons,
            @RequestParam(required = false) String detail,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String origin
    ) {
        Product updatedProduct = repository.findById(id)
                .map(product -> {
                    if (productName != null) {
                        product.setProductName(productName);
                    }
                    if (retailPrice > 0) {
                        product.setRetailPrice(retailPrice);
                    }
                    if(unitName != null){
                        product.setUnitName(unitName);
                    }
                    if (categoryName != null) {
                        product.setCategoryName(categoryName);
                    }
                    if (coupons != product.getCoupons()) {
                        product.setCoupons(coupons);
                    }

                    if (detail != null) {
                        product.setDetail(detail);
                    }
                    if (brand != null) {
                        product.setBrand(brand);
                    }
                    if (origin != null) {
                        product.setOrigin(origin);
                    }

                    return repository.save(product);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return ResponseEntity.ok()
                .body(new ResponseObject("ok", "Update Product successfully", updatedProduct));
    }

    //==================================== Xóa sản phẩm ============================================

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteProduct(@PathVariable String id){
        boolean exists = repository.existsById(id);
        if(exists){
            repository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Delete product successfully", "")
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("failed", "Cannot find product to delete", "")
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseObject> deleteProducts(@RequestBody List<String> ids) {
        List<String> deletedIds = new ArrayList<>();
        List<String> notFoundIds = new ArrayList<>();

        for (String id : ids) {
            boolean exists = repository.existsById(id);
            if (exists) {
                repository.deleteById(id);
                deletedIds.add(id);
            } else {
                notFoundIds.add(id);
            }
        }

        if (!deletedIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Deleted products successfully", deletedIds)
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject("failed", "No products found to delete", notFoundIds)
            );
        }
    }




//==================================== Tìm kiếm sản phẩm=============================================

//    @ApiOperation(value = "API tìm kiếm nâng cao")
//    @PostMapping("/roleUser/search")
//    @ResponseBody
//    public PageResponse<ProductDto> advanceSearch(@Valid @RequestParam SearchProductRequest searchProductRequest,
//                                                  @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer page,
//                                                  @Positive @RequestParam (required = false) Integer size, @RequestParam(required = false) String sort,
//                                                  @RequestParam(required = false)SortOrderEnum order) throws Exception{
//        Pageable pageable = SearchUtil.getPageableFromParam(page, size, sort, order);
//        Page<ProductDto> pageData = productServices.advanceSearch(searchProductRequest, pageable);
//        return new PageResponse<>(pageData);
//    }
//@ApiOperation(value = "API tìm lọc giá")
//@PostMapping("/roleUser/filter")
//@ResponseBody
//public List<PageResponse<ProductDto>> advanceFilter(@Valid @RequestBody SearchProductRequest searchProductRequest,
//                                                    @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer page,
//                                                    @Positive @RequestParam(required = false) Integer size) throws Exception {
//    List<PageResponse<ProductDto>> resultPages = new ArrayList<>();
//
//    // Tính tổng số sản phẩm cần trả về
//    Pageable pageable = SearchUtil.getPageableFromParamP(page, size);
//    Page<ProductDto> pageData = productServices.advanceSearch(searchProductRequest, pageable);
//    long totalElements = pageData.getTotalElements();
//
//    // Tính số lượng trang cần trả về
//    int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / size));
//
//    // Lặp qua từng trang và thêm vào kết quả
//    for (int i = 0; i < totalPages; i++) {
//        pageable = SearchUtil.getPageableFromParamP(i, size);
//        Page<ProductDto> currentPageData = productServices.advanceSearch(searchProductRequest, pageable);
//        resultPages.add(new PageResponse<>(currentPageData));
//    }
//
//    return resultPages;
//}

    //-------------------------------------Số lượng tồn kho-------------------------------------------------

    @GetMapping("/roleUser/inventoryQuantity")
    public ResponseEntity<?> getInventoryQuantity(){
        List<Product> productList = new ArrayList<>();
        productList = repository.findAll();
        int inventoryQuantity = 0;
        if(productList.size() == 0){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Empty product list", ""));
        }
        for(Product product : productList){
            inventoryQuantity += product.getQuantity();
        }
        return  ResponseEntity.ok()
                .body(new ResponseObject("ok", "Total inventory quantity", inventoryQuantity));
    }

    //-------------------------------------Filter-------------------------------------------------
    @GetMapping("/roleUser/price")
    public ResponseEntity<List<Product>> getLaptopsByPriceAndName(
            @RequestParam Float startPrice,
            @RequestParam Float endPrice,
            @RequestParam(required = false) String productName) {

        List<Product> products;
        if (productName != null && !productName.isEmpty()) {
            products = repository.findByRetailPriceBetweenAndProductNameContainingIgnoreCase(startPrice, endPrice, productName);
        } else {
            products = repository.findByRetailPriceBetween(startPrice, endPrice);
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    //-----------------------------Sắp xếp------------------------------------
    @GetMapping("/roleUser/price/sx")
    public ResponseEntity<List<Product>> getProductsByNameAndSortByPrice(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {

        List<Product> products;
        if (productName != null && !productName.isEmpty()) {
            products = repository.findByProductNameContainingIgnoreCase(productName);
        } else {
            products = repository.findAll();
        }

        Comparator<Product> priceComparator = Comparator.comparing(Product::getRetailPrice);

        if (sortDirection.equalsIgnoreCase("desc")) {
            products.sort(priceComparator.reversed());
        } else {
            products.sort(priceComparator);
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    //===========================================Filter   Sắp xếp===========================================
    @GetMapping("/roleUser/priceAndFilter")
  //  @GetMapping("/products")
    public List<ProductDto> searchAndSortProducts(
            @RequestParam(required = false) Float startPrice,
            @RequestParam(required = false) Float endPrice,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {

        List<Product> products;

        if (startPrice != null && endPrice != null) {
            if (productName != null && !productName.isEmpty()) {
                products = repository.findByRetailPriceBetweenAndProductNameContainingIgnoreCase(startPrice, endPrice, productName);
            } else {
                products = repository.findByRetailPriceBetween(startPrice, endPrice);
            }
        } else {
            if (productName != null && !productName.isEmpty()) {
                products = repository.findByProductNameContainingIgnoreCase(productName);
            } else {
                products = repository.findAll();
            }
        }

        Comparator<Product> priceComparator = Comparator.comparing(Product::getRetailPrice);

        if (sortDirection.equalsIgnoreCase("desc")) {
            products.sort(priceComparator.reversed());
        } else {
            products.sort(priceComparator);
        }

        List<ProductDto> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(productDTOs, HttpStatus.OK).getBody();

    }

//===========================================Search CategoryName productName===============================
@GetMapping("/roleUser/searchProdNameAndCategoryName")
public ResponseEntity<List<ProductDto>> searchProducts(
        @RequestParam(required = false) String categoryName,
        @RequestParam(required = false) String productName
) {
    List<Product> products;

    if (categoryName != null && !categoryName.isEmpty() && productName != null && !productName.isEmpty()) {
        // Lấy tất cả các sản phẩm có tên tương ứng với productName trong list sản phẩm có categoryName
        products = repository.findByCategoryNameAndProductNameContainingIgnoreCase(categoryName, productName);
    } else if (categoryName != null && !categoryName.isEmpty()) {
        // Lấy tất cả các sản phẩm theo categoryName
        products = repository.findByCategoryName(categoryName);
    } else if (productName != null && !productName.isEmpty()) {
        // Lấy tất cả các sản phẩm có tên chứa productName
        products = repository.findByProductNameContainingIgnoreCase(productName);
    } else {
        // Lấy tất cả các sản phẩm
        products = repository.findAll();
    }

    // Chuyển đổi từ danh sách Product sang danh sách ProductDto bằng ModelMapper
    List<ProductDto> productDtos = products.stream()
            .map(product -> modelMapper.map(product, ProductDto.class))
            .collect(Collectors.toList());

    return new ResponseEntity<>(productDtos, HttpStatus.OK);
}

//======Search Product by category, name, supplier, warehouse===============
@GetMapping("/roleUser/searchProduct")
public ResponseEntity<List<ProductDto>> searchProduct(
        @RequestParam(required = false) String categoryName,
        @RequestParam(required = false) String productName,
        @RequestParam(required = false) String nameSupplier,
        @RequestParam(required = false) String nameWarehouse
){
    List<Product> products = null;
    // không có dữ liệu
    if(categoryName == null && productName == null && nameSupplier == null && nameWarehouse == null){
        products = repository.findAll();
    }
    // chỉ có categoryName
    else if(categoryName != null && productName == null && nameSupplier == null && nameWarehouse == null){
        products = repository.findByCategoryName(categoryName);
    }
    // chỉ có productName
    else if (productName != null && categoryName == null  && nameSupplier == null && nameWarehouse == null) {
        products = repository.findByProductNameContainingIgnoreCase(productName);
    }
    // chỉ có nameSupplier
    else if (categoryName == null  && productName == null && nameSupplier != null && nameWarehouse == null) {
        products = searchBySupplier(nameSupplier);
    }
    // chỉ có nameWarehouse
    else if (categoryName == null  && nameSupplier == null && productName == null && nameWarehouse != null) {
        products = searchByNameWarehouse(nameWarehouse);
    }
//       có  productName, categoryName
    else if(categoryName != null && productName != null && nameSupplier == null && nameWarehouse == null){
        products = repository.findByCategoryNameAndProductNameContainingIgnoreCase(categoryName, productName);

    }
//        có categoryName, nameSupplier
    else if(categoryName != null && productName == null && nameSupplier != null && nameWarehouse == null){
        List<Product> productListC = repository.findByCategoryName(categoryName);
        List<Product> productListS = searchBySupplier(nameSupplier);
        List<Product> commonProducts = new ArrayList<>(productListC);
        commonProducts.retainAll(productListS);
        products = commonProducts;
    }
//        có categoryName, nameWarehouse
    else if(categoryName != null && productName == null && nameSupplier == null && nameWarehouse != null){
        List<Product> productListC = repository.findByCategoryName(categoryName);
        List<Product> productListW = searchByNameWarehouse(nameWarehouse);
        List<Product> commonProducts = new ArrayList<>(productListC);
        commonProducts.retainAll(productListW);
        products = commonProducts;
    }
    //        có productName, nameSupplier
    else if(categoryName == null && productName != null && nameSupplier != null && nameWarehouse == null){
        List<Product> productListN = repository.findByProductName(productName);
        List<Product> productListS = searchBySupplier(nameSupplier);
        List<Product> commonProducts = new ArrayList<>(productListN);
        commonProducts.retainAll(productListS);
        products = commonProducts;
    }
    //        có productName, nameWarehouse
    else if(categoryName == null && productName != null && nameSupplier == null && nameWarehouse != null){
        List<Product> productListN = repository.findByProductName(productName);
        List<Product> productListW = searchByNameWarehouse(nameWarehouse);
        List<Product> commonProducts = new ArrayList<>(productListN);
        commonProducts.retainAll(productListW);
        products = commonProducts;
    }
    //        có productName, nameWarehouse
    else if(categoryName == null && productName == null && nameSupplier != null && nameWarehouse != null){
        List<Product> productListS = searchBySupplier(nameSupplier);
        List<Product> productListW = searchByNameWarehouse(nameWarehouse);
        List<Product> commonProducts = new ArrayList<>(productListS);
        commonProducts.retainAll(productListW);
        products = commonProducts;
    }
//        có categoryName, productName, nameSupplier
    else if(categoryName != null && productName != null && nameSupplier != null && nameWarehouse == null){
        List<Product> productListC = repository.findByCategoryName(categoryName);
        List<Product> productListN = repository.findByProductName(productName);
        List<Product> productListS = searchBySupplier(nameSupplier);
        List<Product> commonProducts = new ArrayList<>(productListC); // Tạo một danh sách sao chép từ productListC
        commonProducts.retainAll(productListN); // Lọc ra các sản phẩm chung giữa commonProducts và productListN
        commonProducts.retainAll(productListS); // Lọc ra các sản phẩm chung giữa commonProducts và productListS
        products = commonProducts;
    }
    //        có categoryName, productName, nameWarehouse
    else if(categoryName != null && productName != null && nameSupplier == null && nameWarehouse != null){
        List<Product> productListC = repository.findByCategoryName(categoryName);
        List<Product> productListN = repository.findByProductName(productName);
        List<Product> productListW = searchByNameWarehouse(nameWarehouse);
        List<Product> commonProducts = new ArrayList<>(productListC);
        commonProducts.retainAll(productListN);
        commonProducts.retainAll(productListW);
        products = commonProducts;
    }
    //        có categoryName, productName, nameSupplier
    else if(categoryName != null && productName == null && nameSupplier != null && nameWarehouse != null){
        List<Product> productListC = repository.findByCategoryName(categoryName);
        List<Product> productListW = searchByNameWarehouse(nameWarehouse);
        List<Product> productListS = searchBySupplier(nameSupplier);
        List<Product> commonProducts = new ArrayList<>(productListC);
        commonProducts.retainAll(productListW);
        commonProducts.retainAll(productListS);
        products = commonProducts;
    }
    //        có categoryName, productName, nameSupplier
    else if(categoryName == null && productName != null && nameSupplier != null && nameWarehouse != null){
        List<Product> productListN = repository.findByProductName(productName);
        List<Product> productListW = searchByNameWarehouse(nameWarehouse);
        List<Product> productListS = searchBySupplier(nameSupplier);
        List<Product> commonProducts = new ArrayList<>(productListN);
        commonProducts.retainAll(productListW);
        commonProducts.retainAll(productListS);
        products = commonProducts;
    }
    else{
        List<Product> productListC = repository.findByCategoryName(categoryName);
        List<Product> productListN = repository.findByProductName(productName);
        List<Product> productListW = searchByNameWarehouse(nameWarehouse);
        List<Product> productListS = searchBySupplier(nameSupplier);
        List<Product> commonProducts = new ArrayList<>(productListN);
        commonProducts.retainAll(productListW);
        commonProducts.retainAll(productListS);
        commonProducts.retainAll(productListC);
        products = commonProducts;
    }
    List<ProductDto> productDtos = products.stream()
            .map(product -> {
                ProductDto productDto = modelMapper.map(product, ProductDto.class);
                productDto.setImageAvatar( product.getImageAvatar());
                productDto.setImageQR(product.getImageQR());
                return productDto;
            })
            .collect(Collectors.toList());

    return new ResponseEntity<>(productDtos, HttpStatus.OK);
}


    private List<Product> searchBySupplier(String nameSupplier){
        Supplier supplier;
        List<Product> productList, products = new ArrayList<>();
        supplier = supplierRepository.findByNameSupplier(nameSupplier);
        productList = repository.findAll();
        for (Product product : productList){
            if(supplier.equals(product.getSupplier())){
                products.add(product);
            }
        }
        return products;
    }

    private List<Product> searchByNameWarehouse(String nameWarehouse){
        Optional<Warehouse> warehouseOptional = warehouseRepository.findByName(nameWarehouse);
        if (warehouseOptional.isEmpty()){
            return null;
        }
        Warehouse warehouse = warehouseOptional.get();
        List<ProductStandard> productStandards = warehouse.getProductStandards();
        List<Product> products = new ArrayList<>();
        for (ProductStandard productStandard : productStandards){
            products.add(productStandard.getProduct());
        }
        return products;
    }



    //===========================================Search===========================================
    @GetMapping("/roleUser/searchProName")
    public ResponseEntity<List<Product>> getLaptopsByName(
            @RequestParam(required = false) String productName) {

        List<Product> products;
        if (productName != null && !productName.isEmpty()) {
            products = repository.findByProductNameContainingIgnoreCase(productName);
        } else {
           products = repository.findAll();
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/roleUser/searchProdByCategory")
    public ResponseEntity<List<Product>> getProductByCategoryName(
            @RequestParam(required = false) String categoryName

    ){
        List<Product> products;
        if(categoryName != null && !categoryName.isEmpty()){
            products = repository.findByCategoryName(categoryName);
        }else{
            products = repository.findAll();
        }
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/roleUser/searchProdNameByCategory")
    public ResponseEntity<List<Product>> getProductByCategoryName(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String productName
    ) {
        List<Product> products;
        if (categoryName != null && !categoryName.isEmpty()) {
            if (productName != null && !productName.isEmpty()) {
                // Lọc theo cả categoryName và productName
                products = repository.findByCategoryNameAndProductName(categoryName, productName);
            } else {
                // Lọc chỉ theo categoryName
                products = repository.findByCategoryName(categoryName);
            }
        } else {
            products = repository.findAll();
        }
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    private List<Product> searchProducts(String productName) {
        List<Product> products = repository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + productName.toLowerCase() + "%")
        );
        return products;
    }
    @ApiOperation(value = "API tìm lọc giá")
    @PostMapping("/roleUser/filter")
    @ResponseBody
    public PageResponse<ProductDto> advanceFilters(@Valid @RequestBody SearchProductRequest searchProductRequest,
                                                  @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer page,
                                                  @Positive @RequestParam (required = false, defaultValue = "15") Integer size) throws Exception{
        Pageable pageable = SearchUtil.getPageableFromParamP(page, size);
        Page<ProductDto> pageData = productServices.advanceSearch(searchProductRequest, pageable);
        return new PageResponse<>(pageData);
    }

    @GetMapping("/roleUser/coupons")
    public ResponseEntity<List<ProductDto>> getProductsByCoupons() {
        List<Product> productsWithCoupons = repository.findByCouponsNot(0);
        // Chuyển đổi từ danh sách Product sang danh sách ProductDto bằng ModelMapper
        List<ProductDto> productDtos = productsWithCoupons.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());

        return new ResponseEntity<>(productDtos, HttpStatus.OK);
       // return new ResponseEntity<>(productsWithCoupons, HttpStatus.OK);
    }


}




////==========================================search product by name====================
//    @GetMapping("/searchProduct")
//    public ResponseEntity<List<Product>> searchProductByKey(@RequestParam String productName) {
//        List<Product> products = repository.findAll((root, query, criteriaBuilder) ->
//                criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + productName.toLowerCase() + "%")
//        );
//        return new ResponseEntity<>(products, HttpStatus.OK);
//    }

//    private List<Product> searchProducts(String productName) {
//        List<Product> products = repository.findAll((root, query, criteriaBuilder) ->
//                criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + productName.toLowerCase() + "%")
//        );
//
//        return products;
//    }

//    @GetMapping("/price")
//    public ResponseEntity<List<Product>> getLaptopByPrice(@RequestParam Float startPrice, @RequestParam Float endPrice){
//        return new ResponseEntity<List<Product>>(repository.findByPriceBetween(startPrice, endPrice), HttpStatus.OK);
//    }


