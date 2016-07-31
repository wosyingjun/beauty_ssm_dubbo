
package com.yingjun.ssm.mq.listener;

import com.alibaba.fastjson.JSONObject;
import com.yingjun.ssm.common.model.MailParam;
import com.yingjun.ssm.mq.biz.MailService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * 用于发送邮件
 *
 * @author yingjun
 */
@Component
public class MailMessageListener implements SessionAwareMessageListener<Message> {

    private final Logger log = LoggerFactory.getLogger(MailMessageListener.class);

    @Autowired
    private JmsTemplate mailMqJmsTemplate;
    @Autowired
    private MailService mailService;

    /**
     * 发送邮件
     *
     * @param message
     * @param session
     */
    public void onMessage(Message message, Session session) throws JMSException {
        //这里建议不要try catch，让异常抛出，通过redeliveryPolicy去重试，达到重试次数进入死信DLQ(Dead Letter Queue)
        ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
        final String ms = msg.getText();
        log.info("==>receive message:" + ms);
        // 转换成相应的对象
        MailParam mailParam = JSONObject.parseObject(ms, MailParam.class);
        if (mailParam == null) {
            log.error("mailParam is empty!");
            return;
        }
        mailService.mailSend(mailParam);
    }

}
