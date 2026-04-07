package com.lostandfound.app.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

public @interface CheckSecurity {

  public @interface Public {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @PreAuthorize("permitAll()")
    public @interface canRead {}
  }

  public @interface Admin {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @PreAuthorize("hasRole('ADMIN')")
    public @interface isRequired {}
  }

  public @interface Posts {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    // Grants access if user is ADMIN OR if they own the specific post
    @PreAuthorize("hasRole('ADMIN') or @authConfig.isPostOwner(#postId)")
    public @interface canManage {}
  }

  public @interface Comments {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    // Grants access if user is ADMIN OR if they own the specific comment
    @PreAuthorize("hasRole('ADMIN') or @authConfig.isCommentOwner(#commentId)")
    public @interface canManage {}
  }
}
