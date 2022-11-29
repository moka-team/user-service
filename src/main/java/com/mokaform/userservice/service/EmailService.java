package com.mokaform.userservice.service;


import com.mokaform.userservice.common.config.MailConfig;
import com.mokaform.userservice.common.exception.ApiException;
import com.mokaform.userservice.common.exception.errorcode.CommonErrorCode;
import com.mokaform.userservice.common.exception.errorcode.UserErrorCode;
import com.mokaform.userservice.common.util.RedisService;
import com.mokaform.userservice.common.util.UserUtilService;
import com.mokaform.userservice.common.util.constant.EmailType;
import com.mokaform.userservice.common.util.constant.RedisConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Optional;

@Service
public class EmailService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final MailConfig mailConfig;

    private final JavaMailSender javaMailSender;

    private final RedisService redisService;

    private final UserUtilService userUtilService;

    public EmailService(MailConfig mailConfig,
                        JavaMailSender javaMailSender,
                        RedisService redisService,
                        UserUtilService userUtilService) {
        this.mailConfig = mailConfig;
        this.javaMailSender = javaMailSender;
        this.redisService = redisService;
        this.userUtilService = userUtilService;
    }

    /*
        메일 발송
        sendSimpleMessage의 매개변수로 들어온 to는 인증번호를 받을 메일주소
        MimeMessage 객체 안에 내가 전송할 메일의 내용을 담아준다.
        bean으로 등록해둔 javaMailSender 객체를 사용하여 이메일 send
     */
    public void sendVerificationCode(EmailType type, String email) {
        if (type.equals(EmailType.RESET_PASSWORD)) {
            userUtilService.checkUser(email);
        }

        String verificationCode = RandomStringUtils.random(6, true, true);

        try {
            MimeMessage message = createMessage(type.getSubject(), email, verificationCode);
            javaMailSender.send(message); // 메일 발송
        } catch (MailException e) {
            log.debug(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ApiException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        saveVerificationCode(type, email, verificationCode);
    }

    public void checkVerificationCode(EmailType type, String email, String code) {
        String savedCode = getVerificationCode(type, email)
                .orElseThrow(() -> new ApiException(UserErrorCode.EXPIRED_VALIDATION_CODE));
        if (!savedCode.equals(code)) {
            throw new ApiException(UserErrorCode.INVALID_VALIDATION_CODE);
        }
    }

    private MimeMessage createMessage(String subject, String to, String code) throws MessagingException, UnsupportedEncodingException {
        log.info("보내는 대상 : " + to);
        log.info("인증 번호 : " + code);
        MimeMessage message = javaMailSender.createMimeMessage();

        message.addRecipients(MimeMessage.RecipientType.TO, to); // to 보내는 대상
        message.setSubject(subject, "utf-8"); //메일 제목

        // 메일 내용 메일의 subtype을 html로 지정하여 html문법 사용 가능
        String msg = new StringBuilder()
                .append("<h3 style=\"font-size: 20px; padding-right: 30px; padding-left: 30px;\">이메일 주소 확인</h3>")
                .append("<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">아래 확인 코드를 입력해주세요.</p>")
                .append("<div style=\"padding-right: 30px; padding-left: 30px; margin: 32px 0 40px;\"><table style=\"border-collapse: collapse; border: 0; background-color: #F4F4F4; height: 70px; table-layout: fixed; word-wrap: break-word; border-radius: 6px;\"><tbody><tr><td style=\"text-align: center; vertical-align: middle; font-size: 30px;\">")
                .append(code)
                .append("</td></tr></tbody></table></div>")
                .toString();

        message.setText(msg, "utf-8", "html"); //내용, charset타입, subtype
        message.setFrom(new InternetAddress(mailConfig.getUsername(), mailConfig.getSender())); //보내는 사람의 메일 주소, 보내는 사람 이름

        return message;
    }

    private void saveVerificationCode(EmailType type, String email, String code) {
        deleteVerificationCode(type, email);
        redisService.setValues(new StringBuilder()
                        .append(RedisConstants.EMAIL_VERIFICATION.getPrefix())
                        .append(type.getRedisPrefix())
                        .append(email)
                        .toString(),
                code,
                Duration.ofSeconds(mailConfig.getValidTime()));
    }

    private void deleteVerificationCode(EmailType type, String email) {
        redisService.deleteValues(new StringBuilder()
                .append(RedisConstants.EMAIL_VERIFICATION.getPrefix())
                .append(type.getRedisPrefix())
                .append(email)
                .toString());
    }

    private Optional<String> getVerificationCode(EmailType type, String email) {
        String values = redisService.getValues(new StringBuilder()
                .append(RedisConstants.EMAIL_VERIFICATION.getPrefix())
                .append(type.getRedisPrefix())
                .append(email)
                .toString());
        return Optional.ofNullable(values);
    }
}
