package com.bookstore.backend.service;

import com.bookstore.backend.exception.AppException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {
    @Autowired
    private Cloudinary cloudinary;
    public ImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", "bookstore/books",
                    "resource_type", "auto"
            );
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "error uploading photo: " + e.getMessage());
        }
    }

}
