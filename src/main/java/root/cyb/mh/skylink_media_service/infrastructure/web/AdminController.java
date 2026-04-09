package root.cyb.mh.skylink_media_service.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import root.cyb.mh.skylink_media_service.application.dto.ProjectSearchCriteria;
import root.cyb.mh.skylink_media_service.application.services.UserService;
import root.cyb.mh.skylink_media_service.application.services.ProjectService;
import root.cyb.mh.skylink_media_service.application.services.PhotoService;
import root.cyb.mh.skylink_media_service.application.services.ChatService;
import root.cyb.mh.skylink_media_service.application.services.ProjectExportService;
import root.cyb.mh.skylink_media_service.application.services.AuditLogService;
import root.cyb.mh.skylink_media_service.application.services.EmailService;
import root.cyb.mh.skylink_media_service.application.usecases.ChangeProjectStatusUseCase;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.Admin;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectMessage;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus;
import root.cyb.mh.skylink_media_service.domain.valueobjects.PaymentStatus;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectMessageRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.AdminRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.AdminChatReadLogRepository;
import root.cyb.mh.skylink_media_service.domain.entities.AdminChatReadLog;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private ChangeProjectStatusUseCase changeProjectStatusUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ProjectExportService projectExportService;
    
    @Autowired
    private root.cyb.mh.skylink_media_service.infrastructure.config.DevModeConfig devModeConfig;
    
    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProjectMessageRepository projectMessageRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminChatReadLogRepository adminChatReadLogRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
            @RequestParam(required = false) String projectSearch,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(required = false) BigDecimal priceFrom,
            @RequestParam(required = false) BigDecimal priceTo,
            @RequestParam(required = false) Long contractorId,
            @RequestParam(required = false) String contractorSearch,
            Authentication authentication) {

        logger.debug("Dashboard filter params - projectSearch: {}, status: {}, paymentStatus: {}, dueDateFrom: {}, dueDateTo: {}, priceFrom: {}, priceTo: {}, contractorId: {}",
                projectSearch, status, paymentStatus, dueDateFrom, dueDateTo, priceFrom, priceTo, contractorId);

        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.setTextSearch(projectSearch);
        
        if (status != null && !status.isEmpty()) {
            try {
                ProjectStatus parsedStatus = ProjectStatus.valueOf(status);
                criteria.setStatus(parsedStatus);
                logger.debug("Parsed project status: {}", parsedStatus);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid project status value: {}", status);
            }
        }
        
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            try {
                PaymentStatus parsedPaymentStatus = PaymentStatus.valueOf(paymentStatus);
                criteria.setPaymentStatus(parsedPaymentStatus);
                logger.debug("Parsed payment status: {}", parsedPaymentStatus);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid payment status value: {} - This should not happen if UI is correct", paymentStatus);
            }
        }
        
        criteria.setDueDateFrom(dueDateFrom);
        criteria.setDueDateTo(dueDateTo);
        criteria.setPriceFrom(priceFrom);
        criteria.setPriceTo(priceTo);
        criteria.setAssignedContractorId(contractorId);

        logger.debug("Final search criteria - isEmpty: {}, criteria: {}", criteria.isEmpty(), criteria);

        List<Project> projects = criteria.isEmpty() 
            ? projectService.getAllProjects()
            : projectService.advancedSearch(criteria);

        logger.info("Search returned {} projects", projects.size());

        List<Contractor> contractors = (contractorSearch != null && !contractorSearch.trim().isEmpty())
                ? userService.searchContractors(contractorSearch)
                : userService.getAllContractors();

        // Build contractor availability map (for UI indicators)
        Map<Long, Long> contractorActiveCounts = new HashMap<>();
        Map<Long, Boolean> contractorAvailability = new HashMap<>();
        for (Contractor contractor : contractors) {
            long count = projectService.getActiveProjectCount(contractor);
            contractorActiveCounts.put(contractor.getId(), count);
            contractorAvailability.put(contractor.getId(), projectService.canContractorTakeMoreProjects(contractor));
        }

        // Build project availability map (for UI indicators)
        Map<Long, Boolean> projectAvailability = new HashMap<>();
        for (Project project : projects) {
            projectAvailability.put(project.getId(), projectService.isProjectAvailableForAssignment(project));
        }

        model.addAttribute("projects", projects);
        model.addAttribute("contractors", contractors);
        model.addAttribute("searchCriteria", criteria);
        model.addAttribute("allContractors", contractors);
        model.addAttribute("contractorSearch", contractorSearch);
        model.addAttribute("contractorActiveCounts", contractorActiveCounts);
        model.addAttribute("contractorAvailability", contractorAvailability);
        model.addAttribute("projectAvailability", projectAvailability);
        model.addAttribute("isDevMode", devModeConfig.isDev());

        // Unread message counts per project (messages from contractors since admin last read)
        Map<Long, Long> projectUnreadCounts = new HashMap<>();
        String adminUsername = authentication.getName();
        for (Project project : projects) {
            LocalDateTime lastRead = adminChatReadLogRepository
                    .findByProjectAndAdminUsername(project, adminUsername)
                    .map(AdminChatReadLog::getLastReadAt)
                    .orElse(null);
            long count = lastRead == null ? 0L
                    : projectMessageRepository.countUnreadMessages(project, lastRead, adminUsername);
            projectUnreadCounts.put(project.getId(), count);
        }
        model.addAttribute("projectUnreadCounts", projectUnreadCounts);

        return "admin/dashboard";
    }

    @GetMapping("/projects/export")
    public void exportProjects(
            @RequestParam(required = false) String projectSearch,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(required = false) BigDecimal priceFrom,
            @RequestParam(required = false) BigDecimal priceTo,
            @RequestParam(required = false) Long contractorId,
            HttpServletResponse response) throws IOException {
        
        logger.info("Exporting projects with filters - projectSearch: {}, status: {}, paymentStatus: {}", 
                    projectSearch, status, paymentStatus);
        
        // Build search criteria (same logic as dashboard)
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.setTextSearch(projectSearch);
        
        if (status != null && !status.isEmpty()) {
            try {
                criteria.setStatus(ProjectStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid project status value: {}", status);
            }
        }
        
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            try {
                criteria.setPaymentStatus(PaymentStatus.valueOf(paymentStatus));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid payment status value: {}", paymentStatus);
            }
        }
        
        criteria.setDueDateFrom(dueDateFrom);
        criteria.setDueDateTo(dueDateTo);
        criteria.setPriceFrom(priceFrom);
        criteria.setPriceTo(priceTo);
        criteria.setAssignedContractorId(contractorId);
        
        // Get filtered projects
        List<Project> projects = criteria.isEmpty() 
            ? projectService.getAllProjects()
            : projectService.advancedSearch(criteria);
        
        logger.info("Exporting {} projects", projects.size());
        
        // Generate CSV
        String csv = projectExportService.generateCsv(projects);
        
        // Set response headers
        String filename = "projects_export_" + 
                         java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + 
                         ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        // Write CSV to response
        response.getWriter().write(csv);
        response.getWriter().flush();
    }

    @GetMapping("/register-admin")
    public String registerAdminForm(Model model, Authentication authentication) {
        try {
            model.addAttribute("currentAdmin", userService.getAdminByUsername(authentication.getName()));
        } catch (Exception ignored) {}
        return "admin/register-admin";
    }

    @PostMapping("/register-admin")
    public String registerAdmin(@RequestParam String username, @RequestParam String password,
            @RequestParam(required = false) String email,
            RedirectAttributes redirectAttributes) {
        try {
            userService.createAdmin(username, password, email);
            redirectAttributes.addFlashAttribute("success", "Admin registered successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/register-admin";
    }

    @PostMapping("/update-email")
    public String updateEmail(@RequestParam String email,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateAdminEmail(authentication.getName(), email);
            redirectAttributes.addFlashAttribute("emailSuccess", "Notification email updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("emailError", e.getMessage());
        }
        return "redirect:/admin/register-admin";
    }

    @GetMapping("/create-project")
    public String createProjectForm(Model model) {
        model.addAttribute("allAdmins", adminRepository.findAll());
        return "admin/create-project";
    }

    @PostMapping("/create-project")
    public String createProject(@RequestParam String workOrderNumber,
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
            // Get the authenticated admin user
            User admin = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
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
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/create-contractor")
    public String createContractorForm() {
        return "admin/create-contractor";
    }

    @PostMapping("/create-contractor")
    public String createContractor(@RequestParam String username, @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam(required = false) String email,
            RedirectAttributes redirectAttributes) {
        try {
            userService.createContractor(username, password, fullName, email);
            redirectAttributes.addFlashAttribute("success", "Contractor created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/assign-contractor")
    public String assignContractor(@RequestParam Long projectId, @RequestParam Long contractorId,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User admin = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            projectService.assignContractorToProject(projectId, contractorId, admin);
            redirectAttributes.addFlashAttribute("success", "Contractor assigned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/unassign-contractor")
    public String unassignContractor(@RequestParam Long projectId, @RequestParam Long contractorId,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User admin = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            projectService.unassignContractorFromProject(projectId, contractorId, admin);
            redirectAttributes.addFlashAttribute("success", "Contractor unassigned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    /**
     * Delete a project - only available in development mode.
     * This endpoint is disabled in production environments.
     */
    @PostMapping("/delete-project/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Security check: only allow in development mode
        if (!devModeConfig.isDev()) {
            redirectAttributes.addFlashAttribute("error", "Project deletion is only available in development mode");
            return "redirect:/admin/dashboard";
        }
        
        try {
            projectService.deleteProject(id);
            redirectAttributes.addFlashAttribute("success", "Project deleted successfully");
        } catch (Exception e) {
            logger.error("Failed to delete project {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete project: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    /**
     * Get audit history for a project - returns JSON for modal display
     */
    @GetMapping("/project/{id}/history")
    public String getProjectHistory(@PathVariable Long id, Model model) {
        Project project = projectService.getProjectById(id);
        model.addAttribute("project", project);
        model.addAttribute("auditLogs", auditLogService.getAuditLogsForProject(id));
        return "admin/project-history :: historyContent";
    }

    @GetMapping("/project/{id}/photos")
    public String viewProjectPhotos(@PathVariable Long id, Model model, Authentication authentication) {
        Project project = projectService.getProjectById(id);
        model.addAttribute("project", project);
        var photos = photoService.getProjectPhotos(id);
        model.addAttribute("photos", photos);
        
        // Audit log for photos viewed
        try {
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            auditLogService.logPhotosViewed(project, user, photos.size());
        } catch (Exception e) {
            logger.warn("Failed to log photos view: {}", e.getMessage());
        }
        
        return "admin/project-photos";
    }

    @GetMapping("/edit-project/{id}")
    public String editProjectForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProjectById(id));
        model.addAttribute("allAdmins", adminRepository.findAll());
        return "admin/edit-project";
    }

    @PostMapping("/edit-project/{id}")
    public String editProject(@PathVariable Long id,
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
            // Get the authenticated admin user
            User admin = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
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

            projectService.updateProject(id, workOrderNumber, location, clientCode, description,
                    ppwNumber, workType, workDetails, clientCompany,
                    customer, loanNumber, loanType, address,
                    parsedReceivedDate, parsedDueDate, assignedTo, woAdmin, parsedInvoicePrice, admin);
            redirectAttributes.addFlashAttribute("success", "Project updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/change-status/{projectId}")
    public String changeProjectStatus(@PathVariable Long projectId,
            @RequestParam ProjectStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User admin = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            changeProjectStatusUseCase.changeProjectStatus(projectId, status, admin);
            redirectAttributes.addFlashAttribute("success", "Project status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/change-payment-status/{projectId}")
    public String changePaymentStatus(@PathVariable Long projectId,
            @RequestParam PaymentStatus paymentStatus,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User admin = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            changeProjectStatusUseCase.changePaymentStatus(projectId, paymentStatus, admin);
            redirectAttributes.addFlashAttribute("success", "Payment status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/project/{id}/photos/download")
    public void downloadSelectedPhotos(@PathVariable Long id,
            @RequestParam(required = false) List<Long> photoIds,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        if (photoIds == null || photoIds.isEmpty()) {
            response.sendRedirect("/admin/project/" + id + "/photos?error=No+photos+selected");
            return;
        }

        Project project = projectService.getProjectById(id);
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        }

        List<root.cyb.mh.skylink_media_service.domain.entities.Photo> selectedPhotos = photoService
                .getPhotosByIdsAndProjectId(photoIds, id);

        if (selectedPhotos.isEmpty()) {
            response.sendRedirect("/admin/project/" + id + "/photos?error=No+valid+photos+found");
            return;
        }
        
        // Audit log for photos downloaded
        try {
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            auditLogService.logPhotosDownloaded(project, user, selectedPhotos.size());
        } catch (Exception e) {
            logger.warn("Failed to log photos download: {}", e.getMessage());
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"project_" + project.getWorkOrderNumber() + "_photos.zip\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (root.cyb.mh.skylink_media_service.domain.entities.Photo photo : selectedPhotos) {
                try {
                    // Try to read the original file first for maximum quality and metadata preservation
                    Path filePath = null;
                    if (photo.getOriginalPath() != null) {
                        filePath = Paths.get(photo.getOriginalPath());
                    }
                    // Fallbacks for older uploads where original was deleted
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
                } catch (IOException e) {
                    // Log error and continue with next photo
                    e.printStackTrace();
                }
            }
        }
    }

    // ── Chat ─────────────────────────────────────────────────────────────────

    @GetMapping("/project/{projectId}/chat")
    public String chatPage(@PathVariable Long projectId, Model model, Authentication authentication) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null)
            return "redirect:/admin/dashboard";

        // Mark chat as read for this admin
        String adminUsername = authentication.getName();
        AdminChatReadLog readLog = adminChatReadLogRepository
                .findByProjectAndAdminUsername(project, adminUsername)
                .orElseGet(() -> new AdminChatReadLog(project, adminUsername));
        readLog.markRead();
        adminChatReadLogRepository.save(readLog);

        model.addAttribute("project", project);
        model.addAttribute("messages", chatService.getMessages(projectId));
        model.addAttribute("currentUsername", adminUsername);
        return "admin/project-chat";
    }

    @PostMapping("/project/{projectId}/chat/send")
    public String sendMessage(@PathVariable Long projectId,
            @RequestParam String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User sender = userRepository.findByUsername(authentication.getName()).orElseThrow();
            chatService.sendMessage(projectId, sender, content.trim());

            // Email notification to all assigned contractors who have an email
            Project project = projectRepository.findById(projectId).orElseThrow();
            
            // Audit log for chat message sent
            auditLogService.logChatMessageSent(project, sender, content.trim());
            
            String chatUrl = "http://76.13.221.43:8085/contractor/project/" + projectId + "/chat";
            String senderName = sender instanceof Admin admin 
                    ? (admin.getUsername() != null ? admin.getUsername() : "Admin")
                    : sender.getUsername();
            projectService.getContractorsForProject(projectId).forEach(contractor -> {
                if (contractor.getEmail() != null && !contractor.getEmail().isBlank()) {
                    emailService.sendChatNotification(
                            contractor.getEmail(),
                            contractor.getFullName() != null ? contractor.getFullName() : contractor.getUsername(),
                            project.getWorkOrderNumber(),
                            content.trim(),
                            chatUrl,
                            senderName);
                }
            });
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/project/" + projectId + "/chat";
    }

    // ── Contractor Management ─────────────────────────────────────────────────

    @GetMapping("/contractors/{contractorId}/edit")
    public String editContractorForm(@PathVariable Long contractorId, Model model) {
        Contractor contractor = userService.getContractorById(contractorId);
        model.addAttribute("contractor", contractor);
        return "admin/edit-contractor";
    }

    @PostMapping("/contractors/{contractorId}/update")
    public String updateContractor(@PathVariable Long contractorId,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User admin = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            // Update profile info
            userService.updateContractor(contractorId, fullName, email);
            
            // Handle avatar upload if provided
            if (avatar != null && !avatar.isEmpty()) {
                String avatarPath = saveContractorAvatar(avatar, contractorId);
                userService.adminUpdateContractorAvatar(contractorId, avatarPath);
            }
            
            // Log the action
            auditLogService.logContractorUpdated(contractorId, admin);
            
            redirectAttributes.addFlashAttribute("success", "Contractor updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/contractors/{contractorId}/change-password")
    public String changeContractorPassword(@PathVariable Long contractorId,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("Passwords do not match");
            }
            
            User admin = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            userService.adminChangeContractorPassword(contractorId, newPassword);
            
            // Log the password change
            auditLogService.logContractorPasswordChanged(contractorId, admin);
            
            redirectAttributes.addFlashAttribute("passwordSuccess", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/admin/contractors/" + contractorId + "/edit";
    }

    private String saveContractorAvatar(MultipartFile avatar, Long contractorId) throws IOException {
        if (avatar == null || avatar.isEmpty()) return null;

        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed for avatar");
        }
        if (avatar.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("Avatar must be under 2MB");
        }

        Path avatarDir = Paths.get(uploadDir, "avatars");
        Files.createDirectories(avatarDir);

        String ext = avatar.getOriginalFilename() != null && avatar.getOriginalFilename().contains(".")
                ? avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf("."))
                : ".jpg";
        String filename = "avatar_contractor_" + contractorId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        Path dest = avatarDir.resolve(filename);
        Files.copy(avatar.getInputStream(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        logger.info("Contractor avatar saved for ID {}: {}", contractorId, filename);
        return "avatars/" + filename;
    }
}
