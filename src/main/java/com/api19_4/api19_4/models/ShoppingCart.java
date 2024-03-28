package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.ShoppingCartId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCart {

    //    private ShoppingCartId id;
    @Id
    private String idShCart;
    private String idUser;
    private String idProd;
    private int quantityProd;
    private Double price;
    private Double totalPrice;

    public ShoppingCart(IDGenerator idGenerator){
        this.idShCart = idGenerator.generateNextID();
    }
}
