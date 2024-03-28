package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idProdCheck")
@Table(name="ProductCheck")
public class ProductCheck {
    @Id
    private String idProdCheck;
    private int quantityActual;
    private int quantityDeviation;
    private int quantityInventory;
    private double valueDeviation;
    @OneToOne
    @JoinColumn(name = "idProd")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "idInventoryCheck")
    @JsonBackReference
    private InventoryCheck inventoryCheck;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    //  @JsonBackReference
    private Warehouse warehouse;
    public ProductCheck(IDGenerator idGenerator){
        this.idProdCheck = idGenerator.generateNextID();
    }
}
