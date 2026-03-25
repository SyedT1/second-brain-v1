package com.secondbrain.dto;

import com.secondbrain.model.EntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CreateEntryRequest {

    @NotNull(message = "Entry type is required")
    private EntryType type;

    @NotBlank(message = "Content is required")
    private String content;

    private String title;

    private String url;

    private String filePath;

    private Map<String, Object> metadata;
}
