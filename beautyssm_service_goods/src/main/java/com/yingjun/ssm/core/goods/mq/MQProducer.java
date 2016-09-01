package com.yingjun.ssm.core.goods.mq;

import com.alibaba.fastjson.JSONObject;
import com.yingjun.ssm.common.model.BizOperator;
import com.yingjun.ssm.common.model.MailParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * MQ消息生产者
 *
 * @author yingjun
 */
@Component
public class MQProducer {

    private final Logger log = LoggerFactory.getLogger(MQProducer.class);


    @Autowired
    private JmsTemplate bizMqJmsTemplate;
    @Autowired
    private JmsTemplate mailMqJmsTemplate;

    /**
     * 用于解决分布式事务的消息.
     *
     * @param goodsid
     */
    public void sendBizMessage(final long goodsid) {
        final long start = System.currentTimeMillis();
        bizMqJmsTemplate.send(new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                long end = System.currentTimeMillis();
                log.info("createMessage use time" + (end - start) + "ms");
                BizOperator operator = new BizOperator("testDistributedTransaction", goodsid);
                return session.createTextMessage(JSONObject.toJSONString(operator));
            }
        });
        long end = System.currentTimeMillis();
        log.info("sendBizMessage use time" + (end - start) + "ms");
    }

    /**
     * 用于发送email的消息.
     *
     * @param mail
     */
    public void sendMailMessage(final MailParam mail) {
        final long start = System.currentTimeMillis();
        mailMqJmsTemplate.send(new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                long end = System.currentTimeMillis();
                log.info("createMessage use time" + (end - start) + "ms");
                return session.createTextMessage(JSONObject.toJSONString(mail));
            }
        });
        long end = System.currentTimeMillis();
        log.info("sendMailMessage use time" + (end - start) + "ms");
    }

}