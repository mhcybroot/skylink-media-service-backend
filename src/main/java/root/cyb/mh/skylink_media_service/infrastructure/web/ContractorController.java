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
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectMessage;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectViewLog;
import root.cyb.mh.skylink_media_service.domain.valueobjects.ImageCategory;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAssignmentRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectViewLogRepository;
import root.cyb.mh.skylink_media_service.application.usecases.OpenProjectUseCase;
import root.cyb.mh.skylink_media_service.application.usecases.CompleteProjectUseCase;
import root.cyb.mh.skylink_media_service.application.usecases.GetContractorProjectsUseCase;

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

    @Autowired
    private ProjectViewLogRepository projectViewLogRepository;

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

            Map<Long, Long> photoCounts = new HashMap<>();
            Map<Long, Long> unreadCounts = new HashMap<>();
            Map<Long, String> availableActions = getContractorProjectsUseCase.getAvailableActions(contractor);

            for (ProjectAssignment assignment : assignments) {
                Project project = assignment.getProject();
                photoCounts.put(project.getId(), (long) photoService.getProjectPhotos(project.getId()).size());

                // Unread message count: messages since contractor last read this chat
                Optional<ProjectViewLog> viewLog = projectViewLogRepository.findByProjectAndContractor(project, contractor);
                long unread = viewLog
                        .map(vl -> chatService.countUnreadMessages(project, vl.getChatLastReadAt(), contractor.getUsername()))
                        .orElseGet(() -> chatService.countUnreadMessages(project, null, contractor.getUsername()));
                unreadCounts.put(project.getId(), unread);
            }

            model.addAttribute("assignments", assignments);
            model.addAttribute("photoCounts", photoCounts);
            model.addAttribute("unreadCounts", unreadCounts);
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
            @RequestParam(value = "category", required = false, defaultValue = "UNCATEGORIZED") ImageCategory category,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user instanceof Contractor contractor) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        photoService.uploadPhoto(file, projectId, contractor.getId(), category);
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
                Project project = assignment.get().getProject();

                // Mark chat as read
                ProjectViewLog viewLog = projectViewLogRepository
                        .findByProjectAndContractor(project, contractor)
                        .orElseGet(() -> new ProjectViewLog(project, contractor));
                viewLog.recordChatRead();
                projectViewLogRepository.save(viewLog);

                model.addAttribute("project", project);
                model.addAttribute("messages", chatService.getMessages(projectId));
                model.addAttribute("currentUsername", authentication.getName());
                return "contractor/project-chat";
            }
        }
        return "redirect:/contractor/dashboard";
    }

    @PostMapping("/project/{projectId}/chat/send")
    public String sendMessage(@PathVariable Long projectId,
            @RequestParam String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            if (user instanceof Contractor contractor) {
                // Verify assignment
                projectAssignmentRepository.findByProjectAndContractor(
                        projectRepository.findById(projectId).orElseThrow(),
                        contractor).orElseThrow(() -> new RuntimeException("Not assigned to this project"));
                chatService.sendMessage(projectId, user, content.trim());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/contractor/project/" + projectId + "/chat";
    }
}
