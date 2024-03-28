package com.api19_4.api19_4.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ImageProductAvatar")
public class ImageProductAvatar {
    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(
            name = "ImageProductAvatar_sequence",
            sequenceName = "ImageProductAvatar_sequence",
            allocationSize = 1 // increment by 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ImageProductAvatar_sequence"
    )
    private Long idImageAvatar;
    private String imageName;
    private Long idProd;
    private  String imageAvatar;
    private String imageDetail;
}
