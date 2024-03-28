package com.api19_4.api19_4.models;

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
@Table(name = "ImageProductDetail")
public class ImageProductDetail {
    @Id
    @SequenceGenerator(
            name = "ImageProductDetail_sequence",
            sequenceName = "ImageProductDetail_sequence",
            allocationSize = 1 // increment by 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ImageProductDetail_sequence"
    )
    private Long idImageDetail;
    private String imageName;
    private Long idProd;
    private List<String> imageDetail;
}
