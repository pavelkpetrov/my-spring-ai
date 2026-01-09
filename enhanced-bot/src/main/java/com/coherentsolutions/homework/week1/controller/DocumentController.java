package com.coherentsolutions.homework.week1.controller;

import com.coherentsolutions.homework.week1.dto.DocumentIngest;
import com.coherentsolutions.homework.week1.dto.IngestResponse;
import com.coherentsolutions.homework.week1.service.DocumentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Document Controller - requires 'rag' profile
 *
 * This controller depends on DocumentService, which requires VectorStore.
 * Only available when the 'rag' profile is active.
 */
@RestController
@RequestMapping("/documents")
@Profile("rag")
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j // Using Lombok for logging
public class DocumentController {

    private DocumentService documentService;

    @PostMapping(path = "/ingest")
    public ResponseEntity<IngestResponse> ingest(@Valid @RequestBody DocumentIngest documentIngest) {
        log.info("Received ingest request with length: {}", documentIngest.getContent().length());
        IngestResponse ingestResponse = documentService.ingestDocument(documentIngest.getContent());
        return ResponseEntity.ok(ingestResponse);
    }

}
