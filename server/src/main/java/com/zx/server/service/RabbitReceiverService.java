package com.zx.server.service;

import com.zx.model.dto.KillSuccessUserInfo;
import com.zx.model.entity.ItemKillSuccess;
import com.zx.model.mapper.ItemKillSuccessMapper;
import com.zx.server.dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-11
 */
@Service
public class RabbitReceiverService {

    public static final Logger log = LoggerFactory.getLogger(RabbitReceiverService.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private Environment env;

    @Autowired(required = false)
    private ItemKillSuccessMapper itemKillSuccessMapper;


    /**
     * 秒杀异步邮件通知-接收消息
     */
    @RabbitListener(queues = {"${mq.kill.item.success.email.queue}"}, containerFactory = "singleListenerContainer")
    public void consumeEmailMsg(KillSuccessUserInfo info) {
        try {
            log.info("秒杀异步邮件通知-接收消息:{}", info);

            // 真正的发送邮件....
//            MailDto dto=new MailDto(env.getProperty("mail.kill.item.success.subject"),"这是测试内容",new String[]{info.getEmail()});
//            mailService.sendSimpleEmail(dto);
            final String content = String.format(env.getProperty("mail.kill.item.success.content"), info.getItemName(), info.getCode());
            MailDto dto = new MailDto(env.getProperty("mail.kill.item.success.subject"), content, new String[]{info.getEmail()});
            mailService.sendHTMLMail(dto);
        } catch (Exception e) {
            log.error("秒杀异步邮件通知-接收消息-发生异常：", e.fillInStackTrace());
        }
    }

    /**
     * 用户秒杀成功后超时未支付-监听者
     * @param info
     */
    @RabbitListener(queues = {"${mq.kill.item.success.kill.dead.real.queue}"},containerFactory = "singleListenerContainer")
    public void consumeExpireOrder(KillSuccessUserInfo info){
        try {
            log.info("用户秒杀成功后超时未支付-监听者-接收消息:{}",info);

            if (info!=null){
                ItemKillSuccess entity=itemKillSuccessMapper.selectByPrimaryKey(info.getCode());
                // 状态0表示未支付，1表示已支付，2表示取消(1,2由用户触发)
                // 这里针对0状态处理，是则失效订单记录
                // 跟新mysql里的状态为-1，表示失效
                if (entity!=null && entity.getStatus().intValue()==0){
                    itemKillSuccessMapper.expireOrder(info.getCode());
                }
            }
        }catch (Exception e){
            log.error("用户秒杀成功后超时未支付-监听者-发生异常：",e.fillInStackTrace());
        }
    }

}
