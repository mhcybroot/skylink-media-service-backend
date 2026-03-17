package root.cyb.mh.skylink_media_service.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import root.cyb.mh.skylink_media_service.application.services.ChatService;
import root.cyb.mh.skylink_media_service.application.services.PhotoService;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectMessage;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAssignmentRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.application.usecases.OpenProjectUseCase;
import root.cyb.mh.skylink_media_service.application.usecases.CompleteProjectUseCase;
import root.cyb.mh.skylink_media_service.application.usecases.GetContractorProjectsUseCase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private OpenProjectUseCase openProjectUseCase;

    @Autowired
    private CompleteProjectUseCase completeProjectUseCase;

    @Autowired
    private GetContractorProjectsUseCase getContractorProjectsUseCase;

    @Autowired
    private ChatService chatService;

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
            Map<Long, String> availableActions = getContractorProjectsUseCase.getAvailableActions(contractor);

            for (ProjectAssignment assignment : assignments) {
                long photoCount = photoService.getProjectPhotos(assignment.getProject().getId()).size();
                photoCounts.put(assignment.getProject().getId(), photoCount);
            }

            model.addAttribute("assignments", assignments);
            model.addAttribute("photoCounts", photoCounts);
            model.addAttribute("availableActions", availableActions);
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
                            contractor);

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

    @PostMapping("/project/{projectId}/open")
    public String openProject(@PathVariable Long projectId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user instanceof Contractor contractor) {
                openProjectUseCase.openProject(projectId, contractor);
                redirectAttributes.addFlashAttribute("success", "Project opened successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/contractor/dashboard";
    }

    @PostMapping("/project/{projectId}/complete")
    public String completeProject(@PathVariable Long projectId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user instanceof Contractor contractor) {
                completeProjectUseCase.completeProject(projectId, contractor);
                redirectAttributes.addFlashAttribute("success", "Project marked as complete");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/contractor/dashboard";
    }

    // ── Chat ─────────────────────────────────────────────────────────────────

    @GetMapping("/project/{projectId}/chat")
    public String chatPage(@PathVariable Long projectId, Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user instanceof Contractor contractor) {
            Optional<ProjectAssignment> assignment = projectAssignmentRepository
                    .findByProjectAndContractor(
                            projectRepository.findById(projectId).orElse(null),
                            contractor);
            if (assignment.isPresent()) {
                List<ProjectMessage> messages = chatService.getMessages(projectId);
                model.addAttribute("project", assignment.get().getProject());
                model.addAttribute("messages", messages);
                model.addAttribute("currentUsername", authentication.getName());
                return "contractor/project-chat";
            }
        }
        return "redirect:/contractor/dashboard";
    }

    @PostMapping("/project/{projectId}/chat/send")
    @ResponseBody
    public Map<String, Object> sendMessage(@PathVariable Long projectId,
            @RequestParam String content,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            User sender = userRepository.findByUsername(authentication.getName()).orElseThrow();
            ProjectMessage msg = chatService.sendMessage(projectId, sender, content.trim());
            response.put("id", msg.getId());
            response.put("content", msg.getContent());
            response.put("sender", msg.getSender().getUsername());
            response.put("sentAt", msg.getSentAt().toString());
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @GetMapping("/project/{projectId}/chat/poll")
    @ResponseBody
    public List<Map<String, Object>> pollMessages(@PathVariable Long projectId,
            @RequestParam String since,
            Authentication authentication) {
        LocalDateTime sinceTime = LocalDateTime.parse(since, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        List<ProjectMessage> messages = chatService.getMessagesSince(projectId, sinceTime);
        return messages.stream().map(msg -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", msg.getId());
            m.put("content", msg.getContent());
            m.put("sender", msg.getSender().getUsername());
            m.put("sentAt", msg.getSentAt().toString());
            return m;
        }).toList();
    }
}
