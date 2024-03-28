package com.api19_4.api19_4.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Setter
@Getter
public class BatchDTO {
    private String idBatch;
    private String name;
    private LocalDateTime dateImport;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expirationDate;
    private List<UnitDTO> units;
    private String warehouse_id;
    private String supplier_id;
}
