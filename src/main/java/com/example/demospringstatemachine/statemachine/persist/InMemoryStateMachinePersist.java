package com.example.demospringstatemachine.statemachine.persist;

import com.example.demospringstatemachine.constant.enums.OrderStatus;
import com.example.demospringstatemachine.constant.enums.OrderStatusChangeEvent;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 在内存中持久化状态机
 */
@Component
public class InMemoryStateMachinePersist implements StateMachinePersist<OrderStatus, OrderStatusChangeEvent, String> {

	private final Map<String, StateMachineContext<OrderStatus, OrderStatusChangeEvent>> map =
			new HashMap<>();
	
	@Override
	public void write(StateMachineContext<OrderStatus, OrderStatusChangeEvent> context, String contextObj) throws Exception {
		map.put(contextObj, context);
	}

	@Override
	public StateMachineContext<OrderStatus, OrderStatusChangeEvent> read(String contextObj) throws Exception {
		return map.get(contextObj);
	}

}