package root.cyb.mh.skylink_media_service.infrastructure.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.PhotoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
public class StorageCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageCleanupService.class);
    
    private final PhotoRepository photoRepository;
    
    public StorageCleanupService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupOldRawImages() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        logger.info("Starting cleanup of raw images older than {}", cutoff);
        
        int cleanedCount = 0;
        
        photoRepository.findAll().stream()
            .filter(photo -> photo.getUploadedAt().isBefore(cutoff))
            .filter(photo -> photo.getOriginalPath() != null)
            .forEach(photo -> {
                try {
                    Path originalPath = Paths.get(photo.getOriginalPath());
                    if (Files.exists(originalPath)) {
                        Files.delete(originalPath);
                        photo.setOriginalPath(null);
                        photoRepository.save(photo);
                    }
                } catch (IOException e) {
                    logger.error("Failed to cleanup file: {}", photo.getOriginalPath(), e);
                }
            });
        
        logger.info("Cleanup completed. Processed {} files", cleanedCount);
    }
}
