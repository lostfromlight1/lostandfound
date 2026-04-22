package com.lostandfound.app.util;

import com.lostandfound.app.model.Report;
import com.lostandfound.app.model.ReportStatus;
import com.lostandfound.app.model.ReportTargetType;
import org.springframework.data.jpa.domain.Specification;

public class ReportSpecification {

    public static Specification<Report> hasStatus(ReportStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Report> hasTargetType(ReportTargetType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("targetType"), type);
    }
}