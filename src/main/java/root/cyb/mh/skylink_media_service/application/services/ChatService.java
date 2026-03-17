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

    @Transactional
    public ProjectMessage sendMessage(Long projectId, User sender, String content) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        ProjectMessage message = new ProjectMessage(project, sender, content);
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ProjectMessage> getMessages(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        return messageRepository.findByProjectOrderBySentAtAsc(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectMessage> getMessagesSince(Long projectId, LocalDateTime since) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        return messageRepository.findByProjectAndSentAtAfterOrderBySentAtAsc(project, since);
    }
}
