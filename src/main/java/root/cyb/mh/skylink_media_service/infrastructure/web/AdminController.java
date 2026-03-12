package root.cyb.mh.skylink_media_service.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import root.cyb.mh.skylink_media_service.application.services.UserService;
import root.cyb.mh.skylink_media_service.application.services.ProjectService;
import root.cyb.mh.skylink_media_service.application.services.PhotoService;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;

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
                               RedirectAttributes redirectAttributes) {
        try {
            projectService.createProject(workOrderNumber, location, clientCode, description);
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
}
