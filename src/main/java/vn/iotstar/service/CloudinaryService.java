package vn.iotstar.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            Cloudinary cloudinary
    ) {
        this.cloudinary = cloudinary;
    }

    public UploadResult upload(
            MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType =
            file.getContentType();

        if (contentType == null
                || !contentType.startsWith("image/")) {

            throw new IllegalArgumentException(
                "Chỉ được upload file hình ảnh."
            );
        }

        Map<?, ?> result =
            cloudinary
                .uploader()
                .upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                        "folder",
                        "spring-security-demo/products",
                        "resource_type",
                        "image"
                    )
                );

        return new UploadResult(
            String.valueOf(
                result.get("secure_url")
            ),
            String.valueOf(
                result.get("public_id")
            )
        );
    }

    public void delete(
            String publicId
    ) throws IOException {

        if (publicId == null
                || publicId.isBlank()) {
            return;
        }

        cloudinary
            .uploader()
            .destroy(
                publicId,
                ObjectUtils.asMap(
                    "resource_type",
                    "image"
                )
            );
    }

    public record UploadResult(
        String url,
        String publicId
    ) {
    }
}