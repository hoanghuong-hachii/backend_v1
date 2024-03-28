package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductCheckDto {
    private String idProd;
    private String productName;
    private String unitName;
    private double unitPrice;
    private int quantityActual;
    private int quantityDeviation;
    private int quantityInventory;
    private double totalInventoryCost;
    private double totalInventoryValue;
    private String nameWarehouse;
}
