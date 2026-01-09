package com.my.spring.ai.bot.service;

import com.my.spring.ai.bot.dto.AnswerResponse;

public interface QAService {

    AnswerResponse getAnswer(String question);
}
