package me.parhamziaei.practice.configuration;

import me.parhamziaei.practice.dto.response.ticket.TicketDetailResponse;
import me.parhamziaei.practice.dto.response.ticket.TicketMessageResponse;
import me.parhamziaei.practice.entity.jpa.Ticket;
import me.parhamziaei.practice.entity.jpa.TicketMessage;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanManagement {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.typeMap(TicketMessage.class, TicketMessageResponse.class)
                .addMappings(m -> m.skip(TicketMessageResponse::setAttachments));
        mapper.typeMap(Ticket.class, TicketDetailResponse.class)
                .addMappings(m -> m.skip(TicketDetailResponse::setMessages));
        return mapper;
    }
}
