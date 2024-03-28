package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idPurchaseOrder")
@Table(name = "PurchaseOrder")
public class PurchaseOrder {
    @Id
    private String idPurchaseOrder;
    private LocalDateTime dateImport;
    private Long totalQuantity;
    private double totalPurchaseCost;
    private Integer discount;
    private double payableAmount;
    private double paidAmount;
    private String note;
    private String status;
    private String orderInitiator;

    // Getter cho dateImport, để trả về dưới dạng chuỗi đã định dạng
    public String getDateImportFormatted() {
        // Định dạng ngày giờ và ngày tháng theo yêu cầu
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return dateImport.format(formatter);
    }
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    private List<Unit> units;

    @ManyToOne
    @JoinColumn(name = "product_id")
//    @JsonBackReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    //  @JsonBackReference
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    public PurchaseOrder (IDGenerator idGenerator){
        this.idPurchaseOrder = idGenerator.generateNextID();
    }
}
