package com.api19_4.api19_4.models;

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
@Table(name="ProductLike")
public class ProductLike {

    @Id
    @SequenceGenerator(
            name = "prodlike_sequence",
            sequenceName = "prodlike_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "prodlike_sequence"
    )
    private Long idProdLike;
    private String idUser;
    private String idProd;
    // Add this mapping to Product in ProductLike class
    @ManyToOne(fetch = FetchType.EAGER)  // Change FetchType to EAGER
    @JoinColumn(name = "idProd", insertable = false, updatable = false)
    private Product product;

}
