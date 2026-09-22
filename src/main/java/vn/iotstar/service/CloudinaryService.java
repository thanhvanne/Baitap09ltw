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

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String upload(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        Map<?, ?> result =
            cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder",
                    "spring-security-demo"
                )
            );

        return result.get("secure_url").toString();
    }
}