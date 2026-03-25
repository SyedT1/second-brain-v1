package com.secondbrain.repository;

import com.secondbrain.model.Entry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EntrySearchRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Find similar entries using pgvector cosine similarity
     * @param embedding The query embedding vector
     * @param limit Maximum number of results
     * @return List of similar entries ordered by similarity (most similar first)
     */
    @SuppressWarnings("unchecked")
    public List<Entry> findSimilar(float[] embedding, int limit) {
        String sql = """
            SELECT e.* FROM entries e
            WHERE e.embedding IS NOT NULL
            ORDER BY e.embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql, Entry.class);
        query.setParameter("embedding", formatVector(embedding));
        query.setParameter("limit", limit);

        return query.getResultList();
    }

    /**
     * Find similar entries excluding a specific entry ID
     * @param entryId The entry ID to exclude
     * @param embedding The query embedding vector
     * @param limit Maximum number of results
     * @return List of similar entries
     */
    @SuppressWarnings("unchecked")
    public List<Entry> findSimilarExcluding(Long entryId, float[] embedding, int limit) {
        String sql = """
            SELECT e.* FROM entries e
            WHERE e.embedding IS NOT NULL
            AND e.id != :entryId
            ORDER BY e.embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql, Entry.class);
        query.setParameter("entryId", entryId);
        query.setParameter("embedding", formatVector(embedding));
        query.setParameter("limit", limit);

        return query.getResultList();
    }

    /**
     * Hybrid search combining full-text search and vector similarity
     * @param searchQuery The text query for FTS
     * @param embedding The query embedding vector
     * @param limit Maximum number of results
     * @return Ranked list combining both search methods
     */
    @SuppressWarnings("unchecked")
    public List<Entry> hybridSearch(String searchQuery, float[] embedding, int limit) {
        String sql = """
            WITH fts_results AS (
                SELECT id,
                       ts_rank(content_tsvector, websearch_to_tsquery('english', :query)) as fts_score
                FROM entries
                WHERE content_tsvector @@ websearch_to_tsquery('english', :query)
            ),
            vector_results AS (
                SELECT id,
                       1 - (embedding <=> CAST(:embedding AS vector)) as similarity_score
                FROM entries
                WHERE embedding IS NOT NULL
            )
            SELECT e.*
            FROM entries e
            LEFT JOIN fts_results f ON e.id = f.id
            LEFT JOIN vector_results v ON e.id = v.id
            WHERE f.fts_score IS NOT NULL OR v.similarity_score IS NOT NULL
            ORDER BY
                COALESCE(f.fts_score, 0) * 0.5 +
                COALESCE(v.similarity_score, 0) * 0.5 DESC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql, Entry.class);
        query.setParameter("query", searchQuery);
        query.setParameter("embedding", formatVector(embedding));
        query.setParameter("limit", limit);

        return query.getResultList();
    }

    /**
     * Format float array as PostgreSQL vector string
     */
    private String formatVector(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
