package root.cyb.mh.skylink_media_service.infrastructure.storage;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class SystemCommandExecutor {
    
    public void convertToWebP(Path inputPath, Path outputPath, int quality) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
            "cwebp", 
            "-q", String.valueOf(quality),
            "-metadata", "all",
            inputPath.toString(),
            "-o", outputPath.toString()
        );
        
        Process process = pb.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("WebP conversion failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("WebP conversion interrupted", e);
        }
    }
}
