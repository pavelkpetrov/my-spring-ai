package com.my.spring.ai.bot.service;


import com.my.spring.ai.bot.dto.IngestResponse;

public interface DocumentService {
    IngestResponse ingestDocument(String content);

}
