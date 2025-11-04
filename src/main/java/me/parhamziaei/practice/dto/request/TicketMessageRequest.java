package me.parhamziaei.practice.dto.request;

import lombok.Data;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
public class TicketMessageRequest {

    private String content;

}
