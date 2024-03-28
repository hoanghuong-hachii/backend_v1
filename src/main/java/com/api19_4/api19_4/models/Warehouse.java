package com.api19_4.api19_4.models;

import com.api19_4.api19_4.dto.ProductDto;
import com.api19_4.api19_4.generator.IDGenerator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idWarehouse")
@Table(name = "Warehouse")
public class Warehouse {
    @Id
    private String idWarehouse;
    @Column(columnDefinition = "nvarchar(1000)")
    private String name;
    @Column(columnDefinition = "nvarchar(1000)")
    private String address;
    @Column(columnDefinition = "nvarchar(1000)")
    private String information;
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<Batch> batches;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<ProductStandard> productStandards;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<PurchaseOrder> purchaseOrders;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<ProductCheck> productChecks;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<Unit> units;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "warehouse_product",
            joinColumns = @JoinColumn(name = "warehouse_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products; // Many-to-many relationship with products

    public Warehouse(IDGenerator idGenerator){
        this.idWarehouse = idGenerator.generateNextID();
    }
}

//tạo id tự động
////    @GeneratedValue(strategy = GenerationType.AUTO)
//    @SequenceGenerator(
//            name = "warehouse_sequence",
//            sequenceName = "warehouse_sequence",
//            allocationSize = 1 // increment by 1
//    )
//    @GeneratedValue(
//            strategy = GenerationType.SEQUENCE,
//            generator = "warehouse_sequence"
//    )