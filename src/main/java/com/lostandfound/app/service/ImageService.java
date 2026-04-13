package com.lostandfound.app.service;

import com.lostandfound.app.dto.response.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    ImageUploadResponse uploadImage(MultipartFile file, String folderName);
    void deleteImage(String publicId);
}