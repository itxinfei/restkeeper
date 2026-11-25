package com.restkeeper.sms.listener;

import com.alibaba.alicloud.sms.ISmsService;
import com.alibaba.fastjson.JSON;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.sms.SmsObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
//未配置阿里云短信时（ISmsService 不存在）仅记录告警，保证 MQ 消费者服务本身可启动
@Slf4j
public class SmsMessageListener {

    @Autowired
    private ObjectProvider<ISmsService> smsServiceProvider;

    @RabbitListener(queues = SystemCode.SMS_ACCOUNT_QUEUE)
    public void getAccountMessage(String message){

        log.info("发送短信监听类接收到了消息：{}", message);

        ISmsService smsService = smsServiceProvider.getIfAvailable();
        if (smsService == null) {
            log.warn("未配置阿里云短信服务（ISmsService 不可用），跳过短信发送");
            return;
        }

        //转换参数
        SmsObject smsObject = JSON.parseObject(message, SmsObject.class);

        //基于SMS组件进行短信发送
        SendSmsResponse smsResponse = this.sendSms(smsService, smsObject.getPhoneNumber(),smsObject.getSignName(),smsObject.getTemplateCode(),smsObject.getTemplateJsonParam());

        log.info(JSON.toJSONString(smsResponse));
    }

    //发送手机短信
    private SendSmsResponse sendSms(ISmsService smsService, String phoneNumber, String signName, String templateCode, String templateJsonParam) {

        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNumber);
        request.setSignName(signName);
        request.setTemplateCode(templateCode);
        request.setTemplateParam(templateJsonParam);

        SendSmsResponse sendSmsResponse ;

        try {
            sendSmsResponse = smsService.sendSmsRequest(request);
        } catch (ClientException e) {
            //K18：不再吞异常，记录后由 AMQP 重试策略接管（common.yml 已开启 retry）
            log.error("短信发送失败, phone={}, templateCode={}", phoneNumber, templateCode, e);
            throw new RuntimeException("短信发送失败", e);
        }

        return sendSmsResponse;
    }
}
