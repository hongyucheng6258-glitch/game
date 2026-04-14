package com.dianjing.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArbitrationRequest {
    @NotNull(message = "投诉ID不能为空")
    private Long complaintId;

    @NotNull(message = "仲裁结果不能为空")
    @Size(min = 10, max = 2000, message = "仲裁结果长度应在10-2000字符之间")
    private String arbitrationResult;

    @NotNull(message = "仲裁决定不能为空")
    private Integer decision; // 0=支持投诉方, 1=支持被投诉方, 2=双方各承担责任
}
