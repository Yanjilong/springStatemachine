package com.example.demospringstatemachine.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.demospringstatemachine.constant.enums.OrderStatus;
import com.example.demospringstatemachine.constant.enums.OrderStatusChangeEvent;
import com.example.demospringstatemachine.entity.Order;
import com.example.demospringstatemachine.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 业务功能
 *
 */
@Service("orderService")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class OrderServiceImpl implements IOrderService {

    // 自定义状态机
    private final StateMachine<OrderStatus, OrderStatusChangeEvent> orderStateMachine;

    // 状态机前缀
    private final static String MACHINE_PRE = "MACHINE:";

    // 数据前缀
    private final static String DATA_PRE = "orders";

    @Resource(name = "strRedisTemplatec")
    private RedisTemplate<String, String> strRedisTemplatec;

    /**
     * redis 持久化
     */
    @Resource(name = "orderRedisPersister")
    private RedisStateMachinePersister<OrderStatus, OrderStatusChangeEvent> orderRedisPersister;

    /**
     * 自定义持久化
     */
    @Resource(name = "orderPersister")
    private StateMachinePersister<OrderStatus, OrderStatusChangeEvent,Order> orderPersister;

    // 保存订单信息
    private Map<String, Order> orders = new HashMap<>();


    /**
     * 创建订单
     *
     * 初始状态： 待支付
     *
     * @param id
     * @return
     * @throws Exception
     */
    public String create(String id) throws Exception {

        // 初始化订单信息
        getOrderByCache();

        if (orders.containsKey(id)){
            return "订单号已存在";
        }

        Order order = new Order();
        order.setStatus(OrderStatus.WAIT_PAYMENT);
        order.setId(id);
        orders.put(order.getId(), order);


        strRedisTemplatec.opsForHash().put(DATA_PRE,id,JSONUtil.toJsonStr(order));
        return order.toString();
    }

    /**
     * 支付
     *
     * 支付 -> 待发货
     *
     * @param id
     * @return
     */
    @SneakyThrows
    public Order pay(String id) {
        Order order = orders.get(id);
        log.info("[ 尝试支付，订单号：{}]" , id);
        Message message = MessageBuilder.withPayload(OrderStatusChangeEvent.PAYED).
setHeader("order", order).build();
        if (!sendEvent(message, order)) {
            log.info(" ###支付失败, 状态异常，订单号：{}###" , id);
        }
        return orders.get(id);
    }

    /**
     * 发货
     *
     * 代发货 -> 待收货
     *
     * @param id
     * @return
     */
    @SneakyThrows
    public Order deliver(String id) {
        Order order = orders.get(id);
        Message<OrderStatusChangeEvent> message = MessageBuilder.withPayload(OrderStatusChangeEvent.DELIVERY)
                .setHeader("order", order).build();
        log.info("[ 尝试发货，订单号：{}]msg:[{}]" , id, JSONUtil.toJsonStr(message));
        if (!sendEvent(message, order)) {
            log.info("###发货失败，状态异常，订单号：{}###", id);
        }

        return order;
    }

    /**
     * 收货
     *
     *  待收货 -> 完成
     *
     * @param id
     * @return
     */
    @SneakyThrows
    public Order receive(String id) {
        Order order = orders.get(id);
        log.info("[ 尝试收货，订单号：{}]", id);

        Message<OrderStatusChangeEvent> message = MessageBuilder.withPayload(OrderStatusChangeEvent.RECEIVED)
                .setHeader("order", order).build();
        if (!sendEvent(message, order)) {
            log.info(" 收货失败，状态异常，订单号：{};", id);
        }
        return order;
    }


    /**
     * 获取订单列表
     *
     * @return
     */
    public Map<String, Order> getOrders() {


        getOrderByCache();
        return orders;
    }

    // 获取订单暂存信息
    private Map<String, Order> getOrderByCache(){

        // 读取缓存中的订单
        Object o = strRedisTemplatec.opsForHash().entries(DATA_PRE);

        JSONObject object = JSONUtil.parseObj(o);
        Map<String, String> orderMap = JSONUtil.toBean(object,Map.class);

        orders = orderMap.values().stream().map(k -> JSONUtil.toBean(k,Order.class))
                .collect(Collectors.toMap(Order::getId,k -> k));

        return orders;
    }
 
 
    /**
     * 发送订单状态转换事件
     *
     * Message： 不是spirng statemachine专属的，它是spring里面通用的一种消息工具
     *
     * @param message
     * @param order
     * @return
     */
    //@SneakyThrows
    private synchronized boolean sendEvent(Message<OrderStatusChangeEvent> message, Order order) throws Exception {
        boolean result = false;
        try {
            orderStateMachine.start();
            //尝试恢复状态机状态
            orderRedisPersister.restore(orderStateMachine, MACHINE_PRE + order.getId());
            if (order.getStatus().equals(OrderStatus.FINISH)){
                // 订单已完成，删除redis
                strRedisTemplatec.delete(MACHINE_PRE + order.getId());
                return false;
            }
            // 未获取到状态机，认为状态错误
            if (orderStateMachine == null){
                log.error("状态机输入状态错误");
                return false;
            }

            log.info(" msg info：[{}];", JSONUtil.toJsonStr(message));
            result = orderStateMachine.sendEvent(message);

            //获取到监听的结果信息
            String o = (String)orderStateMachine.getExtendedState().getVariables().get("err");
            //操作完成之后,删除本次对应的key信息
            orderStateMachine.getExtendedState().getVariables().remove("err");
            //如果事务执行成功，则持久化状态机
            // 成功变更数据库
            if (result){
                //持久化状态机状态
                orderRedisPersister.persist(orderStateMachine, MACHINE_PRE + order.getId());
                strRedisTemplatec.opsForHash().put(DATA_PRE ,order.getId(), JSONUtil.toJsonStr(order));
            }
            log.info("o exception:{}",o);
        } catch (Exception e) {
            log.error("err occur :{}",e.getMessage());
        } finally {
            orderStateMachine.stop();
        }
        return result;
    }


    /**
     * 发送订单状态转换事件
     *
     * 使用自定义伪持久化策略
     *
     * 我们设想一个业务场景，就比如订单，我们一般的设计都会把订单状态存到订单表里面，
     * 其他的业务信息也都有表保存，而状态机的主要作用其实是规范整个订单业务流程的状态和事件，
     * 所以状态机要不要保存真的不重要，我们只需要从订单表里面把状态取出来，知道当前是什么状态，
     * 然后伴随着业务继续流浪到下一个状态节点就好了。
     *
     * @param message
     * @param order
     * @return
     */
    private synchronized boolean sendEventByRedis(Message<OrderStatusChangeEvent> message, Order order) {
        boolean result = false;
        try {
            orderStateMachine.start();
            // 尝试恢复状态机状态
            orderPersister.restore(orderStateMachine, order);

            // 判断是否已完成
            if (order.getStatus().equals(OrderStatus.FINISH)){
                // 订单已完成，删除redis
                strRedisTemplatec.delete(MACHINE_PRE + order.getId());
                orderStateMachine.setStateMachineError(new RuntimeException("订单已是终态"));
                log.info("订单[{}]已完成",order);
                return false;
            }

            result = orderStateMachine.sendEvent(message);
            // 成功变更数据库
            if (result){
                strRedisTemplatec.opsForHash().put(DATA_PRE,order.getId(), JSONUtil.toJsonStr(order));
                //持久化状态机状态
                orderPersister.persist(orderStateMachine, order);
                log.info("order status change in cache:{}",JSONUtil.toJsonStr(order));
            }
        } catch (Exception e) {
            //
            log.info("err occur :{}",e.getMessage());
        } finally {
            orderStateMachine.stop();
        }
        return result;
    }
}

