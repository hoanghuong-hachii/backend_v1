package com.api19_4.api19_4.services;

import com.api19_4.api19_4.models.Orderr;
import com.api19_4.api19_4.repositories.OrderRepository;
import com.api19_4.api19_4.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements  OrderServices{
    @Autowired
    private OrderRepository productRepository;
    @Override
    public Orderr createOrder(Orderr orderr) {
        return null;
    }
}
