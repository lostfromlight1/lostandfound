package com.lostandfound.app.dto.request;

import com.lostandfound.app.model.MyanmarCity;
import com.lostandfound.app.model.PostStatus;
import com.lostandfound.app.model.PostType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PostRequest {

        public record ImageRequest(
                @NotBlank(message = "Image URL is required")
                String url,

                @NotBlank(message = "Image Public ID is required")
                String publicId,

                Integer sortOrder
        ) {}

        public record CreatePostRequest(
                @NotBlank(message = "Title is required")
                @Size(max = 255, message = "Title must be less than 255 characters")
                String title,

                @NotBlank(message = "Description is required")
                @Size(max = 2000, message = "Description must be less than 2000 characters")
                String description,

                @NotNull(message = "Type is required")
                PostType type,

                PostStatus status,

                @NotNull(message = "Category is required")
                Long categoryId,

                @NotNull(message = "City is required")
                MyanmarCity city,

                @NotBlank(message = "Location detail are require")
                @Size(max = 255, message = "Location details are too long")
                String locationDetails,

                Double latitude,

                Double longitude,

                @NotNull(message = "Date is required")
                LocalDate lostFoundDate,

                @Size(max = 255, message = "Contact info must be less than 255 characters")
                String contactInfo,

                BigDecimal reward,

                List<ImageRequest> images
        ) {}

        public record UpdatePostRequest(
                @NotBlank(message = "Title is required")
                @Size(max = 255, message = "Title must be less than 255 characters")
                String title,

                @NotBlank(message = "Description is required")
                @Size(max = 2000, message = "Description must be less than 2000 characters")
                String description,

                @NotNull(message = "Type is required")
                PostType type,

                @NotNull(message = "Status is required")
                PostStatus status,

                @NotNull(message = "Category is required")
                Long categoryId,

                @NotNull(message = "City is required")
                MyanmarCity city,

                @NotBlank(message = "Location detail are require")
                @Size(max = 255, message = "Location details are too long")
                String locationDetails,

                Double latitude,

                Double longitude,

                @NotNull(message = "Date is required")
                LocalDate lostFoundDate,

                @Size(max = 255, message = "Contact info must be less than 255 characters")
                String contactInfo,

                BigDecimal reward,

                List<ImageRequest> images
        ) {}
}