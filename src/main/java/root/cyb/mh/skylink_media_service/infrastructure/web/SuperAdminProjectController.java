package root.cyb.mh.skylink_media_service.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import root.cyb.mh.skylink_media_service.application.dto.ProjectSearchCriteria;
import root.cyb.mh.skylink_media_service.application.services.ProjectService;
import root.cyb.mh.skylink_media_service.application.services.PhotoService;
import root.cyb.mh.skylink_media_service.application.services.ChatService;
import root.cyb.mh.skylink_media_service.application.services.AuditLogService;
import root.cyb.mh.skylink_media_service.application.services.UserService;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.Admin;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectMessage;
import root.cyb.mh.skylink_media_service.domain.entities.SuperAdmin;
import root.cyb.mh.skylink_media_service.domain.entities.Photo;
import root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus;
import root.cyb.mh.skylink_media_service.domain.valueobjects.PaymentStatus;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectMessageRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.AdminChatReadLogRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.AdminRepository;
import root.cyb.mh.skylink_media_service.domain.entities.AdminChatReadLog;

import java.math.BigDecimal;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMessageRepository projectMessageRepository;

    @Autowired
    private AdminChatReadLogRepository adminChatReadLogRepository;

    @Autowired
    private AdminRepository adminRepository;

    // ==================== Project List ====================

    @GetMapping("/projects")
    public String listProjects(
            @RequestParam(required = false) String projectSearch,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(required = false) BigDecimal priceFrom,
            @RequestParam(required = false) BigDecimal priceTo,
            @RequestParam(required = false) Long contractorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            Authentication authentication) {

        // Build search criteria
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.setTextSearch(projectSearch);

        if (status != null && !status.isEmpty()) {
            try {
                criteria.setStatus(ProjectStatus.valueOf(status));
            } catch (IllegalArgumentException ignored) {}
        }

        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            try {
                criteria.setPaymentStatus(PaymentStatus.valueOf(paymentStatus));
            } catch (IllegalArgumentException ignored) {}
        }

        criteria.setDueDateFrom(dueDateFrom);
        criteria.setDueDateTo(dueDateTo);
        criteria.setPriceFrom(priceFrom);
        criteria.setPriceTo(priceTo);
        criteria.setAssignedContractorId(contractorId);

        List<Project> projects = criteria.isEmpty()
            ? projectService.getAllProjects()
            : projectService.advancedSearch(criteria);

        // Get all contractors for filter dropdown
        List<Contractor> contractors = userService.getAllContractors();

        // Build contractor availability map
        Map<Long, Long> contractorActiveCounts = new HashMap<>();
        Map<Long, Boolean> contractorAvailability = new HashMap<>();
        for (Contractor contractor : contractors) {
            long count = projectService.getActiveProjectCount(contractor);
            contractorActiveCounts.put(contractor.getId(), count);
            contractorAvailability.put(contractor.getId(), projectService.canContractorTakeMoreProjects(contractor));
        }

        // Build project availability map
        Map<Long, Boolean> projectAvailability = new HashMap<>();
        Map<Long, List<Contractor>> projectContractors = new HashMap<>();
        for (Project project : projects) {
            projectAvailability.put(project.getId(), projectService.isProjectAvailableForAssignment(project));
            projectContractors.put(project.getId(), projectService.getContractorsForProject(project.getId()));
        }

        // Unread message counts per project
        Map<Long, Long> projectUnreadCounts = new HashMap<>();
        String username = authentication.getName();
        for (Project project : projects) {
            LocalDateTime lastRead = adminChatReadLogRepository
                    .findByProjectAndAdminUsername(project, username)
                    .map(AdminChatReadLog::getLastReadAt)
                    .orElse(null);
            long count = lastRead == null ? 0L
                    : projectMessageRepository.countUnreadMessages(project, lastRead, username);
            projectUnreadCounts.put(project.getId(), count);
        }

        // Get current user
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("projects", projects);
        model.addAttribute("contractors", contractors);
        model.addAttribute("searchCriteria", criteria);
        model.addAttribute("contractorActiveCounts", contractorActiveCounts);
        model.addAttribute("contractorAvailability", contractorAvailability);
        model.addAttribute("projectAvailability", projectAvailability);
        model.addAttribute("projectContractors", projectContractors);
        model.addAttribute("projectUnreadCounts", projectUnreadCounts);
        model.addAttribute("activePage", "projects");

        return "super-admin/projects";
    }

    // ==================== Project Detail ====================

    @GetMapping("/projects/{id}")
    public String viewProject(@PathVariable Long id, Model model, Authentication authentication) {
        Project project = projectService.getProjectById(id);
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();

        // Audit log for project viewed
        auditLogService.logProjectViewed(project, currentUser);

        model.addAttribute("project", project);
        model.addAttribute("assignedContractors", projectService.getContractorsForProject(id));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("auditLogs", auditLogService.getAuditLogsForProject(id));
        model.addAttribute("activePage", "projects");

        return "super-admin/project-detail";
    }

    // ==================== Project Chat ====================

    @GetMapping("/projects/{projectId}/chat")
    public String projectChat(@PathVariable Long projectId, Model model, Authentication authentication) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return "redirect:/super-admin/projects";
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();

        // Mark chat as read for this user
        AdminChatReadLog readLog = adminChatReadLogRepository
                .findByProjectAndAdminUsername(project, username)
                .orElseGet(() -> new AdminChatReadLog(project, username));
        readLog.markRead();
        adminChatReadLogRepository.save(readLog);

        model.addAttribute("project", project);
        model.addAttribute("messages", chatService.getMessages(projectId).stream()
                .map(message -> toChatMessageView(message, username))
                .toList());
        model.addAttribute("currentUsername", username);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activePage", "projects");

        return "super-admin/project-chat";
    }

    @PostMapping("/projects/{projectId}/chat/send")
    public String sendProjectChatMessage(@PathVariable Long projectId,
            @RequestParam String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User sender = userRepository.findByUsername(authentication.getName()).orElseThrow();
            chatService.sendMessage(projectId, sender, content.trim());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/super-admin/projects/" + projectId + "/chat";
    }

    // ==================== Project Photos ====================

    @GetMapping("/projects/{projectId}/photos")
    public String projectPhotos(@PathVariable Long projectId, Model model, Authentication authentication) {
        Project project = projectService.getProjectById(projectId);
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        var photos = photoService.getProjectPhotos(projectId);

        // Audit log for photos viewed
        auditLogService.logPhotosViewed(project, currentUser, photos.size());

        model.addAttribute("project", project);
        model.addAttribute("photos", photos);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activePage", "projects");

        return "super-admin/project-photos";
    }

    @PostMapping("/projects/{projectId}/photos/download")
    public void downloadSelectedPhotos(@PathVariable Long projectId,
            @RequestParam(required = false) List<Long> photoIds,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        if (photoIds == null || photoIds.isEmpty()) {
            response.sendRedirect("/super-admin/projects/" + projectId + "/photos?error=No+photos+selected");
            return;
        }

        Project project = projectService.getProjectById(projectId);
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        }

        List<Photo> selectedPhotos = photoService.getPhotosByIdsAndProjectId(photoIds, projectId);

        if (selectedPhotos.isEmpty()) {
            response.sendRedirect("/super-admin/projects/" + projectId + "/photos?error=No+valid+photos+found");
            return;
        }

        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            auditLogService.logPhotosDownloaded(project, currentUser, selectedPhotos.size());
        } catch (Exception ignored) {
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"project_" + project.getWorkOrderNumber() + "_photos.zip\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Photo photo : selectedPhotos) {
                try {
                    Path filePath = null;
                    if (photo.getOriginalPath() != null) {
                        filePath = Paths.get(photo.getOriginalPath());
                    }
                    if (filePath == null || !Files.exists(filePath)) {
                        filePath = Paths.get("uploads", photo.getFileName());
                    }
                    if (!Files.exists(filePath) && photo.getWebpPath() != null) {
                        filePath = Paths.get(photo.getWebpPath());
                    }

                    if (Files.exists(filePath)) {
                        ZipEntry zipEntry = new ZipEntry(
                                photo.getOriginalName() != null ? photo.getOriginalName() : photo.getFileName());
                        zos.putNextEntry(zipEntry);
                        try (InputStream is = Files.newInputStream(filePath)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        }
                        zos.closeEntry();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    // ==================== Project History ====================

    @GetMapping("/projects/{projectId}/history")
    public String projectHistory(@PathVariable Long projectId, Model model, Authentication authentication) {
        Project project = projectService.getProjectById(projectId);
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();

        model.addAttribute("project", project);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("auditLogs", auditLogService.getAuditLogsForProject(projectId));
        model.addAttribute("activePage", "projects");

        return "super-admin/project-history";
    }

    // ==================== Create Project ====================

    @GetMapping("/create-project")
    public String createProjectForm(Model model, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("allAdmins", adminRepository.findAll());
        model.addAttribute("activePage", "create-project");
        return "super-admin/create-project";
    }

    @PostMapping("/create-project")
    public String createProject(
            @RequestParam String workOrderNumber,
            @RequestParam String location,
            @RequestParam String clientCode,
            @RequestParam String description,
            @RequestParam(required = false) String ppwNumber,
            @RequestParam(required = false) String workType,
            @RequestParam(required = false) String workDetails,
            @RequestParam(required = false) String clientCompany,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String loanNumber,
            @RequestParam(required = false) String loanType,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String receivedDate,
            @RequestParam(required = false) String dueDate,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String woAdmin,
            @RequestParam(required = false) String invoicePrice,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User admin = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            java.time.LocalDate parsedReceivedDate = null;
            java.time.LocalDate parsedDueDate = null;
            java.math.BigDecimal parsedInvoicePrice = null;

            if (receivedDate != null && !receivedDate.trim().isEmpty()) {
                parsedReceivedDate = java.time.LocalDate.parse(receivedDate);
            }
            if (dueDate != null && !dueDate.trim().isEmpty()) {
                parsedDueDate = java.time.LocalDate.parse(dueDate);
            }
            if (invoicePrice != null && !invoicePrice.trim().isEmpty()) {
                parsedInvoicePrice = new java.math.BigDecimal(invoicePrice);
                if (parsedInvoicePrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Invoice price cannot be negative");
                }
            }

            projectService.createProject(workOrderNumber, location, clientCode, description,
                    ppwNumber, workType, workDetails, clientCompany,
                    customer, loanNumber, loanType, address,
                    parsedReceivedDate, parsedDueDate, assignedTo, woAdmin, parsedInvoicePrice, admin);
            redirectAttributes.addFlashAttribute("success", "Project created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/projects";
    }

    private ChatMessageView toChatMessageView(ProjectMessage message, String currentUsername) {
        User sender = message.getSender();
        String role = sender.getRole();
        String senderName = sender.getUsername();
        boolean ownMessage = sender.getUsername().equals(currentUsername);

        if (sender instanceof Contractor contractor && contractor.getFullName() != null && !contractor.getFullName().isBlank()) {
            senderName = contractor.getFullName();
        }

        if (sender instanceof SuperAdmin) {
            return new ChatMessageView(
                    senderName,
                    role,
                    ownMessage ? "SUPER_ADMIN_SELF" : "SUPER_ADMIN_OTHER",
                    ownMessage,
                    "Super Admin",
                    "justify-center",
                    "items-center text-center",
                    "justify-center",
                    ownMessage ? "bubble-superadmin-self" : "bubble-superadmin-other",
                    message.getContent(),
                    message.getSentAt()
            );
        }

        if (sender instanceof Admin) {
            return new ChatMessageView(
                    senderName,
                    role,
                    ownMessage ? "ADMIN_SELF" : "ADMIN_OTHER",
                    ownMessage,
                    "Admin",
                    "justify-end",
                    "items-end text-right",
                    "justify-end",
                    ownMessage ? "bubble-admin-self" : "bubble-admin-other",
                    message.getContent(),
                    message.getSentAt()
            );
        }

        return new ChatMessageView(
                senderName,
                role,
                "CONTRACTOR",
                ownMessage,
                "Contractor",
                "justify-start",
                "items-start text-left",
                "justify-start",
                "bubble-contractor",
                message.getContent(),
                message.getSentAt()
        );
    }

    private record ChatMessageView(
            String senderName,
            String senderRole,
            String senderVariant,
            boolean ownMessage,
            String senderRoleLabel,
            String rowAlignmentClass,
            String contentAlignmentClass,
            String metaAlignmentClass,
            String bubbleClass,
            String content,
            LocalDateTime sentAt
    ) {}
}
