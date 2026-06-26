package com.lostandfound.app.repository;

import com.lostandfound.app.model.Report;
import com.lostandfound.app.model.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportRepository
    extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

  boolean existsByReportedByIdAndTargetTypeAndTargetId(
      Long userId, ReportTargetType targetType, Long targetId);

  long countByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);
}

