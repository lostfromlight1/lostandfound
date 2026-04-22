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
@RequestMapping("api/v1/reports")
public class ReportController {
    private final ReportService  reportService;

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> report(
            @RequestBody @Valid CreateReportRequest request,
            @CurrentUser CustomUserDetails user
    ) {
        reportService.report(request, user);
        return BaseResponse.success("Reported successfully");
    }




}
