package com.luxeglow.jewelrybackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("No image file selected");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "luxeglow_products")
        );

        Object secureUrl = uploadResult.get("secure_url");

        if (secureUrl == null) {
            throw new IOException("Cloudinary did not return secure_url");
        }

        return secureUrl.toString();
    }

    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String publicId = extractPublicIdFromUrl(imageUrl);

        if (publicId == null || publicId.isBlank()) {
            return;
        }

        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/");
            int uploadIndex = -1;

            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i])) {
                    uploadIndex = i;
                    break;
                }
            }

            if (uploadIndex == -1 || uploadIndex + 1 >= parts.length) {
                return null;
            }

            StringBuilder publicIdBuilder = new StringBuilder();

            for (int i = uploadIndex + 1; i < parts.length; i++) {
                String part = parts[i];

                // Skip version part like v1745678901
                if (i == uploadIndex + 1 && part.matches("v\\d+")) {
                    continue;
                }

                if (publicIdBuilder.length() > 0) {
                    publicIdBuilder.append("/");
                }
                publicIdBuilder.append(part);
            }

            String publicIdWithExtension = publicIdBuilder.toString();

            int lastDotIndex = publicIdWithExtension.lastIndexOf(".");
            if (lastDotIndex != -1) {
                return publicIdWithExtension.substring(0, lastDotIndex);
            }

            return publicIdWithExtension;

        } catch (Exception e) {
            return null;
        }
    }
}