package root.cyb.mh.skylink_media_service.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import root.cyb.mh.skylink_media_service.domain.entities.Photo;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.PhotoRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.storage.FileStorageService;

import java.io.IOException;
import java.util.List;

@Service
public class PhotoService {
    
    private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);
    
    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public Photo uploadPhoto(MultipartFile file, Long projectId, Long contractorId) throws IOException {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        Contractor contractor = contractorRepository.findById(contractorId)
            .orElseThrow(() -> new RuntimeException("Contractor not found"));
        
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        logger.info("Uploading photo for project {} by contractor {}", projectId, contractorId);
        
        FileStorageService.StorageResult result = fileStorageService.storeFile(file);
        
        Photo photo = new Photo(result.getFileName(), result.getFilePath(), 
            file.getOriginalFilename(), file.getSize(), project, contractor);
        photo.setThumbnailPath(result.getThumbnailPath());
        photo.setWebpPath(result.getFilePath());
        photo.setIsOptimized(true);
        photo.setOptimizedAt(java.time.LocalDateTime.now());
        photo.setOptimizationStatus("COMPLETED");
        
        Photo savedPhoto = photoRepository.save(photo);
        logger.info("Photo saved with ID: {} (optimized)", savedPhoto.getId());
        
        return savedPhoto;
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
