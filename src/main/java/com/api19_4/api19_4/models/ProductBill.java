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

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idProdBill")
@Table(name="ProductBill")
public class
ProductBill {

    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(
            name = "prodbill_sequence",
            sequenceName = "prodbill_sequence",
            allocationSize = 1 // increment by 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "prodbill_sequence"
    )
    private Long idProdBill;


    @ManyToOne
    @JoinColumn(name = "idBill")
    @JsonBackReference
    private Bill bill;
    private double totalPriceProd;
    private String status;
    private int discount;
    @ManyToOne
    @JoinColumn(name = "product_id")
//    @JsonBackReference
    private Product product;
    private int quantity;


}
