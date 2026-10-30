package com.platform.search.service;

import com.platform.search.model.SearchDocument;
import com.platform.search.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final OpenSearchClient client;

    // Index name prefix for all platform indexes
    private static final String INDEX_PREFIX = "vve_";

    // ─────────────────────────────────────────────
    // INDEX - Add or update a document
    // Called by each module when data is created/updated
    // ─────────────────────────────────────────────
    public void index(String indexName, SearchDocument document) {
        try {
            document.setIndexedAt(LocalDateTime.now());
            String fullIndex = INDEX_PREFIX + indexName;

            createIndexIfNotExists(fullIndex);

            IndexRequest<SearchDocument> request = IndexRequest.of(i -> i
                    .index(fullIndex)
                    .id(document.getId())
                    .document(document)
            );

            client.index(request);
            log.debug("Search INDEX → index: {}, id: {}", fullIndex, document.getId());

        } catch (IOException e) {
            log.error("Search INDEX failed → index: {}, id: {}", indexName, document.getId(), e);
        }
    }

    // ─────────────────────────────────────────────
    // SEARCH - Full text search across an index
    // ─────────────────────────────────────────────
    public SearchResult search(String indexName, String query, int page, int size) {
        try {
            String fullIndex = INDEX_PREFIX + indexName;
            int from = page * size;

            SearchRequest request = SearchRequest.of(s -> s
                    .index(fullIndex)
                    .from(from)
                    .size(size)
                    .query(q -> q
                            .multiMatch(m -> m
                                    .query(query)
                                    .fields("title^3", "content^1")  // title has 3x weight
                            )
                    )
            );

            SearchResponse<SearchDocument> response = client.search(request, SearchDocument.class);

            List<SearchDocument> documents = response.hits().hits()
                    .stream()
                    .map(Hit::source)
                    .filter(doc -> doc != null)
                    .collect(Collectors.toList());

            return SearchResult.builder()
                    .totalHits(response.hits().total() != null ? response.hits().total().value() : 0)
                    .documents(documents)
                    .tookMs(response.took())
                    .page(page)
                    .size(size)
                    .build();

        } catch (IOException e) {
            log.error("Search SEARCH failed → index: {}, query: {}", indexName, query, e);
            return SearchResult.builder().totalHits(0).documents(List.of()).page(page).size(size).build();
        }
    }

    // ─────────────────────────────────────────────
    // SEARCH WITH FILTER - Search with metadata filters
    // Example: search employees in specific department
    // ─────────────────────────────────────────────
    public SearchResult searchWithFilter(String indexName, String query,
                                          Map<String, String> filters, int page, int size) {
        try {
            String fullIndex = INDEX_PREFIX + indexName;
            int from = page * size;

            SearchRequest request = SearchRequest.of(s -> s
                    .index(fullIndex)
                    .from(from)
                    .size(size)
                    .query(q -> q
                            .bool(b -> {
                                b.must(m -> m
                                        .multiMatch(mm -> mm
                                                .query(query)
                                                .fields("title^3", "content^1")
                                        )
                                );
                                filters.forEach((key, value) ->
                                        b.filter(f -> f
                                                .term(t -> t
                                                        .field("metadata." + key)
                                                        .value(v -> v.stringValue(value))
                                                )
                                        )
                                );
                                return b;
                            })
                    )
            );

            SearchResponse<SearchDocument> response = client.search(request, SearchDocument.class);

            List<SearchDocument> documents = response.hits().hits()
                    .stream()
                    .map(Hit::source)
                    .filter(doc -> doc != null)
                    .collect(Collectors.toList());

            return SearchResult.builder()
                    .totalHits(response.hits().total() != null ? response.hits().total().value() : 0)
                    .documents(documents)
                    .tookMs(response.took())
                    .page(page)
                    .size(size)
                    .build();

        } catch (IOException e) {
            log.error("Search FILTER failed → index: {}", indexName, e);
            return SearchResult.builder().totalHits(0).documents(List.of()).page(page).size(size).build();
        }
    }

    // ─────────────────────────────────────────────
    // DELETE - Remove document from index
    // Called when data is deleted from database
    // ─────────────────────────────────────────────
    public void delete(String indexName, String documentId) {
        try {
            String fullIndex = INDEX_PREFIX + indexName;

            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(fullIndex)
                    .id(documentId)
            );

            client.delete(request);
            log.debug("Search DELETE → index: {}, id: {}", fullIndex, documentId);

        } catch (IOException e) {
            log.error("Search DELETE failed → index: {}, id: {}", indexName, documentId, e);
        }
    }

    // ─────────────────────────────────────────────
    // CREATE INDEX - Create index if not exists
    // ─────────────────────────────────────────────
    private void createIndexIfNotExists(String indexName) {
        try {
            boolean exists = client.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (!exists) {
                client.indices().create(
                        CreateIndexRequest.of(c -> c.index(indexName))
                );
                log.info("Search INDEX CREATED → {}", indexName);
            }
        } catch (IOException e) {
            log.error("Search CREATE INDEX failed → {}", indexName, e);
        }
    }
}
