package me.parhamziaei.practice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.exception.custom.service.EmailServiceException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendTwoFactorCodeEmail(String to, String code) {
        try {
            char[] codeArray = code.toCharArray();

            Context context = new Context();
            context.setVariable("code", codeArray);

            String template = templateEngine.process("two-factor-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Two-Factor Email");
            helper.setText(template, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailServiceException(e.getMessage());
        }
    }

    public void sendEmailVerificationEmail(String to, String code) {
        try {
            char[] codeArray = code.toCharArray();

            Context context = new Context();
            context.setVariable("code", codeArray);

            String template = templateEngine.process("email-verify-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Email Verification");
            helper.setText(template, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailServiceException(e.getMessage());
        }
    }


}
