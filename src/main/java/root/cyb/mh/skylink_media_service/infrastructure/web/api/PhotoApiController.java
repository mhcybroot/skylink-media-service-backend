package root.cyb.mh.skylink_media_service.infrastructure.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import root.cyb.mh.skylink_media_service.application.dto.api.PhotoUploadResponse;
import root.cyb.mh.skylink_media_service.application.services.PhotoService;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.Photo;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/contractor/projects/{projectId}/photos")
@Tag(name = "Photos", description = "Photo upload operations")
@SecurityRequirement(name = "bearer-jwt")
public class PhotoApiController {
    private final PhotoService photoService;
    private final ProjectRepository projectRepository;
    private final ContractorRepository contractorRepository;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");

    public PhotoApiController(PhotoService photoService,
                             ProjectRepository projectRepository,
                             ContractorRepository contractorRepository) {
        this.photoService = photoService;
        this.projectRepository = projectRepository;
        this.contractorRepository = contractorRepository;
    }

    @PostMapping
    @Operation(summary = "Upload photos", description = "Upload one or more photos to a project")
    public ResponseEntity<PhotoUploadResponse> uploadPhotos(
            @PathVariable Long projectId,
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication) {
        
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files provided");
        }

        if (files.length > 10) {
            throw new IllegalArgumentException("Maximum 10 files allowed per request");
        }

        Contractor contractor = getAuthenticatedContractor(authentication);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (!project.isAssignedToContractor(contractor)) {
            throw new AccessDeniedException("You are not assigned to this project");
        }

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File " + file.getOriginalFilename() + " exceeds maximum size of 10MB");
            }
            if (!ALLOWED_TYPES.contains(file.getContentType())) {
                throw new IllegalArgumentException("File " + file.getOriginalFilename() + " has invalid type. Only JPEG, PNG, and WebP are allowed");
            }
        }

        List<Photo> uploadedPhotos = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Photo photo = photoService.uploadPhoto(file, project.getId(), contractor.getId());
                uploadedPhotos.add(photo);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to upload file: " + file.getOriginalFilename(), e);
            }
        }

        List<PhotoUploadResponse.PhotoInfo> photoInfos = uploadedPhotos.stream()
                .map(photo -> new PhotoUploadResponse.PhotoInfo(
                        photo.getId(),
                        photo.getFileName(),
                        photo.getOriginalName(),
                        photo.getFileSize(),
                        "/api/v1/files/" + (photo.getThumbnailPath() != null ? 
                                photo.getThumbnailPath().substring(photo.getThumbnailPath().lastIndexOf("/") + 1) : 
                                photo.getFileName()),
                        photo.getUploadedAt()
                ))
                .collect(Collectors.toList());

        PhotoUploadResponse response = new PhotoUploadResponse(uploadedPhotos.size(), photoInfos);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Contractor getAuthenticatedContractor(Authentication authentication) {
        String username = authentication.getName();
        return contractorRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Contractor not found"));
    }
}
