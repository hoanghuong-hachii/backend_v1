package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUnit")
@Table(name = "unit")
public class Unit {
    @Id
    private String idUnit;
    @Column(name = "product_id")
    private String productId;
    @Column(columnDefinition = "nvarchar(1000)")
    private String unitName;
    private int quantity;
    private double purchasePrice; // Giá nhập của đơn vị
    private double unitPrice; // Giá của mỗi hộp
    private int soldQuantity;
    private int quantityImport;
    private int orderQuantity;
    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne
    @JoinColumn(name = "productStandard_id")
    private ProductStandard productStandard;

    @ManyToOne
    @JoinColumn(name = "purchaseOrder")
    private PurchaseOrder purchaseOrder;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    //  @JsonBackReference
    private Warehouse warehouse;
    @Transient // Use @Transient annotation to indicate that this property is not mapped to the database
    public int getStockQuantity() {
        return quantity - soldQuantity;
    }

    public Unit(IDGenerator idGenerator){
        this.idUnit = idGenerator.generateNextID();
    }
}
