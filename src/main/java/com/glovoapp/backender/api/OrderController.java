package com.glovoapp.backender.api;

import com.glovoapp.backender.api.model.OrderVM;
import com.glovoapp.backender.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Controller
class OrderController {
    private final OrderRepository orderRepository;

    @RequestMapping("/orders")
    @ResponseBody
    List<OrderVM> orders() {
        return orderRepository.findAll()
                .stream()
                .map(order -> new OrderVM(order.getId(), order.getDescription()))
                .collect(Collectors.toList());
    }
}
