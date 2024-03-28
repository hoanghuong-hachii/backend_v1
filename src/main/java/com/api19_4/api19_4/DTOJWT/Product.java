package com.api19_4.api19_4.DTOJWT;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    private int productId;
    private String name;
    private int qty;
    private double price;
}