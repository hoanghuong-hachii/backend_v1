package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CheckWarehouseDto {
    private String idWarehouse;
    private String name;
    private int totalQuantityInventory;
    private double totalInventoryValue;
    private double totalInventoryCost;

}
