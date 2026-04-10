package com.lostandfound.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


public record CategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        String name


) {
}
