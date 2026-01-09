package com.coherentsolutions.homework.week1.service;

import com.coherentsolutions.homework.week1.dto.AnswerResponse;

public interface QAService {

    AnswerResponse getAnswer(String question);
}
