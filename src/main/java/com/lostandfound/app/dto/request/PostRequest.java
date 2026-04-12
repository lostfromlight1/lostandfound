package com.lostandfound.app.dto.request;

import com.lostandfound.app.model.PostStatus;
import com.lostandfound.app.model.PostType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PostRequest {

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

                @NotBlank(message = "Location is required")
                String location,

                @NotNull(message = "Date is required")
                LocalDate lostFoundDate,

                @Size(max = 255, message = "Contact info must be less than 255 characters")
                String contactInfo,

                BigDecimal reward

        ) {}



        public record UpdatePostRequest(

                @NotBlank(message = "Title is required")
                @Size(max = 255)
                String title,

                @NotBlank(message = "Description is required")
                @Size(max = 2000)
                String description,

                @NotNull(message = "Category is required")
                Long categoryId,


                @NotNull(message = "Type is required")
                PostType type,

                @NotNull(message = "Type is required")
                PostStatus status,



                @NotBlank(message = "Location is required")
                String location,

                @NotNull(message = "Date is required")
                LocalDate lostFoundDate,

                @Size(max = 255)
                String contactInfo,

                Double reward

        ) {}
}