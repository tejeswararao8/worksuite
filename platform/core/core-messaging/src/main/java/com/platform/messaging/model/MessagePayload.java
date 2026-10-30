package com.platform.messaging.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private String type;
    private String sourceModule;
    private String tenantId;
    private Map<String, Object> data;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String priority;
}
