package com.lostandfound.app.controller;

import com.lostandfound.app.annotation.ApiId;
import com.lostandfound.app.dto.response.BaseResponse;
import com.lostandfound.app.dto.response.ImageUploadResponse;
import com.lostandfound.app.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "5. Image Management", description = "Endpoints for securely uploading media files to Cloudinary")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    @ApiId("IMG-001")
    @Operation(summary = "Upload Image", description = "Uploads a MultipartFile to Cloudinary. Requires authentication.")
    public ResponseEntity<BaseResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        log.info("REST request to upload image. Filename: {}, Size: {} bytes", file.getOriginalFilename(), file.getSize());
        ImageUploadResponse response = imageService.uploadImage(file, "lost_and_found_uploads");
        return BaseResponse.success("Image uploaded successfully", response);
    }
}