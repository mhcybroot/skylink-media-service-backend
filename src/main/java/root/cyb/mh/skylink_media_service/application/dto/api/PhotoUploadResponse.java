package root.cyb.mh.skylink_media_service.application.dto.api;

import java.time.LocalDateTime;
import java.util.List;

public class PhotoUploadResponse {
    private int uploaded;
    private List<PhotoInfo> photos;

    public PhotoUploadResponse() {}

    public PhotoUploadResponse(int uploaded, List<PhotoInfo> photos) {
        this.uploaded = uploaded;
        this.photos = photos;
    }

    public int getUploaded() { return uploaded; }
    public void setUploaded(int uploaded) { this.uploaded = uploaded; }
    
    public List<PhotoInfo> getPhotos() { return photos; }
    public void setPhotos(List<PhotoInfo> photos) { this.photos = photos; }

    public static class PhotoInfo {
        private Long id;
        private String fileName;
        private String originalName;
        private Long fileSize;
        private String thumbnailUrl;
        private LocalDateTime uploadedAt;

        public PhotoInfo() {}

        public PhotoInfo(Long id, String fileName, String originalName, Long fileSize, 
                        String thumbnailUrl, LocalDateTime uploadedAt) {
            this.id = id;
            this.fileName = fileName;
            this.originalName = originalName;
            this.fileSize = fileSize;
            this.thumbnailUrl = thumbnailUrl;
            this.uploadedAt = uploadedAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    }
}
