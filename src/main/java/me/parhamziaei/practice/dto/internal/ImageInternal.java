package me.parhamziaei.practice.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

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
