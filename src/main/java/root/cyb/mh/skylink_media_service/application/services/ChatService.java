package root.cyb.mh.skylink_media_service.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectMessage;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectMessageRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ProjectMessageRepository messageRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public ProjectMessage sendMessage(Long projectId, User sender, String content) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        if (project.isBlocked()) {
            throw new RuntimeException("This project is temporarily blocked. Chat is disabled until it is unblocked.");
        }
        ProjectMessage message = new ProjectMessage(project, sender, content);
        ProjectMessage savedMessage = messageRepository.save(message);
        auditLogService.logChatMessageSent(project, sender, savedMessage.getContent());
        return savedMessage;
    }

    @Transactional(readOnly = true)
    public List<ProjectMessage> getMessages(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        return messageRepository.findByProjectOrderBySentAtAsc(project);
    }

    @Transactional(readOnly = true)
    public long countUnreadMessages(Project project, LocalDateTime since, String viewerUsername) {
        if (since == null) return 0;
        return messageRepository.countUnreadMessages(project, since, viewerUsername);
    }
}
