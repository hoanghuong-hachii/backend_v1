package com.api19_4.api19_4.dto;

import com.api19_4.api19_4.models.Product;
import com.api19_4.api19_4.models.Unit;
import com.api19_4.api19_4.models.Warehouse;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Setter
@Getter
public class ProductStandardDTO {
    private String idProductStandard;
    private String name;
    private LocalDateTime dateImport;
    private List<UnitDTO> units;
    private String warehouse_id;
    private String nameWarehouse;
    private String supplier_id;
    private String nameSupplier;
    private double totalPrice;
    private String status;
    private String note;
}
