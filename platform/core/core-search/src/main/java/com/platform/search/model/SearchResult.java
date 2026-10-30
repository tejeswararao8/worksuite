package com.platform.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    // Total number of matching documents
    private long totalHits;

    // Current page results
    private List<SearchDocument> documents;

    // Search time in milliseconds
    private long tookMs;

    // Current page number
    private int page;

    // Page size
    private int size;

    // Total pages
    public int getTotalPages() {
        if (size == 0) return 0;
        return (int) Math.ceil((double) totalHits / size);
    }
}
