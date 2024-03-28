package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idBatch")
@Table(name = "batch")
public class Batch {
    @Id
    private String idBatch;
    private String name;
    private LocalDateTime dateImport;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expirationDate;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL)
    private List<Unit> units;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    public Batch(IDGenerator idGenerator){
        this.idBatch = idGenerator.generateNextID();
    }
    // Getter cho dateImport, để trả về dưới dạng chuỗi đã định dạng
    public String getDateImportFormatted() {
        // Định dạng ngày giờ và ngày tháng theo yêu cầu
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return dateImport.format(formatter);
    }
    public String getManufacturingDateFormatted() {
        // Định dạng ngày giờ và ngày tháng theo yêu cầu
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return manufacturingDate.format(formatter);
    }
    public String getExpirationDateFormatted() {
        // Định dạng ngày giờ và ngày tháng theo yêu cầu
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return expirationDate.format(formatter);
    }
}
