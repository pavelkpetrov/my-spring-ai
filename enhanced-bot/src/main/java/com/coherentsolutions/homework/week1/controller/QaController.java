package com.coherentsolutions.homework.week1.controller;

import com.coherentsolutions.homework.week1.dto.AnswerResponse;
import com.coherentsolutions.homework.week1.service.QAService;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * QA Controller - requires 'rag' profile
 *
 * This controller depends on QAService, which requires VectorStore.
 * Only available when the 'rag' profile is active.
 */
@RestController
@RequestMapping("/qa")
@Profile("rag")
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Validated
public class QaController {

    private QAService qaService;

    @GetMapping
    public ResponseEntity<AnswerResponse> question(@RequestParam("question") @NotBlank(message = "Question cannot be empty") String question) {
        log.info("Received question request: {}", question);
        AnswerResponse answerResponse = qaService.getAnswer(question);
        return ResponseEntity.ok(answerResponse);
    }

}
