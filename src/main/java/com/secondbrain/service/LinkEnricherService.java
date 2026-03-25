package com.secondbrain.service;

import com.secondbrain.model.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkEnricherService {

    private static final int TIMEOUT_MS = 5000;

    public void enrichLinkMetadata(Entry entry) {
        if (entry.getUrl() == null || entry.getUrl().isBlank()) {
            return;
        }

        log.debug("Enriching link metadata for URL: {}", entry.getUrl());

        try {
            Document doc = Jsoup.connect(entry.getUrl())
                    .timeout(TIMEOUT_MS)
                    .userAgent("Mozilla/5.0 (compatible; SecondBrain/1.0)")
                    .get();

            Map<String, Object> metadata = entry.getMetadata() != null
                    ? new HashMap<>(entry.getMetadata())
                    : new HashMap<>();

            // Extract Open Graph metadata
            String ogTitle = doc.select("meta[property=og:title]").attr("content");
            String ogDescription = doc.select("meta[property=og:description]").attr("content");
            String ogImage = doc.select("meta[property=og:image]").attr("content");
            String ogType = doc.select("meta[property=og:type]").attr("content");

            // Fallback to standard meta tags
            if (ogTitle.isEmpty()) {
                ogTitle = doc.title();
            }
            if (ogDescription.isEmpty()) {
                ogDescription = doc.select("meta[name=description]").attr("content");
            }

            // Store metadata
            if (!ogTitle.isEmpty()) {
                metadata.put("ogTitle", ogTitle);
                if (entry.getTitle() == null || entry.getTitle().isBlank()) {
                    entry.setTitle(ogTitle);
                }
            }
            if (!ogDescription.isEmpty()) {
                metadata.put("ogDescription", ogDescription);
            }
            if (!ogImage.isEmpty()) {
                metadata.put("ogImage", ogImage);
            }
            if (!ogType.isEmpty()) {
                metadata.put("ogType", ogType);
            }

            // Extract additional useful info
            String siteName = doc.select("meta[property=og:site_name]").attr("content");
            if (!siteName.isEmpty()) {
                metadata.put("siteName", siteName);
            }

            entry.setMetadata(metadata);
            log.info("Successfully enriched link metadata for entry {}", entry.getId());

        } catch (Exception e) {
            log.error("Error enriching link metadata for URL: {}", entry.getUrl(), e);
            // Don't throw - enrichment is optional
        }
    }
}
