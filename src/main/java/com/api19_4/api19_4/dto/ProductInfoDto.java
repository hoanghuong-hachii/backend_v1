package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ProductInfoDto {
    private String idProd;
    private String productName;
    private String unitName;
    private Map<String, Integer> warehouseQuantities; // Map tên kho và số tồn kho ứng với mỗi kho
}

