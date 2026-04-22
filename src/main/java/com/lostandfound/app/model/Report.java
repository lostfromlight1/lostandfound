package com.lostandfound.app.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reports")
@Getter
@Setter
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who reported
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    // what is reported
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType; // POST, COMMENT, USER

    private Long targetId; // id of post/comment/user

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    private String description;

    @Enumerated(EnumType.STRING)
    private ReportStatus status; // PENDING, RESOLVED, REJECTED
}