package com.example.demospringstatemachine.statemachine.action;

import com.example.demospringstatemachine.constant.enums.OrderStatus;
import com.example.demospringstatemachine.constant.enums.OrderStatusChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;

import javax.swing.*;

/**
 * @author yanjl
 * @ClassName OrderServiceActionSevice
 * @Description TODO
 * @Date: 2023/8/14 16:27
 */

@Service
@Slf4j
public class OrderServiceActionService {


    public Action<OrderStatus, OrderStatusChangeEvent> finishActions(){

        Action<OrderStatus, OrderStatusChangeEvent> action1 = new Action<OrderStatus, OrderStatusChangeEvent>(){

            @Override
            public void execute(StateContext stateContext) {
                // todo:
                log.info("this is finish action;");
            }
        };


        return action1;
    }
}