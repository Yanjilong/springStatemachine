package com.example.demospringstatemachine.entity;

import com.example.demospringstatemachine.constant.enums.OrderStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;


@Data
@JsonSerialize
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private OrderStatus status;
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "[订单号：" + id + ", 订单状态：" + status + "]";
    }
}

