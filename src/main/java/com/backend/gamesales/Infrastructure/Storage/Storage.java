package com.backend.gamesales.Infrastructure.Storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
@Component
public class Storage {
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestTemplate restTemplate = new RestTemplate();

    public String saveImage(MultipartFile image, Long gameId) {
        try {
            validateImage(image);
            String extension = getExtension(image);
            String fileName = "games/" + gameId + "/" + UUID.randomUUID() + extension;
            String uploadUrl = buildUrl(fileName);
            HttpHeaders headers = buildHeaders(image.getContentType());
            HttpEntity<byte[]> entity = new HttpEntity<>(image.getBytes(), headers);

            restTemplate.exchange(uploadUrl, HttpMethod.PUT, entity, String.class);
            return buildPublicUrl(fileName);

        } catch (Exception e) {
            throw new RuntimeException("Error uploading image: " + e.getMessage());
        }
    }
    public void deleteImage(String imageUrl) {
        try {
            String path = extractPath(imageUrl);
            String deleteUrl = buildUrl(path);

            HttpHeaders headers = buildHeaders(null);

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        } catch (Exception e) {
            if (e.getMessage().contains("404") || e.getMessage().contains("not found")) {
                System.out.println("Image not found, continuing: " + imageUrl);
                return;
            }
            throw new RuntimeException("Error deleting image: " + e.getMessage());
        }
    }

    public String replaceImage(String oldImageUrl, MultipartFile newImage, Long gameId) {
        deleteImage(oldImageUrl);
        return saveImage(newImage, gameId);
    }
    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Image is required");
        }

        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }
    }

    private HttpHeaders buildHeaders(String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("x-upsert", "true");

        if (contentType != null) {
            headers.setContentType(MediaType.valueOf(contentType));
        }

        return headers;
    }

    private String buildUrl(String fileName) {
        return supabaseUrl.replaceAll("/$", "")
                + "/storage/v1/object/" + bucket + "/" + fileName;
    }

    private String buildPublicUrl(String fileName) {
        return supabaseUrl.replaceAll("/$", "")
                + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }

    private String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            return name.substring(name.lastIndexOf("."));
        }
        return ".jpg";
    }

    private String extractPath(String url) {
        String marker = "/object/public/" + bucket + "/";
        int index = url.indexOf(marker);

        if (index == -1) {
            throw new RuntimeException("Invalid image URL");
        }

        return url.substring(index + marker.length());
    }

    public String saveAvatar(MultipartFile image, Long userId) {
        try {
            validateImage(image);
            String extension = getExtension(image);
            String fileName = "avatars/" + userId + "/" + UUID.randomUUID() + extension;
            String uploadUrl = buildUrl(fileName);
            HttpHeaders headers = buildHeaders(image.getContentType());
            HttpEntity<byte[]> entity = new HttpEntity<>(image.getBytes(), headers);

            restTemplate.exchange(uploadUrl, HttpMethod.PUT, entity, String.class);
            return buildPublicUrl(fileName);

        } catch (Exception e) {
            throw new RuntimeException("Error uploading avatar: " + e.getMessage());
        }
    }

    public String replaceAvatar(String oldAvatarUrl, MultipartFile newImage, Long userId) {
        if (oldAvatarUrl != null
                && !oldAvatarUrl.isBlank()
                && oldAvatarUrl.contains("/avatars/")
                && !oldAvatarUrl.contains("default-avatar")) {
            deleteImage(oldAvatarUrl);
        }
        return saveAvatar(newImage, userId);
    }
}
