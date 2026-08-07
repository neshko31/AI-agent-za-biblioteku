package ftn.iis.search;

import ftn.iis.search.dto.SearchBookDto;
import ftn.iis.search.dto.SearchBookRecordDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.util.Optional;

@Component
public class SearchSyncClient {

    private final RestClient restClient;

    public SearchSyncClient(@Value("${search.service.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public Optional<SearchBookRecordDto> findByIsbn(String isbn) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/api/books/by-isbn/{isbn}", isbn)
                    .retrieve()
                    .body(SearchBookRecordDto.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<SearchBookRecordDto> create(SearchBookDto dto) {
        try {
            return Optional.ofNullable(restClient.post()
                    .uri("/api/books/create")
                    .body(dto)
                    .retrieve()
                    .body(SearchBookRecordDto.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void update(String recordId, SearchBookDto dto) {
        try {
            restClient.patch()
                    .uri("/api/books/{recordId}", recordId)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
        }
    }

    public void delete(String recordId) {
        try {
            restClient.delete()
                    .uri("/api/books/{recordId}", recordId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
        }
    }

    public void requestOcr(String recordId, File pdfFile) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(pdfFile));

            restClient.post()
                    .uri("/api/books/{recordId}/ocr", recordId)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
        }
    }
}
