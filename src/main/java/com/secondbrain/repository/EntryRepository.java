package com.secondbrain.repository;

import com.secondbrain.model.Entry;
import com.secondbrain.model.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

    List<Entry> findByTypeOrderByCreatedAtDesc(EntryType type);

    @Query(value = """
        SELECT e.* FROM entries e
        WHERE e.content_tsvector @@ websearch_to_tsquery('english', :query)
        ORDER BY ts_rank(e.content_tsvector, websearch_to_tsquery('english', :query)) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Entry> fullTextSearch(@Param("query") String query, @Param("limit") int limit);

    @Query(value = """
        SELECT e.* FROM entries e,
        websearch_to_tsquery('english', :query) q,
        LATERAL unnest(tags) tag
        WHERE e.content_tsvector @@ q
        OR tag::text ILIKE '%' || :query || '%'
        ORDER BY
            ts_rank(e.content_tsvector, q) DESC,
            e.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Entry> hybridSearch(@Param("query") String query, @Param("limit") int limit);

    @Query(value = """
        SELECT e.* FROM entries e
        WHERE jsonb_path_exists(tags, ('$[*] ? (@ like_regex "' || :tag || '" flag "i")')::jsonpath)
        ORDER BY e.created_at DESC
        """, nativeQuery = true)
    List<Entry> findByTagContainingIgnoreCase(@Param("tag") String tag);
}
