package com.example.demospringstatemachine.constant.enums;

import lombok.Getter;

/**
 * 订单状态改变事件
 */

@Getter
public enum OrderStatusChangeEvent {
    //支付，发货，确认收货, 创建
    PAYED, DELIVERY, RECEIVED, CREATE;
}