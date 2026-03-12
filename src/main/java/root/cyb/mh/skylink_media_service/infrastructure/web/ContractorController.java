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
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAssignmentRepository;

@Controller
@RequestMapping("/contractor")
public class ContractorController {
    
    @Autowired
    private PhotoService photoService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user instanceof Contractor contractor) {
            model.addAttribute("assignments", projectAssignmentRepository.findByContractor(contractor));
        }
        return "contractor/dashboard";
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
