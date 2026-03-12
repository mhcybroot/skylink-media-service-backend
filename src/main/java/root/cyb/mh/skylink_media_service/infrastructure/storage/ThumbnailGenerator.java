package root.cyb.mh.skylink_media_service.infrastructure.storage;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class ThumbnailGenerator {
    
    private final SystemCommandExecutor commandExecutor;
    
    public ThumbnailGenerator(SystemCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }
    
    public void createThumbnail(Path inputPath, Path outputPath, int width, int height) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
            "cwebp",
            "-resize", String.valueOf(width), String.valueOf(height),
            "-q", "60.0",
            inputPath.toString(),
            "-o", outputPath.toString()
        );
        
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Read error output for debugging
                String error = new String(process.getInputStream().readAllBytes());
                throw new IOException("Thumbnail generation failed with exit code: " + exitCode + ". Error: " + error);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thumbnail generation interrupted", e);
        }
    }
}
