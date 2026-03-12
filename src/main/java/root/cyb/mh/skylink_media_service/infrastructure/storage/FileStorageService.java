package root.cyb.mh.skylink_media_service.infrastructure.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    private final SystemCommandExecutor commandExecutor;
    private final ThumbnailGenerator thumbnailGenerator;
    
    public FileStorageService(SystemCommandExecutor commandExecutor, ThumbnailGenerator thumbnailGenerator) {
        this.commandExecutor = commandExecutor;
        this.thumbnailGenerator = thumbnailGenerator;
    }
    
    public StorageResult storeFile(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        Path uploadPath = ensureUploadDirectory();
        
        logger.info("Processing file: {} -> {}", file.getOriginalFilename(), fileName);
        
        // Save original temporarily
        Path tempPath = uploadPath.resolve("temp_" + fileName);
        Files.copy(file.getInputStream(), tempPath);
        
        // Convert to WebP
        String webpFileName = fileName.replaceAll("\\.[^.]+$", ".webp");
        Path webpPath = uploadPath.resolve(webpFileName);
        commandExecutor.convertToWebP(tempPath, webpPath, 75);
        logger.info("WebP conversion completed: {}", webpFileName);
        
        // Generate thumbnail from original temp file
        Path thumbnailPath = uploadPath.resolve("thumb_" + webpFileName);
        thumbnailGenerator.createThumbnail(tempPath, thumbnailPath, 200, 200);
        logger.info("Thumbnail generated: thumb_{}", webpFileName);
        
        // Schedule cleanup of original
        scheduleCleanup(tempPath);
        
        return new StorageResult(webpFileName, webpPath.toString(), thumbnailPath.toString());
    }
    
    private String generateFileName(String originalName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + originalName;
    }
    
    private Path ensureUploadDirectory() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }
    
    private void scheduleCleanup(Path tempPath) {
        CompletableFuture.delayedExecutor(24 * 60 * 60, java.util.concurrent.TimeUnit.SECONDS)
            .execute(() -> {
                try {
                    Files.deleteIfExists(tempPath);
                    logger.info("Cleaned up temp file: {}", tempPath.getFileName());
                } catch (IOException e) {
                    logger.error("Failed to cleanup temp file: {}", tempPath.getFileName(), e);
                }
            });
    }
    
    public static class StorageResult {
        private final String fileName;
        private final String filePath;
        private final String thumbnailPath;
        
        public StorageResult(String fileName, String filePath, String thumbnailPath) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.thumbnailPath = thumbnailPath;
        }
        
        public String getFileName() { return fileName; }
        public String getFilePath() { return filePath; }
        public String getThumbnailPath() { return thumbnailPath; }
    }
}
