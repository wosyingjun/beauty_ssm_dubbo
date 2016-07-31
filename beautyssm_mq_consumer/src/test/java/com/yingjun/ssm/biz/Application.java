package com.yingjun.ssm.biz;

import com.alibaba.fastjson.JSONObject;
import com.yingjun.ssm.common.model.BizOperator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * @author yingjun
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application.xml")
public class Application {

    private final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private JmsTemplate bizMqJmsTemplate;

    @Test
    public void mailSend() throws Exception {
        bizMqJmsTemplate.setSessionTransacted(true);
        for (int i = 0; i < 1; i++) {
            log.info("==>send message" + i);
            bizMqJmsTemplate.send(new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    log.info("getTransacted:" + session.getTransacted());
                    BizOperator operator = new BizOperator("testDistributedTransaction", 1001);
                    return session.createTextMessage(JSONObject.toJSONString(operator));
                }
            });
            log.info("==>finish send message"+ i);
        }
        while (true) {

        }
    }
}