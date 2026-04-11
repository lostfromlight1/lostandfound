package com.lostandfound.app.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
@SQLRestriction("is_active = true")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;


    @Enumerated(EnumType.STRING)
    private PostType postType;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Enumerated(EnumType.STRING)
    private MyanmarCity location;

    private LocalDate lostFoundDate;

    private String contactInfo;

    private BigDecimal reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // FK in DB
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;


}
