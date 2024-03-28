package com.api19_4.api19_4.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@Setter
@Getter
@Component
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="DateStatusChange")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idDateStatusChange")
public class DateStatusChange {
    @Id
    @SequenceGenerator(
            name = "dateStatusChange_sequence",
            sequenceName = "dateStatusChange_sequence",
            allocationSize = 1 // increment by 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "dateStatusChange_sequence"
    )
    private Long idDateStatusChange;
    private LocalDateTime dateTimeOrder;
    private String status;
    @ManyToOne
    @JoinColumn(name = "bill_id") // Đặt tên cột foreign key là "user_id" và unique = true để chỉ định mối quan hệ một-một
    private Bill bill;
    @ManyToOne
    @JoinColumn(name = "inventory_id") // Đặt tên cột foreign key là "user_id" và unique = true để chỉ định mối quan hệ một-một
    private InventoryCheck inventoryCheck;
}
