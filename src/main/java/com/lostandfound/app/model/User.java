package com.lostandfound.app.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SQLRestriction("is_active = true")
public class User extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "token", length = 512)
    private String token;

    // --- Profile Data ---

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "contact_info")
    private String contactInfo;

    /* * We will map this fully when we create the Image entity next!
     * * @OneToOne(fetch = FetchType.LAZY)
     * @JoinColumn(name = "profile_image_id")
     * private Image profileImage;
     */
}