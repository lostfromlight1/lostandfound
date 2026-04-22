package com.lostandfound.app.dto.request;

import com.lostandfound.app.model.ReportReason;
import com.lostandfound.app.model.ReportTargetType;
import jakarta.validation.constraints.NotNull;

public record CreateReportRequest(

        @NotNull
        ReportTargetType targetType,

        @NotNull
        Long targetId,

        @NotNull
        ReportReason reason,

        String description


) {
}
