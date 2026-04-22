package com.lostandfound.app.service;

import com.lostandfound.app.dto.request.CreateReportRequest;
import com.lostandfound.app.dto.request.ReportActionRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.ReportResponse;
import com.lostandfound.app.model.ReportStatus;
import com.lostandfound.app.model.ReportTargetType;
import com.lostandfound.app.security.CustomUserDetails;

public interface ReportService {


    public void report(CreateReportRequest request, CustomUserDetails userDetails);


    public PageResponse<ReportResponse.ReportDto> getAllReports(
            int page,
            int size,
            ReportStatus status,
            ReportTargetType targetType
    );

    public void resolveReport(Long reportId, ReportActionRequest request);

    public void rejectReport(Long reportId, ReportActionRequest request);
}
