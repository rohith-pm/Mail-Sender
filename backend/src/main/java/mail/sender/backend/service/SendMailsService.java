package mail.sender.backend.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClientRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import mail.sender.backend.utils.DefaultHttpTransportFactory;
import mail.sender.backend.utils.SendMailServiceUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

@Scope(value = "prototype")
@Service
public class SendMailsService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SendMailsService.class);
    private List<MultipartFile> attachments;
    private MultipartFile file;
    private String from;
    private String to;
    private String subject;
    private String bodyText;
    private String numberOfEmails;

    public List<MultipartFile> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MultipartFile> attachments) {
        this.attachments = attachments;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public String getNumberOfEmails() {
        return numberOfEmails;
    }

    public void setNumberOfEmails(String numberOfEmails) {
        this.numberOfEmails = numberOfEmails;
    }

    /**
     * Send emails
     *
     * @throws MessagingException
     * @throws IOException
     */
    public void sendEmails() throws MessagingException, IOException {
        MimeMessage mimeMessage = null;
        if (attachments == null || attachments.isEmpty()) {
            mimeMessage = createEmail(to, from, subject, bodyText);

        } else {
            mimeMessage = createEmailWithAttachment(to, from, subject, bodyText,
                    SendMailServiceUtils.convert(attachments));
        }
        final MimeMessage finalMessage = mimeMessage;
        IntStream stream = IntStream.range(1, Integer.parseInt(numberOfEmails) + 1);
        Gmail service = getGmailService();
        //Mutli-Threading
        stream.parallel().forEach(i -> {
            try {
                sendMessage(service, from, finalMessage);
                LOG.info(subject + "  " + i);
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create a MimeMessage using the parameters provided
     *
     * @param to       Email address of the receiver
     * @param from     Email address of the sender, the mailbox account
     * @param subject  Subject of the email
     * @param bodyText Body text of the email
     * @param files    Path to the file to be attached
     * @return MimeMessage to be used to send email
     * @throws MessagingException
     */
    public MimeMessage createEmailWithAttachment(String to, String from, String subject, String bodyText,
                                                 List<File> files)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(bodyText, "text/plain");
        Multipart multipart = new MimeMultipart("mixed");
        files.forEach(file -> {
            try {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(source.getName());
                multipart.addBodyPart(messageBodyPart);
            } catch (MessagingException e) {
                LOG.error(e.getMessage(), e);
            }
        });
        email.setContent(multipart);
        return email;
    }

    /**
     * @return Gmail service object for a user
     */
    private Gmail getGmailService() throws IOException {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport = new NetHttpTransport();
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(getCredential(httpTransport));
        return new Gmail.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName("Mail Sender").build();

    }

    private GoogleCredentials getCredential(HttpTransport HTTP_TRANSPORT) throws IOException {
        List<String> SCOPES = Arrays.asList(GmailScopes.MAIL_GOOGLE_COM);
        GoogleCredentials credential = GoogleCredentials
                .fromStream(new BufferedInputStream(file.getInputStream()), new DefaultHttpTransportFactory(HTTP_TRANSPORT))
                .createScoped(SCOPES).createDelegated(getFrom());
        credential.refreshIfExpired();
        return credential;
    }

    /**
     * @param to       To email Id
     * @param from     From email Id
     * @param subject  Subject of the email
     * @param bodyText Body of the email
     * @return MimeMessage to be used to send email
     * @throws MessagingException
     */
    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    /**
     * Create a Message using an email
     *
     * @param email Email to be set to raw of message
     * @return Message containing base64url encoded email.
     * @throws IOException
     * @throws MessagingException
     */
    private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        email.writeTo(baos);
        String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * @param userId User Id of Gmail account
     * @param email  The email with body
     * @throws MessagingException
     * @throws IOException
     */
    private void sendMessage(Gmail service, String userId, MimeMessage email) throws MessagingException, IOException {
        Message message = createMessageWithEmail(email);
        SendMailServiceUtils.execute(service.users().messages().send(userId, message),
                this.getClass().getSimpleName(), "sendMessage");
    }

}
