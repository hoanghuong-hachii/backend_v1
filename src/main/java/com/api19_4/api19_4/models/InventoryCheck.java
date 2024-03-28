package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idInventoryCheck")
@Table(name = "InventoryCheck")
public class InventoryCheck {
    @Id
    private String idInventoryCheck;
    private LocalDateTime timeCheck;
    private LocalDateTime balanceDate;
    private int  actualQuantity;
    private double totalActualValue;
    private int totalQuantityDeviation;
    private int increasedDeviation;
    private int decreasedDeviation;
    private String note;
    private String status;
    @OneToMany(mappedBy = "inventoryCheck", cascade = CascadeType.ALL)
    private List<ProductCheck> productChecks;
    public InventoryCheck(IDGenerator idGenerator){
        this.idInventoryCheck = idGenerator.generateNextID();
    }
}
