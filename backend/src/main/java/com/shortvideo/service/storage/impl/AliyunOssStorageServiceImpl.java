package com.shortvideo.service.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.shortvideo.service.storage.OssStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class AliyunOssStorageServiceImpl implements OssStorageService {

    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.bucket:}")
    private String bucket;

    @Value("${aliyun.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.public-url-prefix:}")
    private String publicUrlPrefix;

    @Value("${aliyun.oss.public-read:true}")
    private boolean publicRead;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public UploadResult uploadPublic(String folder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String original = file.getOriginalFilename();
        String ext = getExtension(original);
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        String objectKey = (StringUtils.hasText(folder) ? folder.trim() : "files") + "/" + dateDir + "/" + name;

        // 如果没有配置OSS，降级为本地存储
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucket) || !StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret) || accessKeyId.startsWith("${")) {
            return saveToLocal(objectKey, file);
        }

        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try (InputStream in = file.getInputStream()) {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            String ct = file.getContentType();
            if (StringUtils.hasText(ct)) {
                meta.setContentType(ct);
            }

            PutObjectRequest req = new PutObjectRequest(bucket, objectKey, in, meta);
            client.putObject(req);
            if (publicRead) {
                try {
                    client.setObjectAcl(bucket, objectKey, CannedAccessControlList.PublicRead);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("OSS上传失败: " + e.getMessage());
        } finally {
            try {
                client.shutdown();
            } catch (Exception ignored) {
            }
        }

        String url = buildPublicUrl(objectKey);
        return new UploadResult(objectKey, url);
    }

    private UploadResult saveToLocal(String objectKey, MultipartFile file) {
        try {
            Path targetLocation = Paths.get(uploadDir, objectKey).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation.getParent());
            file.transferTo(targetLocation.toFile());
            
            // 构建本地访问URL，假设前端在同域或者使用代理
            String url = "http://localhost:8080/uploads/" + objectKey;
            return new UploadResult(objectKey, url);
        } catch (Exception e) {
            throw new RuntimeException("本地保存失败: " + e.getMessage());
        }
    }

    private String buildPublicUrl(String objectKey) {
        String prefix = publicUrlPrefix;
        if (!StringUtils.hasText(prefix)) {
            String ep = endpoint;
            if (ep.toLowerCase(Locale.ROOT).startsWith("http")) {
                ep = ep.replaceFirst("^https?://", "");
            }
            prefix = "https://" + bucket + "." + ep;
        }
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String k = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        return prefix + "/" + k;
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        if (idx < 0) {
            return "";
        }
        return filename.substring(idx).toLowerCase(Locale.ROOT);
    }
}
