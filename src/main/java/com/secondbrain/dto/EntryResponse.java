package com.secondbrain.dto;

import com.secondbrain.model.EntryType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class EntryResponse {

    private Long id;
    private EntryType type;
    private String content;
    private String title;
    private String url;
    private String filePath;
    private Map<String, Object> metadata;
    private List<String> tags;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime enrichedAt;
}
