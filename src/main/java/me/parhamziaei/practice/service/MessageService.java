package me.parhamziaei.practice.service;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.enums.Message;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    public String get(Message message) {
        return messageSource.getMessage(message.key(), null, Locale.forLanguageTag("fa"));
    }

}
