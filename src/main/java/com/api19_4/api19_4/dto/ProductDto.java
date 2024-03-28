package com.api19_4.api19_4.dto;

import com.api19_4.api19_4.models.Unit;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private String idProd;
    private String brand;
    private String origin;
    private String detail;
    private String productName;
    private double purchasePrice;
    private double unitPrice;
    private double retailPrice;
    private String unitName;
    private int quantityImported; // New field to track the quantity imported in a batch
    private int quantitySold;
    private int quantity;
    private int orderQuantity;
    private String categoryName;
    private String supplierName;
    private float coupons;
    private String imageAvatar;
    private String imageQR;

    public String getFormattedDiscountedPrice() {
        double discount = (100 - coupons) / 100.0; // Tính phần trăm giảm giá
        double discountedPrice = retailPrice * discount; // Giá sau khi áp dụng khuyến mãi

        DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Định dạng format tiền tệ
        return decimalFormat.format(discountedPrice); // Trả về giá sau khi khuyến mãi đã được định dạng tiền tệ
    }
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "#,###")
    public String getFormattedPrice() {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(retailPrice);
    }



}
