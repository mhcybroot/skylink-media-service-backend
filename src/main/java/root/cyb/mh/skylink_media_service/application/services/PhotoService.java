package root.cyb.mh.skylink_media_service.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import root.cyb.mh.skylink_media_service.domain.entities.Photo;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.PhotoRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoService {
    
    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    public Photo uploadPhoto(MultipartFile file, Long projectId, Long contractorId) throws IOException {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        Contractor contractor = contractorRepository.findById(contractorId)
            .orElseThrow(() -> new RuntimeException("Contractor not found"));
        
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        String originalName = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + originalName;
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        // Save photo record
        Photo photo = new Photo(fileName, filePath.toString(), originalName, file.getSize(), project, contractor);
        return photoRepository.save(photo);
    }
    
    public List<Photo> getProjectPhotos(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        return photoRepository.findByProject(project);
    }
    
    public List<Photo> getContractorPhotos(Long contractorId) {
        Contractor contractor = contractorRepository.findById(contractorId)
            .orElseThrow(() -> new RuntimeException("Contractor not found"));
        return photoRepository.findByContractor(contractor);
    }
}
