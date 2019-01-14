package com.glovoapp.backender.service;

import com.glovoapp.backender.model.Order;
import com.glovoapp.backender.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private OrderRepository orderRepository;

    public List<Order> orders() {
        return orderRepository.findAll();
    }

    public List<Order> ordersForCourier(String courierId) {
        //Temporary implementation
        return orderRepository.findAll();
    }
}
