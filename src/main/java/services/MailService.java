package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Component
public class MailService {

    private static final String MAIL_SENDER_KEY = "mailSender";
    private static final String PASSWORD_SENDER_KEY = "passwordSender";
    private static final String MAIL_CONSUMER_KEY = "mailConsumer";

    private static final String PATH_TO_FILE = "target/test1.xls";

    @Autowired
    private ConfigManager configManager;

    private static Properties getProps() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return prop;
    }

    /**
     * Send mail with file of results of the parsing
     * @param eventManager
     * @throws MessagingException
     * @throws IOException
     */
    @EventListener(condition = "#eventManager.step.equals('SEND_MAIL')")
    public void sendMail(EventManager eventManager) throws MessagingException, IOException {
        String mailSender = configManager.getProperty(MAIL_SENDER_KEY);
        String passwordSender = configManager.getProperty(PASSWORD_SENDER_KEY);
        String mailConsumer = configManager.getProperty(MAIL_CONSUMER_KEY);
        Session session = Session.getInstance(getProps(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailSender, passwordSender);
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailSender));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(mailConsumer));
        message.setSubject("Test1");

        String msg = "File with tickets";

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.attachFile(new File(PATH_TO_FILE));

        multipart.addBodyPart(attachmentBodyPart);
        message.setContent(multipart);

        Transport.send(message);
    }
}
