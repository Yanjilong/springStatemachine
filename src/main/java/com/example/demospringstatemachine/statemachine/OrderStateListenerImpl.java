package com.example.demospringstatemachine.statemachine;

import com.example.demospringstatemachine.constant.enums.OrderStatus;
import com.example.demospringstatemachine.constant.enums.OrderStatusChangeEvent;
import com.example.demospringstatemachine.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 状态转换监听：
 *
 * 无法使用@OnTransition 注释来创建状态和事件枚举。出于这个原因，需要使用字符串表示。
 *
 */
@Component("orderStateListener")
@WithStateMachine(name = "orderStateMachine")
@Slf4j
public class OrderStateListenerImpl {

    @Resource
    private StateMachine<OrderStatus, OrderStatusChangeEvent> orderStateMachine;

    // 支付
    @OnTransition(source = "WAIT_PAYMENT", target = "WAIT_DELIVER")
    public boolean payTransition(Message<OrderStatusChangeEvent> message) {

        //更新订单
        Order order = (Order) message.getHeaders().get("order");
        log.info("支付，状态机反馈信息：{}",  message.getHeaders());
        order.setStatus(OrderStatus.WAIT_DELIVER);
        //TODO 其他业务
        //成功 则为1
        orderStateMachine.getExtendedState().getVariables().put("err","payment->"+order.getId());

        return true;
    }

    // 发货
    @OnTransition(source = "WAIT_DELIVER", target = "WAIT_RECEIVE")
    public boolean deliverTransition(Message<OrderStatusChangeEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        order.setStatus(OrderStatus.WAIT_RECEIVE);
        log.info("发货，状态机反馈信息：{}",  message.getHeaders());
        return true;
    }

    // 收货
    @OnTransition(source = "WAIT_RECEIVE", target = "FINISH")
    public boolean receiveTransition(Message<OrderStatusChangeEvent> message){
        Order order = (Order) message.getHeaders().get("order");
        order.setStatus(OrderStatus.FINISH);
        log.info("收货，状态机反馈信息：{}", message.getHeaders());
        return true;
    }

    // 每次准换都会调用：
    @OnTransition
    public void anyTransition() {
        log.info("order state machine info:{}",orderStateMachine.getState());
        log.info("状态转换......");
    }

}

