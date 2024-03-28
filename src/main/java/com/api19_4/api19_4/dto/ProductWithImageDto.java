package com.api19_4.api19_4.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductWithImageDto {
    private String idProd;
    private String brand;
    private String origin;
    private String detail;
    private String productName;
    private double price;
    private String categoryName;
    private float coupons;
    private int initialorderquantity;
    private String weight;
    private String imageAvatar;
    private String imageDetail;
}
