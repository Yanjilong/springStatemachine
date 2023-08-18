package com.example.demospringstatemachine.service;

import com.example.demospringstatemachine.entity.Order;

import java.util.List;
import java.util.Map;

public interface IOrderService {
    //创建新订单
    String create(String id) throws Exception;
    //发起支付
    Order pay(String id);
    //订单发货
    Order deliver(String id);
    //订单收货
    Order receive(String id);
    //获取所有订单信息
    Map<String, Order> getOrders();
}

