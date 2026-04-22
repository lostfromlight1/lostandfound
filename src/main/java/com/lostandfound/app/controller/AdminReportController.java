package com.lostandfound.app.controller;


import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.dto.request.ReportActionRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.ReportResponse;
import com.lostandfound.app.model.ReportStatus;
import com.lostandfound.app.model.ReportTargetType;
import com.lostandfound.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;




    @GetMapping
    @CheckSecurity.Admin.isRequired
    public ResponseEntity<BaseResponse<PageResponse<ReportResponse.ReportDto>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportTargetType targetType
    ) {

        PageResponse<ReportResponse.ReportDto> response =
                reportService.getAllReports(page, size, status, targetType);

        return BaseResponse.success("Reports fetched successfully", response);
    }

    @PutMapping("/{id}/resolve")
    @CheckSecurity.Admin.isRequired
    public ResponseEntity<?> resolveReport(
            @PathVariable Long id,
            @RequestBody ReportActionRequest request
    ) {
        reportService.resolveReport(id, request);
        return BaseResponse.success("Report resolved successfully");
    }


    @PutMapping("/{id}/reject")
    @CheckSecurity.Admin.isRequired
    public ResponseEntity<?> rejectReport(
            @PathVariable Long id,
            @RequestBody ReportActionRequest request
    ) {
        reportService.rejectReport(id, request);
        return BaseResponse.success("Report rejected");
    }
}


