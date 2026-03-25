package com.secondbrain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {

    private List<SearchResult> results;
    private int totalResults;
    private String query;

    @Data
    @Builder
    public static class SearchResult {
        private Long id;
        private String type;
        private String title;
        private String content;
        private List<String> tags;
        private Double score;
        private String snippet;
    }
}
