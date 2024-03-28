package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="orderr")
public class Orderr {
    @Id
    private String idOrder;

    @Column(name = "idUser")
    private String idUser;

    @Column(name = "idProd")
    private String idProd;

    @Column(name = "quantity")
    private int quantity;

    @Transient
    private double totalPriceProd;

    @Column(name = "dateOrder")
    private LocalDateTime dateOrder;

    @Column(name = "address", columnDefinition = "nvarchar(1000)")
    private String address;

    @Column(name = "payment")
    private double payment;

    public Orderr(IDGenerator idGenerator){
        this.idOrder = idGenerator.generateNextID();
    }
    // Getter cho dateImport, để trả về dưới dạng chuỗi đã định dạng
    public String getDateImportFormatted() {
        // Định dạng ngày giờ và ngày tháng theo yêu cầu
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return dateOrder.format(formatter);
    }

}
