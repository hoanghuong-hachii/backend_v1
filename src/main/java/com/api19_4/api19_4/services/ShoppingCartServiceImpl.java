package com.api19_4.api19_4.services;

import com.api19_4.api19_4.models.ShoppingCart;
import com.api19_4.api19_4.models.ShoppingCartId;
import com.api19_4.api19_4.repositories.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartServices {
    private final ShoppingCartRepository shoppingCartRepository;

    @Autowired
    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
    }

    @Override
    public List<ShoppingCart> getAllShoppingCarts() {
        return shoppingCartRepository.findAll();
    }


    @Override
    public Optional<ShoppingCart> findById(String id) {
        return shoppingCartRepository.findById(id);
    }

    @Override
    public ShoppingCart createShoppingCart(ShoppingCart shoppingCart) {
        return shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public Optional<ShoppingCart> getShoppingCartById(String id) {
        return shoppingCartRepository.findById(id);
    }
    // Các phương thức xử lý khác của ShoppingCartServices
}
