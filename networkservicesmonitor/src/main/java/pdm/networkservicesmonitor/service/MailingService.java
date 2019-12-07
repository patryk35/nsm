package pdm.networkservicesmonitor.service;

import com.sun.mail.smtp.SMTPTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.exceptions.AppException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

@Service
@Slf4j
public class MailingService {

    @Autowired
    private SettingsService settingsService;

    public void sendMail(String to, String subject, String content) {
        Properties prop = System.getProperties();
        prop.put("mail.smtp.host", settingsService.getAppSettings().getSmtpServer());
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", settingsService.getAppSettings().getSmtpPort());

        Session session = Session.getInstance(prop, null);
        Message message = new MimeMessage(session);

        content = content.replace("%footer%", settingsService.getAppSettings().getSmtpMailsFooterName());
        try {
            message.setFrom(new InternetAddress(String.format("NSM Notification Service <%s>", settingsService.getAppSettings().getSmtpFromAddress())));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to, false));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");
            message.setSentDate(new Date());

            SMTPTransport smtpTransport = (SMTPTransport) session.getTransport("smtp");
            smtpTransport.connect(
                    settingsService.getAppSettings().getSmtpServer(),
                    settingsService.getAppSettings().getSmtpUsername(),
                    settingsService.getAppSettings().getSmtpPassword());
            smtpTransport.sendMessage(message, message.getAllRecipients());
            if (smtpTransport.getLastReturnCode() != 200 && smtpTransport.getLastReturnCode() != 250) {
                log.error("Message sending problems! SMTP server response: " + smtpTransport.getLastServerResponse());
                throw new AppException("Problems during sending email!");
            } else {
                log.trace("SMTP server response: " + smtpTransport.getLastServerResponse());
            }
            smtpTransport.close();
        } catch (MessagingException e) {
            log.error("Message sending problems: " + e.getMessage());

            throw new AppException("Problems during sending email!");
        }
    }
}
