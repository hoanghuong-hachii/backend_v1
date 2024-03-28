package com.api19_4.api19_4.models;

import com.api19_4.api19_4.generator.IDGenerator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idSupplier")
@Table(name = "Supplier")
public class Supplier {
    @Id
    private String idSupplier;
    @Column(columnDefinition = "nvarchar(1000)")
    private String name;
    private String numberPhone;
    @Column(columnDefinition = "nvarchar(1000)")
    private String address;
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<Product> products;
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<Batch> batches;

    public Supplier(IDGenerator idGenerator){
        this.idSupplier = idGenerator.generateNextID();
    }

}
