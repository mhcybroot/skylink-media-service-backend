package root.cyb.mh.skylink_media_service.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import root.cyb.mh.skylink_media_service.application.services.UserService;
import root.cyb.mh.skylink_media_service.application.services.ProjectService;
import root.cyb.mh.skylink_media_service.application.services.PhotoService;
import root.cyb.mh.skylink_media_service.application.services.ChatService;
import root.cyb.mh.skylink_media_service.application.usecases.ChangeProjectStatusUseCase;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectMessage;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus;
import root.cyb.mh.skylink_media_service.domain.valueobjects.PaymentStatus;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

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

    @GetMapping("/dashboard")
    public String dashboard(Model model,
            @RequestParam(required = false) String projectSearch,
            @RequestParam(required = false) String contractorSearch) {

        List<Project> projects = (projectSearch != null && !projectSearch.trim().isEmpty())
                ? projectService.searchProjects(projectSearch)
                : projectService.getAllProjects();

        List<Contractor> contractors = (contractorSearch != null && !contractorSearch.trim().isEmpty())
                ? userService.searchContractors(contractorSearch)
                : userService.getAllContractors();

        model.addAttribute("projects", projects);
        model.addAttribute("contractors", contractors);
        model.addAttribute("projectSearch", projectSearch);
        model.addAttribute("contractorSearch", contractorSearch);

        return "admin/dashboard";
    }

    @GetMapping("/register-admin")
    public String registerAdminForm() {
        return "admin/register-admin";
    }

    @PostMapping("/register-admin")
    public String registerAdmin(@RequestParam String username, @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            userService.createAdmin(username, password);
            redirectAttributes.addFlashAttribute("success", "Admin registered successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/create-project")
    public String createProjectForm() {
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
            RedirectAttributes redirectAttributes) {
        try {
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
                    parsedReceivedDate, parsedDueDate, assignedTo, woAdmin, parsedInvoicePrice);
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
            RedirectAttributes redirectAttributes) {
        try {
            userService.createContractor(username, password, fullName);
            redirectAttributes.addFlashAttribute("success", "Contractor created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/assign-contractor")
    public String assignContractor(@RequestParam Long projectId, @RequestParam Long contractorId,
            RedirectAttributes redirectAttributes) {
        try {
            projectService.assignContractorToProject(projectId, contractorId);
            redirectAttributes.addFlashAttribute("success", "Contractor assigned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/project/{id}/photos")
    public String viewProjectPhotos(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProjectById(id));
        model.addAttribute("photos", photoService.getProjectPhotos(id));
        return "admin/project-photos";
    }

    @GetMapping("/edit-project/{id}")
    public String editProjectForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProjectById(id));
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
            RedirectAttributes redirectAttributes) {
        try {
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
                    parsedReceivedDate, parsedDueDate, assignedTo, woAdmin, parsedInvoicePrice);
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
            HttpServletResponse response) throws IOException {
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

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"project_" + project.getWorkOrderNumber() + "_photos.zip\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (root.cyb.mh.skylink_media_service.domain.entities.Photo photo : selectedPhotos) {
                try {
                    // Try to read the original file first, fallback to webp
                    Path filePath = Paths.get("uploads", photo.getFileName());
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
        List<ProjectMessage> messages = chatService.getMessages(projectId);
        model.addAttribute("project", project);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUsername", authentication.getName());
        return "admin/project-chat";
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
            @RequestParam String since) {
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
