package com.tenxi.listener;

import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Component
@Log
@RabbitListener(bindings = {@QueueBinding(
    value = @Queue(name = "online.education.mail"),
    exchange = @Exchange(name = "online.direct", type = ExchangeTypes.DIRECT),
    key = "online-education-mail"
)})
public class MailListener {
    @Resource
    private MailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;

    @RabbitHandler
    public void sendMail(Map<String,Object> map) {
        String email = (String)map.get("email");
        String code = (String)map.get("code");
        String type = (String)map.get("type");

        SimpleMailMessage message = switch (type) {
            case "register" -> createMessage(email,
                    "您的邮件注册验证码为：" + code + "，有效时间3分钟，为了保障您的账户安全，请勿向他人泄露验证码信息",
                    "欢迎注册Online Education");
            case "reset" -> createMessage(email,
                    "您的密码修改验证码为：" + code + "，有效时间3分钟，为了保障您的账户安全，请勿向他人泄露验证码信息",
                    "Online Education密码重置邮件");
            case "modify" -> createMessage(email,
                    "您的邮件修改验证码为：" + code + "，有效时间3分钟，为了保障您的账户安全，请勿向他人泄露验证码信息",
                    "Online Education邮箱重置邮件");
            default -> null;
        };
        if(message != null) {
            log.info("邮件发送成功");
            mailSender.send(message);
        }
        else return;
    }

    public SimpleMailMessage createMessage(String email, String content, String title) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject(title);
        message.setText(content);
        message.setSentDate(new Date());
        return message;
    }
}
