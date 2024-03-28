package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class PurchaseOrderDto {
    private String idPurchaseOrder;
    private String dateImport;
    private Long totalQuantity;
    private double totalPurchaseCost;
    private Integer discount;
    private double payableAmount;
    private double paidAmount;
    private double debt;
    private String note;
    private String status;
    private String orderInitiator;
    private List<UnitDTO> units;
    private List<ProductDto> productDtos;
    private String warehouse_id;
    private String nameWarehouse;
    private String supplier_id;
    private String nameSupplier;

}
