package me.parhamziaei.practice.service;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.enums.Text;
import me.parhamziaei.practice.enums.TicketDepartment;
import me.parhamziaei.practice.enums.TicketStatus;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;
    private final MessageSource textSource;

    public String get(Message message) {
        return messageSource.getMessage(message.key(), null, Locale.forLanguageTag("fa"));
    }

    public String get(TicketStatus status) {
        return textSource.getMessage(status.key(), null, Locale.forLanguageTag("fa"));
    }

    public String get(TicketDepartment department) {
        return textSource.getMessage(department.key(), null, Locale.forLanguageTag("fa"));
    }

    public String get(Text text) {
        return textSource.getMessage(text.key(), null, Locale.forLanguageTag("fa"));
    }

}
