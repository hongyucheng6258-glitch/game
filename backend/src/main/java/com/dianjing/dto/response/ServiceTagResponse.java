package com.dianjing.dto.response;

import com.dianjing.entity.ServiceTag;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceTagResponse {
    private Long id;
    private String name;
    private Long useCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ServiceTagResponse fromEntity(ServiceTag tag, Long useCount) {
        ServiceTagResponse resp = new ServiceTagResponse();
        resp.setId(tag.getId());
        resp.setName(tag.getName());
        resp.setUseCount(useCount);
        resp.setCreatedAt(tag.getCreatedAt());
        resp.setUpdatedAt(tag.getUpdatedAt());
        return resp;
    }
}
