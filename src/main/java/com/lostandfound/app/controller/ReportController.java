package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.annotation.CurrentUser;
import com.lostandfound.app.dto.request.CreateReportRequest;
import com.lostandfound.app.dto.request.ReportActionRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.ReportResponse;
import com.lostandfound.app.model.ReportStatus;
import com.lostandfound.app.model.ReportTargetType;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report Management", description = "Endpoints for submitting, filtering, and resolving user/content reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @CheckSecurity.Authenticated.isRequired
    @ApiId("RPT-001")
    @Operation(summary = "Submit Report", description = "Allows an authenticated user to submit a report against a post, comment, or user.")
    public ResponseEntity<BaseResponse<Void>> report(
            @Valid @RequestBody CreateReportRequest request,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails userDetails) {

        log.info("REST request to submit a report by user ID: {}, Target: {} ID: {}",
                userDetails.getId(), request.targetType(), request.targetId());

        reportService.report(request, userDetails);
        return BaseResponse.success("Reported successfully");
    }

    @GetMapping
    @CheckSecurity.Admin.isRequired
    @ApiId("RPT-002")
    @Operation(summary = "Get All Reports", description = "Fetches a paginated list of reports with optional filtering by status and target type. Restricted to administrators.")
    public ResponseEntity<BaseResponse<PageResponse<ReportResponse.ReportDto>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportTargetType targetType,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails userDetails) {

        log.info("REST request to fetch reports list by admin user ID: {}. Page: {}, Size: {}, Status: {}, TargetType: {}",
                userDetails.getId(), page, size, status, targetType);

        PageResponse<ReportResponse.ReportDto> reports = reportService.getAllReports(page, size, status, targetType);
        return BaseResponse.success("Reports fetched successfully", reports);
    }

    @PostMapping("/{id}/resolve")
    @CheckSecurity.Admin.isRequired
    @ApiId("RPT-003")
    @Operation(summary = "Resolve Report", description = "Resolves a report and applies the necessary automated penalty to the target. Restricted to administrators.")
    public ResponseEntity<BaseResponse<Void>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportActionRequest request,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails userDetails) {

        log.info("REST request to resolve report ID: {} by admin user ID: {}", id, userDetails.getId());

        reportService.resolveReport(id, request);
        return BaseResponse.success("Report resolved successfully");
    }

    @PostMapping("/{id}/reject")
    @CheckSecurity.Admin.isRequired
    @ApiId("RPT-004")
    @Operation(summary = "Reject Report", description = "Rejects/dismisses a report without applying a penalty. Restricted to administrators.")
    public ResponseEntity<BaseResponse<Void>> rejectReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportActionRequest request,
            @Parameter(hidden = true) @CurrentUser CustomUserDetails userDetails) {

        log.info("REST request to reject report ID: {} by admin user ID: {}", id, userDetails.getId());

        reportService.rejectReport(id, request);
        return BaseResponse.success("Report rejected successfully");
    }
}