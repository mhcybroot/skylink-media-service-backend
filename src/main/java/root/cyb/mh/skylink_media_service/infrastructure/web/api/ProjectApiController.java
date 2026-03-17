package root.cyb.mh.skylink_media_service.infrastructure.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import root.cyb.mh.skylink_media_service.application.dto.api.PagedResponse;
import root.cyb.mh.skylink_media_service.application.dto.api.ProjectDetailResponse;
import root.cyb.mh.skylink_media_service.application.dto.api.ProjectListResponse;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAssignmentRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/contractor/projects")
@Tag(name = "Projects", description = "Contractor project operations")
@SecurityRequirement(name = "bearer-jwt")
public class ProjectApiController {
    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final ContractorRepository contractorRepository;

    public ProjectApiController(ProjectRepository projectRepository,
                               ProjectAssignmentRepository assignmentRepository,
                               ContractorRepository contractorRepository) {
        this.projectRepository = projectRepository;
        this.assignmentRepository = assignmentRepository;
        this.contractorRepository = contractorRepository;
    }

    @GetMapping
    @Operation(summary = "List assigned projects", description = "Get paginated list of projects assigned to contractor")
    public ResponseEntity<PagedResponse<ProjectListResponse>> listProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "id,desc") String sort,
            Authentication authentication) {
        
        Contractor contractor = getAuthenticatedContractor(authentication);
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<ProjectAssignment> assignments = assignmentRepository.findByContractor(contractor, pageable);
        
        List<ProjectListResponse> content = assignments.getContent().stream()
                .map(assignment -> {
                    Project project = assignment.getProject();
                    ProjectListResponse response = new ProjectListResponse();
                    response.setId(project.getId());
                    response.setWorkOrderNumber(project.getWorkOrderNumber());
                    response.setLocation(project.getLocation());
                    response.setClientCode(project.getClientCode());
                    response.setDescription(project.getDescription());
                    response.setStatus(project.getStatus().name());
                    response.setDueDate(project.getDueDate());
                    response.setPhotoCount(project.getPhotos() != null ? project.getPhotos().size() : 0);
                    response.setAssignedAt(assignment.getAssignedAt());
                    return response;
                })
                .collect(Collectors.toList());

        PagedResponse.PageMetadata metadata = new PagedResponse.PageMetadata(
                assignments.getNumber(),
                assignments.getSize(),
                assignments.getTotalElements(),
                assignments.getTotalPages()
        );

        return ResponseEntity.ok(new PagedResponse<>(content, metadata));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details", description = "Get detailed information about a specific project")
    public ResponseEntity<ProjectDetailResponse> getProject(
            @PathVariable Long id,
            Authentication authentication) {
        
        Contractor contractor = getAuthenticatedContractor(authentication);
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (!project.isAssignedToContractor(contractor)) {
            throw new AccessDeniedException("You are not assigned to this project");
        }

        ProjectDetailResponse response = new ProjectDetailResponse();
        response.setId(project.getId());
        response.setWorkOrderNumber(project.getWorkOrderNumber());
        response.setLocation(project.getLocation());
        response.setClientCode(project.getClientCode());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus().name());
        response.setPaymentStatus(project.getPaymentStatus().name());
        response.setDueDate(project.getDueDate());
        response.setReceivedDate(project.getReceivedDate());
        response.setWorkType(project.getWorkType());
        response.setAddress(project.getAddress());
        response.setCustomer(project.getCustomer());

        if (project.getPhotos() != null) {
            List<ProjectDetailResponse.PhotoInfo> photos = project.getPhotos().stream()
                    .map(photo -> new ProjectDetailResponse.PhotoInfo(
                            photo.getId(),
                            photo.getFileName(),
                            "/api/v1/files/" + (photo.getThumbnailPath() != null ? 
                                    photo.getThumbnailPath().substring(photo.getThumbnailPath().lastIndexOf("/") + 1) : 
                                    photo.getFileName()),
                            photo.getUploadedAt()
                    ))
                    .collect(Collectors.toList());
            response.setPhotos(photos);
        }

        ProjectAssignment assignment = assignmentRepository.findByProjectAndContractor(project, contractor)
                .orElse(null);
        if (assignment != null) {
            response.setAssignedAt(assignment.getAssignedAt());
        }

        return ResponseEntity.ok(response);
    }

    private Contractor getAuthenticatedContractor(Authentication authentication) {
        String username = authentication.getName();
        return contractorRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Contractor not found"));
    }
}
