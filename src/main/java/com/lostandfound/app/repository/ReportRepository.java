package com.lostandfound.app.repository;

import com.lostandfound.app.model.Report;
import com.lostandfound.app.model.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportRepository extends JpaRepository<Report, Long> , JpaSpecificationExecutor<Report> {

    // 🚫 prevent duplicate report
    boolean existsByReportedByIdAndTargetTypeAndTargetId(
            Long userId,
            ReportTargetType targetType,
            Long targetId
    );

    // 🔥 count reports
    long countByTargetTypeAndTargetId(
            ReportTargetType targetType,
            Long targetId
    );




}