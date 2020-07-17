package com.zx.server.service;

import com.zx.server.dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-11
 * 邮件服务
 */
@Service
// 开启异步服务
@EnableAsync
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;


    /**
     * 发送简单文本文件
     */
    @Async
    public void sendSimpleEmail(final MailDto dto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // 设置发件人
            message.setFrom(env.getProperty("mail.send.from"));
            // 设置收件人
            message.setTo(dto.getTos());
            // 设置邮件主题
            message.setSubject(dto.getSubject());
            // 设置内容
            message.setText(dto.getContent());
            // 发送
            mailSender.send(message);

            log.info("发送简单文本文件-发送成功!");
        } catch (Exception e) {
            log.error("发送简单文本文件-发生异常： ", e.fillInStackTrace());
        }
    }

    /**
     * 发送花哨邮件
     *
     * @param dto
     */
    @Async
    public void sendHTMLMail(final MailDto dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "utf-8");
            messageHelper.setFrom(env.getProperty("mail.send.from"));
            messageHelper.setTo(dto.getTos());
            messageHelper.setSubject(dto.getSubject());
            // html文本
            messageHelper.setText(dto.getContent(), true);

            mailSender.send(message);
            log.info("发送花哨邮件-发送成功!");
        } catch (Exception e) {
            log.error("发送花哨邮件-发生异常： ", e.fillInStackTrace());
        }
    }
}
