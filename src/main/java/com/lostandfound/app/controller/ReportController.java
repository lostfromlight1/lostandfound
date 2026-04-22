package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.CheckSecurity;
import com.lostandfound.app.annotation.CurrentUser;
import com.lostandfound.app.dto.request.CreateReportRequest;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.ReportResponse;
import com.lostandfound.app.model.ReportStatus;
import com.lostandfound.app.model.ReportTargetType;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/")
public class ReportController {
    private final ReportService  reportService;

    @PostMapping("/reports")
    public ResponseEntity<BaseResponse<Void>> report(
            @RequestBody @Valid CreateReportRequest request,
            @CurrentUser CustomUserDetails user
    ) {
        reportService.report(request, user);
        return BaseResponse.success("Reported successfully");
    }


    @GetMapping("/admin/reports")
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

}
