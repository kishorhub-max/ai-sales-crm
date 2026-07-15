package com.aisalescrm.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    @NotBlank(message = "Message is required")
    private String message;
    private List<ChatMessageDto> history;
}