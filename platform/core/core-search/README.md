# core-search

**OpenSearch full-text search engine integration for all modules.**

## What It Does

- `OpenSearchConfig` — configures the OpenSearch Java client with host/port/scheme
- `SearchService` — index, search (multi-match), search with filters, delete documents; auto-creates indexes
- `SearchDocument` — serializable document model indexed in OpenSearch
- `SearchResult` — paginated search result with totalHits, documents, and timing

## Tech

OpenSearch is a 100% free Apache 2.0 licensed fork of Elasticsearch 7.10, backed by AWS. It uses the same API as Elasticsearch — all client code is compatible.

## Package

```
com.platform.search.config
com.platform.search.model
com.platform.search.service
```

## Files

| File | Purpose |
|---|---|
| `config/OpenSearchConfig.java` | OpenSearch Java client bean |
| `service/SearchService.java` | Index, search, filter, delete operations |
| `model/SearchDocument.java` | Document model for OpenSearch |
| `model/SearchResult.java` | Paginated search result |

---

## How to Use — Index a Document

Call `SearchService.index()` whenever you create or update data that should be searchable:

```java
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final SearchService searchService;

    @Auditable(entity = "Employee", action = AuditLog.AuditAction.CREATE)
    public EmployeeDto create(CreateEmployeeRequest req) {
        Employee saved = employeeRepository.save(employee);

        // Index in OpenSearch for full-text search
        searchService.index("employees", SearchDocument.builder()
            .id(saved.getId().toString())
            .module("HR")
            .type("EMPLOYEE")
            .title(saved.getFirstName() + " " + saved.getLastName())
            .content(saved.getEmail() + " " + saved.getEmployeeCode())
            .tenantId(saved.getCompanyId().toString())
            .metadata(Map.of(
                "departmentId",  saved.getDepartment() != null ? saved.getDepartment().getId().toString() : "",
                "designationId", saved.getDesignation() != null ? saved.getDesignation().getId().toString() : "",
                "status",        saved.getStatus().name()
            ))
            .build());

        return mapper.toDto(saved);
    }

    public void delete(UUID id) {
        employeeRepository.deleteById(id);

        // Remove from OpenSearch
        searchService.delete("employees", id.toString());
    }
}
```

---

## Search — Full Text

```java
// Search employees by name, email, or employee code
SearchResult result = searchService.search("employees", "john doe", 0, 20);

result.getTotalHits();          // total matching documents
result.getDocuments();          // List<SearchDocument>
result.getTookMs();             // how long the query took
result.getPage();               // current page
result.getSize();               // page size
```

---

## Search — With Filters

```java
// Search employees in a specific department
SearchResult result = searchService.searchWithFilter(
    "employees",
    "john",                                          // full-text query
    Map.of("departmentId", departmentId.toString()), // metadata filters
    0,                                               // page
    20                                               // size
);
```

---

## Index Naming

All indexes are prefixed with `vve_` automatically:

| Module | Index Name | Full Index |
|---|---|---|
| HRMS | `employees` | `vve_employees` |
| Tickets | `tickets` | `vve_tickets` |
| Support | `support_tickets` | `vve_support_tickets` |
| Chat | `chat_messages` | `vve_chat_messages` |
| Documents | `documents` | `vve_documents` |

Indexes are created automatically on first use — no manual setup needed.

---

## SearchDocument Fields

| Field | Type | Description |
|---|---|---|
| `id` | String | Entity UUID (used as OpenSearch document ID) |
| `module` | String | Module name e.g. `"HR"`, `"TICKET"` |
| `type` | String | Entity type e.g. `"EMPLOYEE"`, `"TICKET"` |
| `title` | String | Primary searchable text (3x weight in search) |
| `content` | String | Secondary searchable text (1x weight) |
| `metadata` | `Map<String, Object>` | Filterable fields (department, status, etc.) |
| `tenantId` | String | Company UUID for tenant isolation |
| `indexedAt` | LocalDateTime | When the document was indexed |

---

## Search Scoring

Multi-match query with field boosting:
- `title` field has **3x weight** — name/title matches rank higher
- `content` field has **1x weight** — secondary content matches

---

## Configuration

```yaml
opensearch:
  host:   ${OPENSEARCH_HOST:localhost}
  port:   ${OPENSEARCH_PORT:9200}
  scheme: ${OPENSEARCH_SCHEME:http}
```

OpenSearch Dashboard: http://localhost:5601

Start with:
```bash
docker compose up -d opensearch opensearch-dashboards
```

Note: OpenSearch uses ~1GB RAM. Start it separately when needed.

---

## Dependencies

```xml
<dependency><groupId>com.platform</groupId><artifactId>core-tenancy</artifactId></dependency>
<dependency>
    <groupId>org.opensearch.client</groupId>
    <artifactId>opensearch-java</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
</dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
```
