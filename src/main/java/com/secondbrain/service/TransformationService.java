package com.secondbrain.service;

import com.secondbrain.model.Entry;
import com.secondbrain.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransformationService {

    private final EntryRepository entryRepository;
    private final ChatClient chatClient;

    public enum TransformFormat {
        TWEET_THREAD,
        ACTION_ITEMS,
        PRODUCT_BRIEF,
        EMAIL,
        BLOG_POST,
        LINKEDIN_POST
    }

    /**
     * Transform an entry into a different format using AI
     */
    @Transactional(readOnly = true)
    public String transformEntry(Long entryId, TransformFormat format) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found: " + entryId));

        log.info("Transforming entry {} to format: {}", entryId, format);

        String prompt = buildTransformationPrompt(entry, format);

        try {
            String result = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("Transformation completed successfully");
            return result;

        } catch (Exception e) {
            log.error("Error transforming entry", e);
            throw new RuntimeException("Failed to transform entry: " + e.getMessage());
        }
    }

    private String buildTransformationPrompt(Entry entry, TransformFormat format) {
        String baseContext = String.format("""
            Title: %s
            Content: %s
            Tags: %s
            Summary: %s
            """,
            entry.getTitle(),
            entry.getContent(),
            entry.getTags() != null ? String.join(", ", entry.getTags()) : "None",
            entry.getSummary() != null ? entry.getSummary() : "None"
        );

        return switch (format) {
            case TWEET_THREAD -> """
                Convert the following content into an engaging Twitter/X thread:

                %s

                Instructions:
                - Create 5-7 tweets (numbered 1/7, 2/7, etc.)
                - First tweet should be a hook
                - Each tweet max 280 characters
                - Use emojis where appropriate
                - End with a call-to-action
                - Make it engaging and quotable
                """.formatted(baseContext);

            case ACTION_ITEMS -> """
                Extract concrete action items from this content:

                %s

                Instructions:
                - List actionable tasks (not observations)
                - Use checkbox format: [ ] Task description
                - Prioritize by importance (High/Medium/Low)
                - Include deadlines if mentioned
                - Keep items specific and measurable
                """.formatted(baseContext);

            case PRODUCT_BRIEF -> """
                Create a product brief from this idea:

                %s

                Instructions:
                - Problem Statement (what problem does it solve?)
                - Target Users (who is this for?)
                - Key Features (3-5 main features)
                - Success Metrics (how to measure success?)
                - Potential Challenges (what could go wrong?)
                - Next Steps (immediate actions)
                """.formatted(baseContext);

            case EMAIL -> """
                Convert this into a professional email:

                %s

                Instructions:
                - Professional but friendly tone
                - Clear subject line
                - Concise body (3-4 paragraphs max)
                - Clear call-to-action
                - Appropriate sign-off
                """.formatted(baseContext);

            case BLOG_POST -> """
                Expand this into a blog post outline:

                %s

                Instructions:
                - Engaging title
                - Hook/Introduction
                - 3-5 main sections with subheadings
                - Key points under each section
                - Conclusion with takeaways
                - Estimated reading time
                """.formatted(baseContext);

            case LINKEDIN_POST -> """
                Convert this into a LinkedIn post:

                %s

                Instructions:
                - Professional and insightful tone
                - Start with a strong hook
                - Use line breaks for readability
                - Include relevant hashtags (3-5)
                - End with engagement question
                - 1300 characters max
                """.formatted(baseContext);
        };
    }
}
