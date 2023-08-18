package com.example.demospringstatemachine.constant.enums;

import lombok.Getter;

/**
 * 订单状态
 */

@Getter
public enum OrderStatus {
    //待支付，待发货，待收货，订单结束
    WAIT_PAYMENT, WAIT_DELIVER, WAIT_RECEIVE, FINISH;
}



