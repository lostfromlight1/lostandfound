package com.lostandfound.app.serviceimpl;

import com.lostandfound.app.dto.request.CreateReportRequest;
import com.lostandfound.app.dto.response.PageResponse;
import com.lostandfound.app.dto.response.ReportResponse;
import com.lostandfound.app.model.*;
import com.lostandfound.app.repository.CommentRepository;
import com.lostandfound.app.repository.PostRepository;
import com.lostandfound.app.repository.ReportRepository;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.security.CustomUserDetails;
import com.lostandfound.app.service.ReportService;
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
protected final CommentRepository commentRepository;


    @Override
    @Transactional
    public void report(CreateReportRequest request, CustomUserDetails userDetails) {

        Long userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🚫 1. Prevent duplicate report
        boolean alreadyReported = reportRepository
                .existsByReportedByIdAndTargetTypeAndTargetId(
                        userId,
                        request.targetType(),
                        request.targetId()
                );

        if (alreadyReported) {
            throw new RuntimeException("You already reported this");
        }

        // 🔍 2. Validate target exists
        switch (request.targetType()) {
            case POST -> postRepository.findById(request.targetId())
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            case COMMENT -> commentRepository.findById(request.targetId())
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            case USER -> userRepository.findById(request.targetId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        // 🧱 3. Save report
        Report report = new Report();
        report.setReportedBy(user);
        report.setTargetType(request.targetType());
        report.setTargetId(request.targetId());
        report.setReason(request.reason());
        report.setDescription(request.description());
        report.setStatus(ReportStatus.PENDING);

        reportRepository.save(report);

        // 🔥 4. AUTO ACTION (only for POST)
        if (request.targetType() == ReportTargetType.POST) {

            long count = reportRepository.countByTargetTypeAndTargetId(
                    ReportTargetType.POST,
                    request.targetId()
            );

            if (count >= 5) {
                Post post = postRepository.findById(request.targetId()).orElseThrow();

                post.setStatus(PostStatus.HIDDEN); // 👈 you need this enum
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

        Specification<Report> spec = Specification
                .where(hasStatus(status))
                .and(hasTargetType(targetType));

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
