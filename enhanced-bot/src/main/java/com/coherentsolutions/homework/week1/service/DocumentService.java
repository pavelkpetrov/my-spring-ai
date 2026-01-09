package com.coherentsolutions.homework.week1.service;


import com.coherentsolutions.homework.week1.dto.IngestResponse;

public interface DocumentService {
    IngestResponse ingestDocument(String content);

}
