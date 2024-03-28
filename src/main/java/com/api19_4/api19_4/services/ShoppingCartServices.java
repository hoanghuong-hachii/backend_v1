package com.api19_4.api19_4.services;

import com.api19_4.api19_4.models.ShoppingCart;
import com.api19_4.api19_4.models.ShoppingCartId;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartServices {
    List<ShoppingCart> getAllShoppingCarts();

    Optional<ShoppingCart> findById(String idUser);

    ShoppingCart createShoppingCart(ShoppingCart shoppingCart);

    Optional<ShoppingCart> getShoppingCartById(String id);
}
