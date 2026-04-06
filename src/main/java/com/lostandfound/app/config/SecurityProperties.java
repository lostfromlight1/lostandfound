package com.lostandfound.app.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
    List<String> publicRoutes, String apiVersion, Cors cors, ApiKey apiKey) {
  public record Cors(
      List<String> allowedOrigins,
      List<String> allowedMethods,
      List<String> allowedHeaders,
      List<String> exposedHeaders) {}

  public record ApiKey(String header, String value) {}
}
