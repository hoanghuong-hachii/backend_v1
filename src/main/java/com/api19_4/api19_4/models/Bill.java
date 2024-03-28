package com.api19_4.api19_4.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Setter
@Getter

@Component
@AllArgsConstructor
@Entity
@Table(name="Bill")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idBill")
public class Bill {

    @Id
    @SequenceGenerator(
            name = "bill_sequence",
            sequenceName = "bill_sequence",
            allocationSize = 1 // increment by 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "bill_sequence"
    )
    private Long idBill;
    private String numberPhoneCustomer;
    @Column (name = "addressCustomer", columnDefinition = "nvarchar(1000)")
    private String addressCustomer;
    private LocalDateTime dateTimeOrder;
    private String status;
    private double totalPayment;
   // private Integer discount;
    private double payableAmount;
    private double shippingFee;
    private String note;
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
    private List<ProductBill> productBills;
    @ManyToOne
    @JoinColumn(name = "idUser") // Đặt tên cột foreign key là "user_id" và unique = true để chỉ định mối quan hệ một-một
    private UserInfo user;
    public List<ProductBill> getProductBills() {
        return productBills != null ? productBills : Collections.emptyList();
    }
    public Bill() {

    }

}
