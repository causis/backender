package com.glovoapp.backender.api;

import com.glovoapp.backender.api.model.OrderVM;
import com.glovoapp.backender.model.Order;
import com.glovoapp.backender.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Controller
class OrderController {
    private final OrderService orderService;

    @RequestMapping("/orders")
    @ResponseBody
    List<OrderVM> orders() {
        return orderService
                .orders()
                .stream()
                .map(this::toOrderVM)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of orders that are available for a particular courier
     * Candidate note: Personally, I'd prefer to use this mapping to retrieve an order based on the id.
     * I'd push against this API and use /orders?availableForCourierId=:courierId, that's much more REST IMO
     */
    @RequestMapping("/orders/{courierId}")
    @ResponseBody
    List<OrderVM> orders(@PathVariable String courierId) {
        return orderService
                .ordersForCourier(courierId)
                .stream()
                .map(this::toOrderVM)
                .collect(Collectors.toList());
    }

    private OrderVM toOrderVM(Order order) {
        return new OrderVM(order.getId(), order.getDescription());
    }
}
