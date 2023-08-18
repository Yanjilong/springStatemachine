package com.example.demospringstatemachine.controller;

import cn.hutool.json.JSONUtil;
import com.example.demospringstatemachine.entity.Order;
import com.example.demospringstatemachine.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author yanjl
 * @ClassName StateMachineController
 * @Description TODO
 * @Date: 2023/7/25 11:11
 */
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class StateMachineController {

    private final IOrderService orderService;

    @GetMapping("/create/{id}")
    public String createOrder(@PathVariable String id) throws Exception {
        return orderService.create(id);
    }

    /**
     * 根据id查询订单
     *
     * @return
     */
    @GetMapping("/orders")
    public String getById() {
        // 查询订单
        Map<String, Order> order = orderService.getOrders();

        return JSONUtil.toJsonStr(order);
    }

    /**
     * 对订单进行支付
     *
     * @param id
     * @return
     */
    @GetMapping("/pay/{id}")
    public Order pay(@PathVariable("id") String id) {
        //对订单进行支付
        return orderService.pay(id);
    }

    /**
     * 对订单进行发货
     *
     * @param id
     * @return
     */
    @GetMapping("/deliver/{id}")
    public Order deliver(@PathVariable("id") String id) {
        //对订单进行确认收货
        return orderService.deliver(id);
    }
    /**
     * 对订单进行确认收货
     *
     * @param id
     * @return
     */
    @GetMapping("/receive/{id}")
    public Order receive(@PathVariable("id") String id) {
        //对订单进行确认收货
        return orderService.receive(id);
    }


}