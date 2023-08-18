package com.example.demospringstatemachine.config;

import com.example.demospringstatemachine.constant.enums.OrderStatus;
import com.example.demospringstatemachine.constant.enums.OrderStatusChangeEvent;
import com.example.demospringstatemachine.entity.Order;
import com.example.demospringstatemachine.statemachine.persist.OrderStateMachinePersist;
import com.example.demospringstatemachine.statemachine.action.OrderServiceActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.redis.RedisStateMachineContextRepository;
import org.springframework.statemachine.redis.RedisStateMachinePersister;
import javax.annotation.Resource;
import java.io.Serializable;
import java.util.EnumSet;

/**
 * 订单状态机配置,
 *
 * 这里使用状态机适配器配置状态机，还可以通过状态机工厂创建
 */
@Configuration
@EnableStateMachine(name = "orderStateMachine")
public class OrderStateMachineConfig extends
        StateMachineConfigurerAdapter<OrderStatus, OrderStatusChangeEvent> implements Serializable {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Resource
    private OrderServiceActionService actionService;


    /**
     * 配置状态机初始状态
     *
     * @param states
     * @throws Exception
     */
    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderStatusChangeEvent> states) throws Exception {
        states
                .withStates()
                .initial(OrderStatus.WAIT_PAYMENT,action())
                .state(OrderStatus.FINISH,actionService.finishActions())
                .end(OrderStatus.FINISH)
                .states(EnumSet.allOf(OrderStatus.class));
    }
 
    /**
     * 配置状态转换事件关系
     *
     * @param transitions
     * @throws Exception
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderStatusChangeEvent> transitions) throws Exception {
        transitions

                .withExternal().source(OrderStatus.WAIT_PAYMENT).target(OrderStatus.WAIT_DELIVER)
                .event(OrderStatusChangeEvent.PAYED) // 待支付-> 待发货
                .and()
                .withExternal().source(OrderStatus.WAIT_DELIVER).target(OrderStatus.WAIT_RECEIVE)
                .event(OrderStatusChangeEvent.DELIVERY) // 待发货 -> 待收货
                .and()
                .withExternal().source(OrderStatus.WAIT_RECEIVE).target(OrderStatus.FINISH)
                .event(OrderStatusChangeEvent.RECEIVED); // 待收货 -> 完成
    }


    @Bean
    public Action<OrderStatus, OrderStatusChangeEvent> action(){
        return new Action<OrderStatus, OrderStatusChangeEvent>() {
            @Override
            public void execute(StateContext<OrderStatus, OrderStatusChangeEvent> stateContext) {
                System.out.println("hello, this is action ");
            }
        };
    }

    @Bean
    public Action<OrderStatus, OrderStatusChangeEvent> finishAction(){
        return new Action<OrderStatus, OrderStatusChangeEvent>() {
            @Override
            public void execute(StateContext<OrderStatus, OrderStatusChangeEvent> stateContext) {
                System.out.println("state machine is over...");
            }
        };
    }

    /**
     * 注入RedisStateMachinePersister对象
     *
     * @return
     */
    @Bean(name = "orderRedisPersister")
    public RedisStateMachinePersister<OrderStatus, OrderStatusChangeEvent> redisPersister() {
        return new RedisStateMachinePersister<>(redisPersist());
    }

    /**
     * 通过redisConnectionFactory创建StateMachinePersist
     * redis持久化
     * @return
     */
    public StateMachinePersist<OrderStatus, OrderStatusChangeEvent, String> redisPersist() {

        RedisStateMachineContextRepository<OrderStatus, OrderStatusChangeEvent> repository =
                new RedisStateMachineContextRepository<>(redisConnectionFactory);

        return new RepositoryStateMachinePersist<>(repository);
    }

    @Autowired
    private OrderStateMachinePersist orderStateMachinePersist;

    // 注入自定义持久化策略(伪持久化)
    @Bean(name="orderPersister")
    public StateMachinePersister<OrderStatus, OrderStatusChangeEvent, Order> orderPersister() {
        return new DefaultStateMachinePersister<>(orderStateMachinePersist);
    }



}

