package com.lostandfound.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLRestriction;

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
  private MyanmarCity city;

  private String locationDetails;

  private Double latitude;

  private Double longitude;

  private LocalDate lostFoundDate;

  private String contactInfo;

  private BigDecimal reward;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PostImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PostLike> likes = new ArrayList<>();

  @Formula("(SELECT COUNT(pl.id) FROM post_likes pl WHERE pl.post_id = id)")
  private Long likeCount = 0L;

  @Formula(
      "(SELECT COUNT(c.id) FROM comments c WHERE c.post_id = id AND c.is_active = true) +"
          + " COALESCE((SELECT COUNT(r.id) FROM replies r INNER JOIN comments c ON r.comment_id ="
          + " c.id WHERE c.post_id = id AND r.is_active = true AND c.is_active = true), 0)")
  private Long commentCount = 0L;

  public void addImage(PostImage image) {
    images.add(image);
    image.setPost(this);
  }

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PostBookmark> bookmarks = new ArrayList<>();
}

