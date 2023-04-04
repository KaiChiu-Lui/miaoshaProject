package com.miaoshaproject.mq;

import com.alibaba.fastjson2.JSON;
import com.miaoshaproject.client.ItemFeignClient;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
public class MqConsumer {

    @Autowired
    private ItemFeignClient itemFeignClient;

    private DefaultMQPushConsumer consumer = null;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(nameAddr);
        //订阅所有消息
        consumer.subscribe(topicName,"*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //实现库存真正到数据库内扣减的逻辑
                System.out.println("消费者消费消息");
                Message msg = msgs.get(0);
                String jsonString = new String(msg.getBody());
                Map<String,Object> map = JSON.parseObject(jsonString);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                try {
                    System.out.println("消费者扣减库存");
                    itemFeignClient.decreaseStock(itemId,amount);
                    System.out.println("消费者扣减库存完毕");
                } catch (BusinessException e) {
                    CommonReturnType commonReturnType = new CommonReturnType();
                    commonReturnType.setStatus("fail");
                    commonReturnType.setData("MQ扣减库存失败");
                    System.out.println(commonReturnType);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }
}
