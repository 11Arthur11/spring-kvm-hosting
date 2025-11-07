package me.parhamziaei.practice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.enums.Text;
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
    private final MessageService messageService;

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
            helper.setSubject(messageService.get(Text.MAIL_TITLE_EMAIL_TWO_FACTOR));
            helper.setText(template, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailServiceException(e.getMessage());
        }
    }

    @Async
    public void sendEmailVerificationEmail(String to, String code) {
        try {
            char[] codeArray = code.toCharArray();

            Context context = new Context();
            context.setVariable("code", codeArray);

            String template = templateEngine.process("email-verify-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(messageService.get(Text.MAIL_TITLE_EMAIL_VERIFICATION));
            helper.setText(template, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailServiceException(e.getMessage());
        }
    }

    @Async
    public void sendForgotPasswordCodeEmail(String to, String code) {
        try {
            char[] codeArray = code.toCharArray();

            Context context = new Context();
            context.setVariable("code", codeArray);

            String template = templateEngine.process("forgot-password-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(messageService.get(Text.MAIL_TITLE_FORGOT_PASSWORD));
            helper.setText(template, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailServiceException(e.getMessage());
        }
    }

}
