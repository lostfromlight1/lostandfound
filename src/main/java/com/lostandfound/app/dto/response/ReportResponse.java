package com.lostandfound.app.dto.response;

import com.lostandfound.app.model.ReportReason;
import com.lostandfound.app.model.ReportStatus;
import com.lostandfound.app.model.ReportTargetType;

import java.time.LocalDateTime;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;


public class ReportResponse {

    public record ReportDto(
            Long id,
            String reportedByName,
            ReportTargetType targetType,
            Long targetId,
            ReportReason reason,
            String description,
            ReportStatus status,
            LocalDateTime createdAt


    ) {
    }

}
