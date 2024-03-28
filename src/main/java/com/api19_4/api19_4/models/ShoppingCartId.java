package com.api19_4.api19_4.models;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ShoppingCartId {
    private Long idUser;
    private Long idProd;
}
