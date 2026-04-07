package root.cyb.mh.skylink_media_service.application.services;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendChatNotification(String toEmail, String contractorName,
                                     String workOrderNumber, String adminMessage,
                                     String chatUrl) {
        if (toEmail == null || toEmail.isBlank()) return;

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject("New message on project WO# " + workOrderNumber);

            String html = """
                    <div style="font-family:Inter,Arial,sans-serif;max-width:560px;margin:0 auto;background:#ffffff;border:1px solid #E5E7EB;border-radius:12px;overflow:hidden;">
                      <div style="background:#4f46e5;padding:24px 32px;">
                        <h1 style="margin:0;color:#ffffff;font-size:18px;font-weight:700;letter-spacing:-0.3px;">Skylink Hub</h1>
                        <p style="margin:4px 0 0;color:#c7d2fe;font-size:12px;">Project Communication</p>
                      </div>
                      <div style="padding:32px;">
                        <p style="margin:0 0 8px;font-size:13px;color:#6B7280;font-weight:600;text-transform:uppercase;letter-spacing:0.05em;">New Message</p>
                        <h2 style="margin:0 0 24px;font-size:20px;font-weight:700;color:#111827;">WO# %s</h2>
                        <p style="margin:0 0 8px;font-size:12px;color:#9CA3AF;font-weight:600;text-transform:uppercase;letter-spacing:0.05em;">Message from Admin</p>
                        <div style="background:#F9FAFB;border:1px solid #E5E7EB;border-radius:8px;padding:16px 20px;margin-bottom:28px;">
                          <p style="margin:0;font-size:14px;color:#1F2937;line-height:1.6;">%s</p>
                        </div>
                        <a href="%s" style="display:inline-block;background:#4f46e5;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:8px;font-size:14px;font-weight:700;">View Conversation</a>
                      </div>
                      <div style="padding:16px 32px;background:#F9FAFB;border-top:1px solid #E5E7EB;">
                        <p style="margin:0;font-size:11px;color:#9CA3AF;">You are receiving this because you are assigned to project WO# %s on Skylink Hub.</p>
                      </div>
                    </div>
                    """.formatted(workOrderNumber, escapeHtml(adminMessage), chatUrl, workOrderNumber);

            helper.setText(buildPlainText(workOrderNumber, adminMessage, chatUrl), html);

            mailSender.send(mime);
            logger.info("Chat notification sent to {} for project WO#{}", toEmail, workOrderNumber);

        } catch (Exception e) {
            logger.error("Failed to send chat notification to {} for project WO#{}: {}", toEmail, workOrderNumber, e.getMessage());
        }
    }

    private String buildPlainText(String workOrderNumber, String message, String chatUrl) {
        return "New message on project WO# " + workOrderNumber + "\n\n"
                + "Message from Admin:\n" + message + "\n\n"
                + "View conversation: " + chatUrl;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("\n", "<br>");
    }
}
