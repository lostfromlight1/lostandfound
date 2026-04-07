package com.lostandfound.app.security;

import com.lostandfound.app.model.User;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component("guard")
public class ResourceGuard {

  public void checkOwner(Long ownerId) {
    if (ownerId == null) {
      throw deny("Resource owner ID is null");
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof User user)) {
      throw deny("User not authenticated or invalid principal");
    }

    if (!user.getId().equals(ownerId)) {
      log.warn("[{}] Access Denied: User {} attempted to access resource owned by {}",
              getTraceId(), user.getId(), ownerId);
      throw deny("User is not the owner of this resource");
    }
  }

  public void checkOwnerOrAdmin(Long ownerId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw deny("User not authenticated");
    }

    // Check if the user has the ADMIN role
    boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (isAdmin) {
      log.debug("[{}] Admin access granted for resource owned by {}", getTraceId(), ownerId);
      return; // Admins bypass the ownership check
    }

    // If not admin, verify ownership
    checkOwner(ownerId);
  }

  private AccessDeniedException deny(String reason) {
    return new AccessDeniedException(reason);
  }

  private String getTraceId() {
    return Objects.toString(MDC.get("traceId"), "SYSTEM");
  }
}