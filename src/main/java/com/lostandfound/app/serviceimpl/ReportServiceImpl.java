package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.CreateReportRequest;
import com.lostandfound.app.dto.request.ReportActionRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.ReportResponse;
import com.lostandfound.app.exception.AppException;
import com.lostandfound.app.exception.ErrorCode;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.CommentRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.ReportRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.NotificationService;
import com.lostandfound.app.service.ReportService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.lostandfound.app.util.ReportSpecification.hasStatus;
import static com.lostandfound.app.util.ReportSpecification.hasTargetType;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void report(CreateReportRequest request, CustomUserDetails userDetails) {
        Long userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        boolean alreadyReported = reportRepository
                .existsByReportedByIdAndTargetTypeAndTargetId(
                        userId,
                        request.targetType(),
                        request.targetId()
                );

        if (alreadyReported) {
            throw new AppException(ErrorCode.BUSINESS_ERROR, "You have already reported this item");
        }

        switch (request.targetType()) {
            case POST -> postRepository.findById(request.targetId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

            case COMMENT -> commentRepository.findById(request.targetId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment not found"));

            case USER -> userRepository.findById(request.targetId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        }

        Report report = new Report();
        report.setReportedBy(user);
        report.setTargetType(request.targetType());
        report.setTargetId(request.targetId());
        report.setReason(request.reason());
        report.setDescription(request.description());
        report.setStatus(ReportStatus.PENDING);

        reportRepository.save(report);

        // Notify Admins
        notificationService.notifyReportSubmitted(
                report.getId(),
                user.getDisplayName(),
                request.targetType().toString(),
                request.targetId()
        );

        if (request.targetType() == ReportTargetType.POST) {
            long count = reportRepository.countByTargetTypeAndTargetId(
                    ReportTargetType.POST,
                    request.targetId()
            );

            // Auto-hide post if reported 5 or more times
            if (count >= 5) {
                Post post = postRepository.findById(request.targetId()).orElseThrow();
                post.setStatus(PostStatus.HIDDEN);
                postRepository.save(post);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportResponse.ReportDto> getAllReports(
            int page,
            int size,
            ReportStatus status,
            ReportTargetType targetType
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Report> spec = Specification.allOf(
                hasStatus(status),
                hasTargetType(targetType)
        );

        Page<Report> reportPage = reportRepository.findAll(spec, pageable);

        List<ReportResponse.ReportDto> content = reportPage.getContent()
                .stream()
                .map(this::mapToDto)
                .toList();

        return new PageResponse<>(
                content,
                reportPage.getNumber(),
                reportPage.getSize(),
                reportPage.getTotalElements(),
                reportPage.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void resolveReport(Long reportId, ReportActionRequest request) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new AppException(ErrorCode.BUSINESS_ERROR, "This report has already been processed");
        }

        switch (report.getTargetType()) {
            case POST -> {
                Post post = postRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

                post.setStatus(PostStatus.HIDDEN);
                postRepository.save(post);
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment not found"));

                comment.softDelete();
                commentRepository.save(comment);
            }
            case USER -> {
                User user = userRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

                user.setActive(false);
                userRepository.save(user);
            }
        }

        report.setStatus(ReportStatus.RESOLVED);
        report.setAdminNote(request.adminNote());

        reportRepository.save(report);

        // Notify the user who submitted the report
        notificationService.notifyReportResolved(
                report.getId(),
                report.getReportedBy().getId(),
                report.getTargetType().toString()
        );
    }

    @Override
    @Transactional
    public void rejectReport(Long reportId, ReportActionRequest request) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new AppException(ErrorCode.BUSINESS_ERROR, "This report has already been processed");
        }

        report.setStatus(ReportStatus.REJECTED);
        report.setAdminNote(request.adminNote());

        reportRepository.save(report);

        // Notify the user who submitted the report
        notificationService.notifyReportRejected(
                report.getId(),
                report.getReportedBy().getId(),
                report.getTargetType().toString()
        );
    }

    @Override
    @Transactional
    public void restoreTarget(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));

        switch (report.getTargetType()) {
            case POST -> {
                Post post = entityManager.find(Post.class, report.getTargetId());
                if (post == null) throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found in database");

                post.setStatus(PostStatus.OPEN);
                post.setActive(true);
                post.setDeletedAt(null);

                entityManager.merge(post);
            }
            case COMMENT -> {
                Comment comment = entityManager.find(Comment.class, report.getTargetId());
                if (comment == null) throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment not found in database");

                comment.setActive(true);
                comment.setDeletedAt(null);

                entityManager.merge(comment);
            }
            case USER -> {
                User user = entityManager.find(User.class, report.getTargetId());
                if (user == null) throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found in database");

                user.setActive(true);
                user.setDeletedAt(null);
                user.setIsLocked(false);

                entityManager.merge(user);
            }
        }

        report.setStatus(ReportStatus.REJECTED);
        report.setAdminNote("Restored by admin");
        reportRepository.save(report);

        // Notify the user who submitted the report that their report was rejected (since the item was restored)
        notificationService.notifyReportRejected(
                report.getId(),
                report.getReportedBy().getId(),
                report.getTargetType().toString()
        );
    }

    private ReportResponse.ReportDto mapToDto(Report report) {
        return new ReportResponse.ReportDto(
                report.getId(),
                report.getReportedBy().getDisplayName(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}