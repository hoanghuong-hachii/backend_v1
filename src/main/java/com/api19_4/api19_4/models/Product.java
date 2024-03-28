package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.List;
@Component
@Setter
@Getter
@AllArgsConstructor
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idProd")
@Table(name="Product")
public class Product {
    @Id
    private String idProd;

    @Column(name = "productName",columnDefinition = "nvarchar(1000)")
    private String productName;

// giá bán
    @Column(name = "retailPrice")
    private double retailPrice;

    @Column(name = "unitName",columnDefinition = "nvarchar(1000)")
    private String unitName;

    @Column(name = "quantityImported")
    private int quantityImported; // New field to track the quantity imported in a batch

    @Column(name = "orderQuantity")
    private int orderQuantity;

    @Column(name = "soldQuantity")
    private int soldQuantity;

    @Column(name = "quantity")
    private int quantity;

    @Column (name = "categoryName", columnDefinition = "nvarchar(1000)")
    private String categoryName;

    @Column(name = "coupons")
    private int coupons;

    @Column(name = "brand", columnDefinition = "nvarchar(1000)")
    private String brand;

    @Column(name = "origin", columnDefinition = "nvarchar(1000)")
    private String origin;

    @Column(name = "detail", columnDefinition = "nvarchar(1000)")
    private String detail;

    @Column(name = "unitPrice")
    private double unitPrice;

    private String imageAvatar;

    private String imageQR;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Batch> batches;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductStandard> productStandards;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PurchaseOrder> purchaseOrders;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToMany(mappedBy = "products")
    private List<Warehouse> warehouses; // Many-to-many relationship with warehouses

    @OneToOne
    @JoinColumn(name = "idProdCheck")
    private ProductCheck productCheck;

    public Product(IDGenerator idGenerator) {
        this.idProd = idGenerator.generateNextID();
    }
    public Product() {
        // Default constructor with no arguments
    }


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

    public String getId() {
        return idProd;
    }


}
