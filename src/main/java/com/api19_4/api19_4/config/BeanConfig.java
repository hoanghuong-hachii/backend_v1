package com.api19_4.api19_4.config;

import com.api19_4.api19_4.dto.BillDTO;
import com.api19_4.api19_4.dto.ProductBillDTO;
import com.api19_4.api19_4.dto.ProductCheckDto;
import com.api19_4.api19_4.dto.ProductDto;
import com.api19_4.api19_4.models.Bill;
import com.api19_4.api19_4.models.Product;
import com.api19_4.api19_4.models.ProductBill;
import com.api19_4.api19_4.models.ProductCheck;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BeanConfig {
//    @Bean
//    public ModelMapper modelMapper() {
//        return new ModelMapper();
//    }
@Bean
public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();

    // Mapping configuration for ProductBill to ProductBillDTO
   // modelMapper.createTypeMap(ProductBill.class, ProductBillDTO.class)
         //   .addMapping(ProductBill::getQuantity, ProductBillDTO::setQuantity);



    // Mapping configuration for Bill to BillDTO
    modelMapper.createTypeMap(Bill.class, BillDTO.class)
            .addMapping(Bill::getProductBills, BillDTO::setProductBillDTOS);

    return modelMapper;
}
    @Bean
    public ModelMapper modelMapperProduct() {
        ModelMapper modelMapperProd = new ModelMapper();

        // Mapping configuration for Product to ProductDTO
        modelMapperProd.createTypeMap(Product.class, ProductDto.class)
                .addMapping(Product::getIdProd, ProductDto::setIdProd)
                .addMapping(Product::getProductName, ProductDto::setProductName)
                .addMapping(Product::getCategoryName, ProductDto::setCategoryName)
                .addMapping(Product::getRetailPrice, ProductDto::setRetailPrice)
                .addMapping(Product::getUnitName, ProductDto::setUnitName)
                .addMapping(Product::getCoupons, ProductDto::setCoupons)
                .addMapping(Product::getImageQR, ProductDto::setImageQR)
                .addMapping(Product::getImageAvatar, ProductDto::setImageAvatar);

        return modelMapperProd;
    }

}
