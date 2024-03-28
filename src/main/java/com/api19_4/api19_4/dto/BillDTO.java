package com.api19_4.api19_4.dto;

import com.api19_4.api19_4.models.Bill;
import com.api19_4.api19_4.models.ProductBill;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BillDTO {
    private Long idBill;
//    private String idCustomer;
    private String idUser;
    private String userName;
    private String numberPhoneCustomer;
    private String addressCustomer;
    private String dateTimeOrder;
    private String status;
    private double totalPayment;
//    private Integer discount;
    private double payableAmount;
    private double shippingFee;
    private String note;
    private List<ProductBillDTO> productBillDTOS;
//    public BillDTO(Bill bill) {
//        this.idUser = bill.getUser().getIdUser(); // Assuming there's a user associated with the bill
//        this.numberPhoneCustomer = bill.getNumberPhoneCustomer();
//        this.addressCustomer = bill.getAddressCustomer();
//        this.dateTimeOrder = bill.getDateTimeOrder();
//        this.status = bill.getStatus();
//        this.totalPayment = bill.getTotalPayment();
//        this.productBillDTOS = bill.getProductBills().stream()
//                .map(productBill -> new ProductBillDTO(
//                        productBill.getQuantity(),
//                        productBill.getTotalPriceProd(),
//                        productBill.getProduct().getId()
//                ))
//                .collect(Collectors.toList());
//    }
}
