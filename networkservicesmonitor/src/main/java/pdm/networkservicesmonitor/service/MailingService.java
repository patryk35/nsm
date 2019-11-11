package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import com.sun.mail.smtp.SMTPTransport;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.exceptions.AppException;

@Service
@Slf4j
public class MailingService {

    @Value("${app.smtp.server}")
    private String smtpServer;

    @Value("${app.smtp.port}")
    private String smtpPort;

    @Value("${app.smtp.username}")
    private String smtpUserName;

    @Value("${app.smtp.password}")
    private String smtpPassword;

    @Value("${app.smtp.fromAddress}")
    private String fromAddress;

    public void sendMail(String to, String subject, String content){
        Properties prop = System.getProperties();
        prop.put("mail.smtp.host", smtpServer);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(prop, null);
        Message message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(String.format("NSM Notification Service <%s>", fromAddress)));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to, false));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");
            message.setSentDate(new Date());

            SMTPTransport smtpTransport = (SMTPTransport) session.getTransport("smtp");
            smtpTransport.connect(smtpServer, smtpUserName, smtpPassword);
            smtpTransport.sendMessage(message, message.getAllRecipients());
            if(smtpTransport.getLastReturnCode() != 200 && smtpTransport.getLastReturnCode() != 250){
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
