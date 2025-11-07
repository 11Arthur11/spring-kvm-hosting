package me.parhamziaei.practice.service;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.enums.Text;
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

    public String get(Text message) {
        return textSource.getMessage(message.key(), null, Locale.forLanguageTag("fa"));
    }

}
