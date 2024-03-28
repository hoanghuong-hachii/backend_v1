package com.api19_4.api19_4.services;

import com.api19_4.api19_4.dto.ProductDto;
import com.api19_4.api19_4.dto.SearchProductRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductServices {
    Page<ProductDto> findAll(Pageable pageable) throws Exception;
    Page<ProductDto> advanceSearch(@Valid SearchProductRequest searchProductRequest, Pageable pageable) throws Exception ;

    Page<ProductDto> advanceSearchP(@Valid SearchProductRequest searchProductRequest, Pageable pageable) throws Exception ;
}
