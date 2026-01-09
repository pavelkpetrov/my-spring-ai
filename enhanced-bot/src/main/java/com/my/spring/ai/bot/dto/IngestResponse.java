package com.my.spring.ai.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class IngestResponse {
    private int chunksCount;
    private int chunkSize;
    private String status;
}
