package root.cyb.mh.skylink_media_service.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import root.cyb.mh.skylink_media_service.application.services.PhotoService;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAssignmentRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/contractor")
public class ContractorController {
    
    @Autowired
    private PhotoService photoService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication,
                           @RequestParam(required = false) String projectSearch) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user instanceof Contractor contractor) {
            List<ProjectAssignment> assignments;
            
            if (projectSearch != null && !projectSearch.trim().isEmpty()) {
                assignments = projectAssignmentRepository.searchAssignmentsByContractor(contractor, projectSearch);
            } else {
                assignments = projectAssignmentRepository.findByContractor(contractor);
            }
            
            // Add photo counts for each assignment
            Map<Long, Long> photoCounts = new HashMap<>();
            for (ProjectAssignment assignment : assignments) {
                long photoCount = photoService.getProjectPhotos(assignment.getProject().getId()).size();
                photoCounts.put(assignment.getProject().getId(), photoCount);
            }
            
            model.addAttribute("assignments", assignments);
            model.addAttribute("photoCounts", photoCounts);
            model.addAttribute("projectSearch", projectSearch);
        }
        return "contractor/dashboard";
    }
    
    @GetMapping("/project/{projectId}/photos")
    public String viewProjectPhotos(@PathVariable Long projectId, Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user instanceof Contractor contractor) {
            // Verify contractor is assigned to this project
            Optional<ProjectAssignment> assignment = projectAssignmentRepository
                .findByProjectAndContractor(
                    projectRepository.findById(projectId).orElse(null), 
                    contractor
                );
            
            if (assignment.isPresent()) {
                model.addAttribute("project", assignment.get().getProject());
                model.addAttribute("photos", photoService.getProjectPhotos(projectId));
                return "contractor/project-photos";
            }
        }
        return "redirect:/contractor/dashboard";
    }
    
    @GetMapping("/upload-photo/{projectId}")
    public String uploadPhotoForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("projectId", projectId);
        return "contractor/upload-photo";
    }
    
    @PostMapping("/upload-photo/{projectId}")
    public String uploadPhoto(@PathVariable Long projectId, 
                             @RequestParam("files") MultipartFile[] files,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user instanceof Contractor contractor) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        photoService.uploadPhoto(file, projectId, contractor.getId());
                    }
                }
                redirectAttributes.addFlashAttribute("success", "Photos uploaded successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/contractor/dashboard";
    }
}
