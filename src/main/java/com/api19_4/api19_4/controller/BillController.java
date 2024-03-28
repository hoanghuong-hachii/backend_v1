package com.api19_4.api19_4.controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.api19_4.api19_4.dto.*;
import com.api19_4.api19_4.models.*;
import com.api19_4.api19_4.repositories.*;
import com.api19_4.api19_4.services.BillService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

//@CrossOrigin(origins = "http://127.0.0.1:5000")
//@CrossOrigin(origins = "http://192.168.60.5:8080")
@RestController
@RequestMapping(path = "/api/v5/Bill")
@SpringBootApplication
@ComponentScan(basePackages = "com.api19_4.api19_4") // Thay thế bằng package chứa các bean của ứng dụng của bạn
public class BillController {
    private final List<SseEmitter> emitters = new ArrayList<>();

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BillRepository repository;
    @Autowired
    private BillService billService;

    @Autowired
    private UserRepositories userRepositories;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private ProductBillRepository productBillRepository;
    @Autowired
    private DateStatusChangeRepository dateStatusChangeRepository;
    @Autowired
    private ModelMapper modelMapper;



    @GetMapping("")
    List<BillDTO> getAllBills() {
        List<Bill> bills = repository.findAll();

        // Convert the list of Bill entities to a list of BillDTO objects using ModelMapper
        List<BillDTO> billDTOs = bills.stream()
                .map(bill -> modelMapper.map(bill, BillDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(billDTOs, HttpStatus.OK).getBody();
        //return billDTOs;
    }

    @PostMapping("/bills")
    public ResponseEntity<?> create(@RequestBody BillDTO billDTO) {
            // Check if the user ID is null
            if (billDTO.getIdUser() == null) {
                return new ResponseEntity<>("User ID is null", HttpStatus.BAD_REQUEST);
            }
            Bill bill = new Bill();
            bill.setNumberPhoneCustomer(billDTO.getNumberPhoneCustomer());
            bill.setAddressCustomer(billDTO.getAddressCustomer());
            // Chuyển đổi định dạng từ String sang LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            LocalDateTime dateImport = LocalDateTime.parse(billDTO.getDateTimeOrder(), formatter);
            bill.setDateTimeOrder(dateImport);
            bill.setStatus("Pending");
          //  bill.setDiscount(billDTO.getDiscount());
            bill.setShippingFee(billDTO.getShippingFee());
            bill.setNote(billDTO.getNote());
            Optional<UserInfo> userOptional = userRepositories.findById(billDTO.getIdUser());
            if (userOptional.isEmpty()) {
                return new ResponseEntity<>("Invalid User ID", HttpStatus.BAD_REQUEST);
            }
            UserInfo user = userOptional.get();
            bill.setUser(user);

            bill.setTotalPayment(0); // Khởi tạo tổng giá trị của hóa đơn là 0
            List<Bill> bills = repository.findAll();
            long maxid = 0;
            for(Bill bill1 : bills){
                if(maxid < bill1.getIdBill()){
                    maxid = bill1.getIdBill();
                }
            }
            // Lưu danh sách ProductBill
            List<ProductBill> productBills = new ArrayList<>();
            for (ProductBillDTO productBillDTO : billDTO.getProductBillDTOS()) {
                ProductBill productBill = new ProductBill();
                productBill.setBill(bill);
                productBill.setQuantity(productBillDTO.getQuantity());
                Optional<Product> productOptional = productRepository.findById(productBillDTO.getProductId());
                if (productOptional.isEmpty()) {
                    return new ResponseEntity<>("Invalid Product ID", HttpStatus.BAD_REQUEST);
                }
                Product product = productOptional.get();
                if(product.getQuantity() < productBillDTO.getQuantity()){
                    String errorMessage = "The requested sold quantity (" + productBillDTO.getQuantity() + ") exceeds the available quantity (" + product.getQuantity() + ") in the unit.";
                    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
                }
                productBill.setDiscount(product.getCoupons());
                product.setOrderQuantity(product.getOrderQuantity() + productBillDTO.getQuantity()); // Cập nhật quantitySold
                productBill.setProduct(product);
                productBill.setStatus("Ordered");
                productBill.setTotalPriceProd(product.getRetailPrice() * productBillDTO.getQuantity() - product.getRetailPrice() * productBillDTO.getQuantity() * ((double) productBillDTO.getDiscount() /100)); // Tính tổng giá trị của sản phẩm
                productBills.add(productBill);
                bill.setTotalPayment(bill.getTotalPayment() + productBill.getTotalPriceProd()); // Cộng tổng giá trị các sản phẩm vào tổng giá trị của hóa đơn
                List<Unit> unitList = unitRepository.findAll();
                if(unitList.isEmpty()){
                    return new ResponseEntity<>("Empty Unit List", HttpStatus.BAD_REQUEST);
                }
                int tmp = productBillDTO.getQuantity();
                for(Unit unit : unitList){
                    if(Objects.equals(unit.getProductId(), product.getIdProd())){
                        if(tmp <= unit.getQuantity()){
                            unit.setOrderQuantity(unit.getOrderQuantity() + productBillDTO.getQuantity());
                            break;
                        }
                        else {
                            tmp = productBillDTO.getQuantity() - unit.getQuantity();
                            unit.setOrderQuantity(unit.getOrderQuantity() + unit.getQuantity());
                            unit.setQuantity(0);
                        }
                    }
                }

            }

            // Lưu danh sách ProductBill vào hóa đơn
            bill.setPayableAmount(bill.getTotalPayment() + billDTO.getShippingFee());
            bill.setProductBills(productBills);
            user.getBills().add(bill);

            //set DateStatusChange
            DateStatusChange dateStatusChange = new DateStatusChange();
            dateStatusChange.setBill(bill);
            dateStatusChange.setStatus(bill.getStatus());
            dateStatusChange.setDateTimeOrder(bill.getDateTimeOrder());

            Bill saveBill = repository.save(bill);
            DateStatusChange dateStatusChange1 = dateStatusChangeRepository.save(dateStatusChange);
            return ResponseEntity.status(HttpStatus.CREATED).body(saveBill);
    }

    @PutMapping("/bills/{id}")
    public ResponseEntity<?> updateBill(@PathVariable("id") Long id, @RequestBody BillDTO billDTO)  {
        if (id == null) {
            return new ResponseEntity<>("Bill ID is null", HttpStatus.BAD_REQUEST);
        }
        Optional<Bill> billOptional = repository.findById(id);
        if (billOptional.isEmpty()) {
            return new ResponseEntity<>("Invalid Bill ID", HttpStatus.NOT_FOUND);
        }
        Bill bill = billOptional.get();
        bill.setNumberPhoneCustomer(billDTO.getNumberPhoneCustomer());
        bill.setAddressCustomer(billDTO.getAddressCustomer());
        // Chuyển đổi định dạng từ String sang LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        LocalDateTime dateImport = LocalDateTime.parse(billDTO.getDateTimeOrder(), formatter);
        bill.setDateTimeOrder(dateImport);
        bill.setStatus(billDTO.getStatus());
        bill.setNote(billDTO.getNote());
        //bill.setDiscount(billDTO.getDiscount());
        bill.setShippingFee(billDTO.getShippingFee());
        bill.setTotalPayment(billDTO.getTotalPayment());
        bill.setPayableAmount(billDTO.getPayableAmount());
        Optional<UserInfo> userOptional = userRepositories.findById(billDTO.getIdUser());
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>("Invalid User ID", HttpStatus.BAD_REQUEST);
        }
        UserInfo user = userOptional.get();
        bill.setUser(user);
        List<ProductBill> productBills = bill.getProductBills();
        List<ProductBillDTO> productBillDTOList = billDTO.getProductBillDTOS();

        //===================
        String idUser = billDTO.getIdUser();
        String status = billDTO.getStatus();
        String trangthai = "";
        if(status.equals("Shipped")){
            trangthai = "Đang giao hàng";
        }
        else if(status.equals("Successful_delivery")){
            trangthai = "Giao hàng thành công";
        }

        sendNotification(id,trangthai,idUser);
        //===============
        if(Objects.equals(billDTO.getStatus(), "Cancelled")){
            // Lưu danh sách ProductBill
            for(ProductBillDTO productBillDTO : productBillDTOList){
                if(IsExist(productBillDTO, productBills)){
                    Optional<ProductBill> productBillOptional = productBillRepository.findById(productBillDTO.getIdProductBill());
                    if(productBillOptional.isEmpty()){
                        continue;
                    }
                    ProductBill productBill = productBillOptional.get();
                    Optional<Product> productOptional = productRepository.findById(productBill.getProduct().getIdProd());
                    if(productOptional.isEmpty()){
                        return new ResponseEntity<>("Invalid Product ID", HttpStatus.BAD_REQUEST);
                    }
                    Product product = productOptional.get();
                    List<Unit> unitList = unitRepository.findAll();
                    for(Unit unit: unitList){
                        if(unit.getProductId().equals(productBill.getProduct().getIdProd())){
                            unit.setOrderQuantity(unit.getOrderQuantity() - productBill.getQuantity());
                            product.setOrderQuantity(product.getOrderQuantity() - productBill.getQuantity());
                            productBill.setQuantity(productBillDTO.getQuantity());
                            productBill.setTotalPriceProd(productBillDTO.getTotalPriceProd());
                        }
                    }
                }
            }
        } else if (Objects.equals(billDTO.getStatus(), "Shipped") || Objects.equals(billDTO.getStatus(), "Successful_delivery")) {
            // Lưu danh sách ProductBill
            for(ProductBillDTO productBillDTO : productBillDTOList){
                if(IsExist(productBillDTO, productBills)){
                    Optional<ProductBill> productBillOptional = productBillRepository.findById(productBillDTO.getIdProductBill());
                    if(productBillOptional.isEmpty()){
                        continue;
                    }
                    ProductBill productBill = productBillOptional.get();
                    Optional<Product> productOptional = productRepository.findById(productBill.getProduct().getIdProd());
                    if(productOptional.isEmpty()){
                        return new ResponseEntity<>("Invalid Product ID", HttpStatus.BAD_REQUEST);
                    }
                    Product product = productOptional.get();
                    List<Unit> unitList = unitRepository.findAll();
                    if (unitList.isEmpty()){
                        return new ResponseEntity<>("Unit List is Empty", HttpStatus.BAD_REQUEST);
                    }
                    for (Unit unit: unitList){
                        if(unit.getProductId().equals(productBill.getProduct().getIdProd())){
                            unit.setSoldQuantity(unit.getSoldQuantity() + productBill.getQuantity());
                            unit.setQuantity(unit.getQuantity() - productBill.getQuantity());
                            unit.setOrderQuantity(unit.getOrderQuantity() - productBill.getQuantity());
                            product.setOrderQuantity(product.getOrderQuantity() - productBill.getQuantity());
                            product.setQuantity(product.getQuantity() - productBill.getQuantity());
                            product.setSoldQuantity(product.getSoldQuantity() + productBill.getQuantity());
                            productBill.setQuantity(productBillDTO.getQuantity());
                            productBill.setTotalPriceProd(productBillDTO.getTotalPriceProd());
                        }
                    }

                }
            }
        } else {
            // Lưu danh sách ProductBill
            for(ProductBillDTO productBillDTO : productBillDTOList){
                if(IsExist(productBillDTO, productBills)){
                    Optional<ProductBill> productBillOptional = productBillRepository.findById(productBillDTO.getIdProductBill());
                    if(productBillOptional.isEmpty()){
                        continue;
                    }
                    ProductBill productBill = productBillOptional.get();
                    productBill.setQuantity(productBillDTO.getQuantity());
                    productBill.setTotalPriceProd(productBillDTO.getTotalPriceProd());
                }
            }
        }
        bill.setProductBills(productBills);
        user.getBills().add(bill);

        //set DateStatusChange
        DateStatusChange dateStatusChange = new DateStatusChange();
        dateStatusChange.setBill(bill);
        dateStatusChange.setStatus(bill.getStatus());
        dateStatusChange.setDateTimeOrder(bill.getDateTimeOrder());
        DateStatusChange dateStatusChange1 = dateStatusChangeRepository.save(dateStatusChange);
        Bill saveBill = repository.save(bill);

        return ResponseEntity.status(HttpStatus.CREATED).body(saveBill);
    }
    private static String fetchToken(String idUser) throws IOException {
        String targetUrl = "http://localhost:8080/notification/token/" + idUser;

        URL url = new URL(targetUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Return the fetched token
            return response.toString();
        } else {
            throw new IOException("Failed to fetch token for idUser: " + idUser);
        }
    }
    public static void sendNotification(long id, String status,String idUser) {
        try {
            String token = fetchToken(idUser);

            System.out.println("token="+token);
            String targetUrl = "http://localhost:8080/notification/token";
            String requestBody = "{\n" +
                    "  \"title\": \"Thông báo trạng thái đơn hàng\",\n" +
                    "  \"message\": \"Đơn hàng mã #" + id + " :" + status + "\",\n" +
                    "  \"token\": \"" + token + "\"\n" +
                    "}";

            byte[] postData = requestBody.getBytes(StandardCharsets.UTF_8);

            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", String.valueOf(postData.length));
            connection.setDoOutput(true);

            try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
                dataOutputStream.write(postData);
                dataOutputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    boolean IsExist(ProductBillDTO productBillDTO, List<ProductBill> productBillList){
        for(ProductBill productBill : productBillList){
            if(productBill.getProduct().getIdProd().equals(productBillDTO.getProductId())){
                return true;
            }
        }
        return false;
    }
    @GetMapping("/statusUpdates")
    public SseEmitter subscribeToStatusUpdates() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);

        // Remove emitter on completion
        emitter.onCompletion(() -> emitters.remove(emitter));
        // Remove emitter on timeout
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }
    @PutMapping("updateStatus/{id}")
    public ResponseEntity<?> updateStatusBill(@PathVariable("id") Long id, @RequestParam String status){
        if (id == null) {
            return new ResponseEntity<>("Bill ID is null", HttpStatus.BAD_REQUEST);
        }
        String trangthai = "";
        if(status.equals("Shipped")){
            trangthai = "Đang giao hàng";
        }
        else if(status.equals("Successful_delivery")){
            trangthai = "Giao hàng thành công";
        }
       // sendNotification(id,trangthai,);
        Optional<Bill> billOptional = repository.findById(id);
        if (billOptional.isEmpty()) {
            return new ResponseEntity<>("Invalid Bill ID", HttpStatus.NOT_FOUND);
        }
        Bill bill = billOptional.get();
        bill.setStatus(status);
        List<ProductBill> productBills = bill.getProductBills();
        List<ProductBill> productBillList = bill.getProductBills();
        if(Objects.equals(bill.getStatus(), "Cancelled")){
            // Lưu danh sách ProductBill
            for(ProductBill productBill : productBillList){
                Optional<Product> productOptional = productRepository.findById(productBill.getProduct().getIdProd());
                if(productOptional.isEmpty()){
                    return new ResponseEntity<>("Invalid Product ID", HttpStatus.BAD_REQUEST);
                }
                Product product = productOptional.get();
                List<Unit> unitList = unitRepository.findAll();
                for(Unit unit: unitList){
                    if(unit.getProductId().equals(productBill.getProduct().getIdProd())){
                        unit.setOrderQuantity(unit.getOrderQuantity() - productBill.getQuantity());
                        product.setOrderQuantity(product.getOrderQuantity() - productBill.getQuantity());
                        productBill.setQuantity(productBill.getQuantity());
                        productBill.setTotalPriceProd(productBill.getTotalPriceProd());
                    }
                }

            }
        } else if (Objects.equals(status, "Shipped") || Objects.equals(status, "Successful_delivery")) {
            // Lưu danh sách ProductBill
            for(ProductBill productBill : productBillList){
                    Optional<Product> productOptional = productRepository.findById(productBill.getProduct().getIdProd());
                    if(productOptional.isEmpty()){
                        return new ResponseEntity<>("Invalid Product ID", HttpStatus.BAD_REQUEST);
                    }
                    Product product = productOptional.get();
                    List<Unit> unitList = unitRepository.findAll();
                    if (unitList.isEmpty()){
                        return new ResponseEntity<>("Unit List is Empty", HttpStatus.BAD_REQUEST);
                    }
                    for (Unit unit: unitList){
                        if(unit.getProductId().equals(productBill.getProduct().getIdProd())){
                            unit.setSoldQuantity(unit.getSoldQuantity() + productBill.getQuantity());
                            unit.setQuantity(unit.getQuantity() - productBill.getQuantity());
                            unit.setOrderQuantity(unit.getOrderQuantity() - productBill.getQuantity());
                            product.setOrderQuantity(product.getOrderQuantity() - productBill.getQuantity());
                            product.setQuantity(product.getQuantity() - productBill.getQuantity());
                            product.setSoldQuantity(product.getSoldQuantity() + productBill.getQuantity());
                            productBill.setQuantity(productBill.getQuantity());
                            productBill.setTotalPriceProd(productBill.getTotalPriceProd());
                        }
                    }


            }
        } else {
            // Lưu danh sách ProductBill
            for(ProductBill productBill : productBillList){
                    productBill.setQuantity(productBill.getQuantity());
                    productBill.setTotalPriceProd(productBill.getTotalPriceProd());
                }
        }
        bill.setProductBills(productBills);
        //set DateStatusChange
        DateStatusChange dateStatusChange = new DateStatusChange();
        dateStatusChange.setBill(bill);
        dateStatusChange.setStatus(bill.getStatus());
        dateStatusChange.setDateTimeOrder(bill.getDateTimeOrder());
        DateStatusChange dateStatusChange1 = dateStatusChangeRepository.save(dateStatusChange);
        Bill saveBill = repository.save(bill);
//============================
        for (SseEmitter emitter : new ArrayList<>(emitters)) {
            try {
                emitter.send(status, MediaType.TEXT_PLAIN);
            } catch (IOException e) {
                // Handle exception (e.g., remove broken emitters)
                emitters.remove(emitter);
            }
        }
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

    @GetMapping("GetStatus/{id}")
    public ResponseEntity<List<DateStatusChangeDto>> getStatus(@PathVariable Long id){
        List<DateStatusChangeDto> dateStatusChangeList1 = new ArrayList<>();
        List <DateStatusChange> dateStatusChangeList = dateStatusChangeRepository.findAll();
        for (DateStatusChange dateStatusChange : dateStatusChangeList){
            if(dateStatusChange.getBill().getIdBill().equals(id)){
                DateStatusChangeDto dateStatusChangeDto = new DateStatusChangeDto();
                dateStatusChangeDto.setIdBill(id);
                dateStatusChangeDto.setIdDateStatusChange(dateStatusChange.getIdDateStatusChange());
                dateStatusChangeDto.setStatus(dateStatusChange.getStatus());
                LocalDateTime dateTime = dateStatusChange.getDateTimeOrder();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
                String formattedDateTime = dateTime.format(formatter);
                dateStatusChangeDto.setDateTimeOrder(formattedDateTime);
                dateStatusChangeList1.add(dateStatusChangeDto);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(dateStatusChangeList1);
    }


    //========================hiển thị doanh thu theo ngày==========================
    @GetMapping("/displayRevenue")
    public ResponseEntity<?> displayRevenue(
            @RequestParam String  startDate
    ){
        LocalDateTime parsedStartDate = null;
        if (startDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            parsedStartDate = LocalDate.parse(startDate, formatter).atStartOfDay();
        }
        final LocalDateTime finalParsedStartDate = parsedStartDate;
        List<Bill> billList= repository.findAll();
        if(billList.isEmpty()){
            return new ResponseEntity<>("Empty Bill List", HttpStatus.BAD_REQUEST);
        }
        List<Bill> bills = repository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();


            if (finalParsedStartDate != null) {
                predicates.add(criteriaBuilder.equal(root.get("dateTimeOrder"), finalParsedStartDate));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
        if (bills.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BillSummaryDto billSummaryDto = new BillSummaryDto();
        for (Bill bill1 : bills){
            billSummaryDto.setTotalBillCount(billSummaryDto.getTotalBillCount() + 1);
            billSummaryDto.setTotalPrice(billSummaryDto.getTotalBillCount() + bill1.getPayableAmount());
        }
        return new ResponseEntity<>(billSummaryDto, HttpStatus.OK);
    }



    @GetMapping("/search")
    public ResponseEntity<?> searchBill(
            @RequestParam(required = false) String idBill,
            @RequestParam(required = false) String nameCustomer,
            @RequestParam(required = false)  String idUser,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String  startDate,
            @RequestParam(required = false) String  endDate
    ) {
        // Parse the startDate and endDate to LocalDateTime
        LocalDateTime parsedStartDate = null;
        LocalDateTime parsedEndDate = null;
        if (startDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            parsedStartDate = LocalDate.parse(startDate, formatter).atStartOfDay();
        }

        if (endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            parsedEndDate = LocalDate.parse(endDate, formatter).atTime(23, 59, 59); // Set the time to end of the day
        }
        final LocalDateTime finalParsedStartDate = parsedStartDate; // effectively final
        final LocalDateTime finalParsedEndDate = parsedEndDate; // effectively final


        List<Bill> bills = repository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(idBill != null){
                predicates.add(criteriaBuilder.equal(root.get("idBill"), idBill));
            }

            if(nameCustomer != null && !nameCustomer.isEmpty()){
                Join<Bill, UserInfo> userJoin = root.join("user");
                predicates.add(criteriaBuilder.like(userJoin.get("userName"), "%" + nameCustomer + "%"));
            }

            if(idUser != null && !idUser.isEmpty()){
                Join<Bill, UserInfo> userJoin = root.join("user");
                predicates.add(criteriaBuilder.equal(userJoin.get("idUser"), idUser));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (finalParsedStartDate != null && finalParsedEndDate != null) {
                predicates.add(criteriaBuilder.between(root.get("dateTimeOrder"), finalParsedStartDate, finalParsedEndDate));
            } else if (finalParsedStartDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateTimeOrder"), finalParsedStartDate));
            } else if (finalParsedEndDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateTimeOrder"), finalParsedEndDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        if (bills.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<BillDTO> billDTOList = new ArrayList<>();
        for(Bill bill1 : bills){
            BillDTO billDTO = new BillDTO();
            billDTO.setIdBill(bill1.getIdBill());
            billDTO.setIdUser(bill1.getUser().getIdUser());
            billDTO.setUserName(bill1.getUser().getUserName());
            billDTO.setNumberPhoneCustomer(bill1.getNumberPhoneCustomer());
            billDTO.setAddressCustomer(bill1.getAddressCustomer());
            billDTO.setNote(bill1.getNote());
            // billDTO.setDiscount(bill1.getDiscount());
            billDTO.setShippingFee(bill1.getShippingFee());
            billDTO.setPayableAmount(bill1.getPayableAmount());
            billDTO.setStatus(bill1.getStatus());
            // Assuming bill1.getDateTimeOrder() returns a LocalDateTime instance
            LocalDateTime dateTime = bill1.getDateTimeOrder();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            String formattedDateTime = dateTime.format(formatter);

            billDTO.setDateTimeOrder(formattedDateTime);
            billDTO.setStatus(bill1.getStatus());
            billDTO.setTotalPayment(bill1.getTotalPayment());

            List<ProductBill> productBillList = bill1.getProductBills();
            if (productBillList.isEmpty()) {
                return new ResponseEntity<>("ProductBill is empty", HttpStatus.BAD_REQUEST);
            }

            List<ProductBillDTO> productBillDTOList = new ArrayList<>();

            for (ProductBill productBill : productBillList) {
                // Create a new ProductBillDTO for each ProductBill
                ProductBillDTO productBillDTO = new ProductBillDTO();
                productBillDTO.setIdProductBill(productBill.getIdProdBill());
                productBillDTO.setProductId(productBill.getProduct().getIdProd());
                productBillDTO.setProductName(productBill.getProduct().getProductName());
                productBillDTO.setUnitName(productBill.getProduct().getUnitName());
                productBillDTO.setRetailPrice(productBill.getProduct().getRetailPrice());
                productBillDTO.setQuantity(productBill.getQuantity());
                productBillDTO.setDiscount(productBill.getDiscount());
                productBillDTO.setStatus(productBill.getStatus());
                productBillDTO.setTotalPriceProd(productBill.getTotalPriceProd());
                // Add the ProductBillDTO to the list
                productBillDTOList.add(productBillDTO);
            }
            billDTO.setProductBillDTOS(productBillDTOList);
            billDTOList.add(billDTO);
        }

        // Chuyển đổi danh sách sản phẩm tìm thấy thành danh sách DTO hoặc các đối tượng phù hợp khác

        return new ResponseEntity<>(billDTOList, HttpStatus.OK);
    }

    @GetMapping("/getNumberBillByStatus")
    public ResponseEntity<?> searchBill1(){
        List<Bill> billList = repository.findAll();
        if(billList.isEmpty()){
            return new ResponseEntity<>("Empty Bill List", HttpStatus.BAD_REQUEST);
        }

        // Initialize a map to store the count for each status
        Map<String, Integer> statusCountMap = new HashMap<>();
        initializeStatusCounts(statusCountMap);

        // Iterate through the billList and update the counts for each status
        for(Bill bill : billList){
            String status = bill.getStatus(); // Assuming Bill class has a getStatus() method
            if (statusCountMap.containsKey(status)) {
                int count = statusCountMap.get(status);
                statusCountMap.put(status, count + 1);
            }
        }

        // Create a list of StatusBasedOrderCounterDto objects
        List<StatusBasedOrderCounterDto> statusBasedOrderCounterDtos = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusCountMap.entrySet()) {
            StatusBasedOrderCounterDto dto = new StatusBasedOrderCounterDto();
            dto.setStatus(entry.getKey());
            dto.setQuantity(entry.getValue());
            statusBasedOrderCounterDtos.add(dto);
        }

        return new ResponseEntity<>(statusBasedOrderCounterDtos, HttpStatus.OK);
    }

    private void initializeStatusCounts(Map<String, Integer> statusCountMap) {
        // Initialize the map with all status values and set the initial count to 0
        List<String> statusList = Arrays.asList("Pending", "Processing", "Shipped", "Successful_delivery",
                "Unsuccessful_delivery", "Cancelled", "Out_for_Delivery", "Payment_Received");

        for (String status : statusList) {
            statusCountMap.put(status, 0);
        }
    }


    @GetMapping("/totalPaymentByDateRange")
    public ResponseEntity<?> getTotalPaymentByDateRange() {
        // Get the list of dates from the earliest date in the bills to the current date
        List<Bill> bills = repository.findAll();
        if(bills.isEmpty()){
            return new ResponseEntity<>("Empty Bill List", HttpStatus.BAD_REQUEST);
        }
        List<LocalDate> dateList = getDateList(bills);

        List<DailyTotalPaymentDTO> dailyTotalPayments = new ArrayList<>();

        for (LocalDate date : dateList) {
            BigDecimal totalPayment = BigDecimal.ZERO;

            for (Bill bill : bills) {
                LocalDateTime dateTimeOrder = bill.getDateTimeOrder();
                LocalDate billDate = dateTimeOrder.toLocalDate();

                if (billDate.isEqual(date)) {
                    totalPayment = totalPayment.add(BigDecimal.valueOf(bill.getTotalPayment()));
                }
            }

            DailyTotalPaymentDTO dailyTotalPaymentDTO = new DailyTotalPaymentDTO();
            dailyTotalPaymentDTO.setDate(date);
            dailyTotalPaymentDTO.setTotalPayment(totalPayment);

            dailyTotalPayments.add(dailyTotalPaymentDTO);
        }

        return new ResponseEntity<>(dailyTotalPayments, HttpStatus.OK);
    }

    private List<LocalDate> getDateList(List<Bill> bills) {
        // Determine the start date based on the earliest date in bills
        LocalDate startDate = bills.stream()
                .map(Bill::getDateTimeOrder)
                .map(LocalDateTime::toLocalDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate currentDate = LocalDate.now();
        List<LocalDate> dateList = new ArrayList<>();

        // Generate a list of dates from the start date to the current date
        LocalDate date = startDate;
        while (!date.isAfter(currentDate)) {
            dateList.add(date);
            date = date.plusDays(1);
        }

        return dateList;
    }


    @GetMapping("/totalPaymentByMonthRange")
    public ResponseEntity<?> getTotalPaymentByMonthRange() {
        // Get the list of bills
        List<Bill> bills = repository.findAll();
        if (bills.isEmpty()) {
            return new ResponseEntity<>("Empty Bill List", HttpStatus.BAD_REQUEST);
        }

        Map<String, BigDecimal> monthlyTotalPayments = new LinkedHashMap<>();

        for (Bill bill : bills) {
            LocalDateTime dateTimeOrder = bill.getDateTimeOrder();
            String monthYearKey = getMonthYearKey(dateTimeOrder.toLocalDate());

            // Calculate totalPayment for the month and year
            BigDecimal totalPayment = BigDecimal.valueOf(bill.getTotalPayment());
            monthlyTotalPayments.merge(monthYearKey, totalPayment, BigDecimal::add);
        }

        List<MonthlyTotalPaymentDTO> monthlyTotalPaymentDTOs = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : monthlyTotalPayments.entrySet()) {
            MonthlyTotalPaymentDTO monthlyTotalPaymentDTO = new MonthlyTotalPaymentDTO();
            monthlyTotalPaymentDTO.setMonthYear(entry.getKey());
            monthlyTotalPaymentDTO.setTotalPayment(entry.getValue());

            monthlyTotalPaymentDTOs.add(monthlyTotalPaymentDTO);
        }

        return new ResponseEntity<>(monthlyTotalPaymentDTOs, HttpStatus.OK);
    }

    private String getMonthYearKey(LocalDate date) {
        // Create a key using the format "MM-yyyy" (e.g., "09-2023")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        return date.format(formatter);
    }


    @GetMapping("/totalPaymentByWeekRange")
    public ResponseEntity<?> getTotalPaymentByWeekRange() {
        // Get the list of bills
        List<Bill> bills = repository.findAll();
        if (bills.isEmpty()) {
            return new ResponseEntity<>("Empty Bill List", HttpStatus.BAD_REQUEST);
        }

        Map<String, BigDecimal> weeklyTotalPayments = new LinkedHashMap<>();

        for (Bill bill : bills) {
            LocalDateTime dateTimeOrder = bill.getDateTimeOrder();
            String weekYearKey = getWeekYearKey(dateTimeOrder);

            // Calculate totalPayment for the week and year
            BigDecimal totalPayment = BigDecimal.valueOf(bill.getTotalPayment());
            weeklyTotalPayments.merge(weekYearKey, totalPayment, BigDecimal::add);
        }

        List<WeeklyTotalPaymentDTO> weeklyTotalPaymentDTOs = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : weeklyTotalPayments.entrySet()) {
            WeeklyTotalPaymentDTO weeklyTotalPaymentDTO = new WeeklyTotalPaymentDTO();
            weeklyTotalPaymentDTO.setWeekYear(entry.getKey());
            weeklyTotalPaymentDTO.setTotalPayment(entry.getValue());

            weeklyTotalPaymentDTOs.add(weeklyTotalPaymentDTO);
        }

        return new ResponseEntity<>(weeklyTotalPaymentDTOs, HttpStatus.OK);
    }

    private String getWeekYearKey(LocalDateTime dateTime) {
        // Calculate week of year and year
        int weekOfYear = dateTime.get(WeekFields.ISO.weekOfWeekBasedYear());
        int year = dateTime.getYear();

        // Create a key using the format "WW-yyyy" (e.g., "39-2023")
        return String.format("%02d-%04d", weekOfYear, year);
    }

    @GetMapping("/totalPaymentByYear")
    public ResponseEntity<?> getTotalPaymentByYear() {
        // Get the list of bills
        List<Bill> bills = repository.findAll();
        if (bills.isEmpty()) {
            return new ResponseEntity<>("Empty Bill List", HttpStatus.BAD_REQUEST);
        }

        Map<Integer, BigDecimal> yearlyTotalPayments = new LinkedHashMap<>();

        for (Bill bill : bills) {
            LocalDateTime dateTimeOrder = bill.getDateTimeOrder();
            int year = dateTimeOrder.getYear();

            // Calculate totalPayment for the year
            BigDecimal totalPayment = BigDecimal.valueOf(bill.getTotalPayment());
            yearlyTotalPayments.merge(year, totalPayment, BigDecimal::add);
        }

        List<YearlyTotalPaymentDTO> yearlyTotalPaymentDTOs = new ArrayList<>();

        for (Map.Entry<Integer, BigDecimal> entry : yearlyTotalPayments.entrySet()) {
            YearlyTotalPaymentDTO yearlyTotalPaymentDTO = new YearlyTotalPaymentDTO();
            yearlyTotalPaymentDTO.setYear(entry.getKey());
            yearlyTotalPaymentDTO.setTotalPayment(entry.getValue());

            yearlyTotalPaymentDTOs.add(yearlyTotalPaymentDTO);
        }

        return new ResponseEntity<>(yearlyTotalPaymentDTOs, HttpStatus.OK);
    }

    @GetMapping("/totalPaymentByHourOfDay")
    public ResponseEntity<List<HourlyTotalPaymentDTO>> getTotalPaymentByHourOfDay(
            @RequestParam("datetime") @DateTimeFormat(pattern = "HH:mm:ss dd/MM/yyyy") LocalDateTime dateTime) {

        // Lấy ngày và giờ hiện tại từ 00:00
        LocalDateTime startOfCurrentDay = dateTime.toLocalDate().atStartOfDay();
        int currentHour = dateTime.getHour();

        // Initialize a list to store HourlyTotalPaymentDTO
        List<HourlyTotalPaymentDTO> hourlyTotalPaymentDTOs = new ArrayList<>();

        // Iterate through the hours and calculate total payment for each hour
        for (int hour = 0; hour <= currentHour; hour++) {
            LocalDateTime hourStart = startOfCurrentDay.plusHours(hour);
            LocalDateTime hourEnd = startOfCurrentDay.plusHours(hour + 1);

            BigDecimal totalPaymentForHour = calculateTotalPaymentForHour(hourStart, hourEnd);

            HourlyTotalPaymentDTO dto = new HourlyTotalPaymentDTO();
            dto.setHour(String.format("%02d:00", hour));  // Format hour to "hh:00"
            dto.setTotalPayment(totalPaymentForHour);
            hourlyTotalPaymentDTOs.add(dto);
        }

        return new ResponseEntity<>(hourlyTotalPaymentDTOs, HttpStatus.OK);
    }

    // Helper method to calculate total payment for an hour
    private BigDecimal calculateTotalPaymentForHour(LocalDateTime hourStart, LocalDateTime hourEnd) {
        BigDecimal totalPaymentForHour = BigDecimal.ZERO;

        List<Bill> bills = repository.findByDateTimeOrderBetween(hourStart, hourEnd);
        for (Bill bill : bills) {
            totalPaymentForHour = totalPaymentForHour.add(BigDecimal.valueOf(bill.getTotalPayment()));
        }

        return totalPaymentForHour;
    }


//    @GetMapping("/totalPaymentByHourOfDay")
//    public ResponseEntity<List<HourlyTotalPaymentDTO>> getTotalPaymentByHourOfDay() {
//        // Get the current date and time
//        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.systemDefault());
//
//        // Lấy giờ hiện tại và chuyển thành giờ nguyên (ví dụ: 10:49 -> 10:00)
//        int currentHour = currentDateTime.getHour();
//
//        // Initialize a map to store total payments for each hour
//        Map<Integer, BigDecimal> hourlyTotalPayments = new HashMap<>();
//
//        // Iterate through the bills and calculate total payment for each hour
//        List<Bill> bills = repository.findAll();
//        for (Bill bill : bills) {
//            LocalDateTime dateTimeOrder = bill.getDateTimeOrder();
//            int hour = dateTimeOrder.getHour();
//            BigDecimal totalPayment = BigDecimal.valueOf(bill.getTotalPayment());
//
//            // Accumulate the total payment for the respective hour
//            hourlyTotalPayments.merge(hour, totalPayment, BigDecimal::add);
//        }
//
//        // Convert the map to a list of HourlyTotalPaymentDTO with formatted hours
//        List<HourlyTotalPaymentDTO> hourlyTotalPaymentDTOs = new ArrayList<>();
//        for (int i = 0; i <= currentHour; i++) {
//            HourlyTotalPaymentDTO dto = new HourlyTotalPaymentDTO();
//            dto.setHour(String.format("%02d:00", i));  // Format hour to "hh:00"
//            dto.setTotalPayment(hourlyTotalPayments.getOrDefault(i, BigDecimal.ZERO));
//            hourlyTotalPaymentDTOs.add(dto);
//        }
//
//        return new ResponseEntity<>(hourlyTotalPaymentDTOs, HttpStatus.OK);
//    }


}
