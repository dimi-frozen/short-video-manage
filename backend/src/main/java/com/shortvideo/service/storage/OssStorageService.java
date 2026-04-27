package com.shortvideo.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface OssStorageService {
    UploadResult uploadPublic(String folder, MultipartFile file);

    class UploadResult {
        private final String objectKey;
        private final String url;

        public UploadResult(String objectKey, String url) {
            this.objectKey = objectKey;
            this.url = url;
        }

        public String getObjectKey() {
            return objectKey;
        }

        public String getUrl() {
            return url;
        }
    }
}

