package me.parhamziaei.practice.dto.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.parhamziaei.practice.entity.jpa.TicketMessage;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageInternal {

    private Resource image;

    private String originalName;

    private String storedName;

    private String mimeType;

    private Long size;

}
