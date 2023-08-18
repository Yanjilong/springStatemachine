package com.example.demospringstatemachine.statemachine.persist;

import com.example.demospringstatemachine.constant.enums.OrderStatus;
import com.example.demospringstatemachine.constant.enums.OrderStatusChangeEvent;
import com.example.demospringstatemachine.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;


/**
 * 伪持久化：实现持久化接口，实现write 和read 方法
 *
 */
@Component
@Slf4j
public class OrderStateMachinePersist implements StateMachinePersist<OrderStatus, OrderStatusChangeEvent, Order> {

    // 写入持久化
    @Override
    public void write(StateMachineContext<OrderStatus, OrderStatusChangeEvent> context, Order contextObj) throws Exception {
        //这里不做任何持久化工作

    }

    // 读状态机，这里可以返回一个 任意状态 的状态机，（伪持久化）
    @Override
    public StateMachineContext<OrderStatus, OrderStatusChangeEvent> read(Order order) throws Exception {
        StateMachineContext<OrderStatus, OrderStatusChangeEvent> result =
                new DefaultStateMachineContext<OrderStatus, OrderStatusChangeEvent>(order.getStatus(),
                null, null, null, null, "refundReasonMachine");

        return result;
    }
}
