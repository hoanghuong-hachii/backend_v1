package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class WareDTO {
    private String name;
    private String address;
    private String information;
    private List<ProductDto> productDtos;
}
