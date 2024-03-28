package com.api19_4.api19_4.services;

import com.api19_4.api19_4.dto.ProductDto;
import com.api19_4.api19_4.dto.SearchProductRequest;
import com.api19_4.api19_4.models.Product;
import com.api19_4.api19_4.repositories.ProductRepository;
import com.api19_4.api19_4.util.SearchUtil;

import com.api19_4.api19_4.validator.Validator;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class ProServiceImpl implements ProductServices {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Page<ProductDto> advanceSearch(@Valid SearchProductRequest searchProductRequest, Pageable pageable) throws Exception {
        if (searchProductRequest != null) {
            List<Specification<Product>> specList = getAdvanceSearchSpecList(searchProductRequest);
            if (!specList.isEmpty()) {
                Specification<Product> spec = specList.get(0);
                for (int i = 1; i < specList.size(); i++) {
                    spec = spec.and(specList.get(i));
                }
                Page<Product> page = productRepository.findAll(spec, pageable);
                List<ProductDto> ls = new ArrayList<>();
                for (Product product : page) {
                    ProductDto productDto = new ProductDto();
                    BeanUtils.copyProperties(product, productDto);
                    ls.add(productDto);
                }
                long totalElements = page.getTotalElements();
                return new PageImpl<>(ls, page.getPageable(), totalElements);
            }
        }
        return findAll(pageable);
    }

    @Override
    public Page<ProductDto> advanceSearchP(@Valid SearchProductRequest searchProductRequest, Pageable pageable) throws Exception {
        if (searchProductRequest != null) {
            List<Specification<Product>> specList = getAdvanceSearchSpecListPrice(searchProductRequest);
            if (!specList.isEmpty()) {
                Specification<Product> spec = specList.get(0);
                for (int i = 1; i < specList.size(); i++) {
                    spec = spec.and(specList.get(i));
                }
                Page<Product> page = productRepository.findAll(spec, pageable);
                List<ProductDto> ls = new ArrayList<>();
                for (Product product : page) {
                    ProductDto productDto = new ProductDto();
                    BeanUtils.copyProperties(product, productDto);
                    ls.add(productDto);
                }
                long totalElements = page.getTotalElements();
                return new PageImpl<>(ls, page.getPageable(), totalElements);
            }
        }
        return findAll(pageable);
    }



    @Override
    public Page<ProductDto> findAll(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        List<ProductDto> ls = new ArrayList<>();
        for (Product u : page) {
            ProductDto productDto = new ProductDto();
            BeanUtils.copyProperties(u, productDto);
            ls.add(productDto);
        }
        long totalElements = page.getTotalElements();
        return new PageImpl<>(ls, page.getPageable(), totalElements);
    }


    private List<Specification<Product>> getAdvanceSearchSpecList(@Valid SearchProductRequest s) {
        List<Specification<Product>> specList = new ArrayList<>();
        // advance
        if (Validator.isHaveDataString(s.getProductName())) {
            specList.add(SearchUtil.like("productName", "%" + s.getProductName() + "%"));
        }else if(Validator.isHaveDataString(s.getCategoryName())){
            specList.add(SearchUtil.like("categoryName","%" + s.getCategoryName() + "%" ));
        }
        return specList;
    }
    private List<Specification<Product>> getAdvanceSearchSpecListPrice(@Valid SearchProductRequest s) {
        List<Specification<Product>> list = new ArrayList<>();
        if(Validator.isRange(s.getPriceFrom(), s.getPriceTo())){

//            list.add(priceSpec);
        }
        return list;
    }


}
