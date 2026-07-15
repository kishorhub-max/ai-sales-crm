package com.aisalescrm.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailGeneratorResponse {

    private String emailType;

    private String subject;

    private String body;

    private List<String> suggestedSubjectLines;
}