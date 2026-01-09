package com.my.spring.ai.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDefinitionResponse {

    private String name;

    private  String description;

    private  String inputSchema;

}
