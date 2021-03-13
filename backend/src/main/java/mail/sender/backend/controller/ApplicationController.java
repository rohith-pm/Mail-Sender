package mail.sender.backend.controller;

import mail.sender.backend.service.SendMailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@ComponentScan(basePackages = "mail.sender.backend.service")
public class ApplicationController {

    @Autowired
    private ApplicationContext context;

    @PostMapping(value = "/send")
    public ResponseEntity<Object> sendEmails(@RequestParam("file") MultipartFile file,
                                             @RequestParam("from") String from,
                                             @RequestParam("to") String to, @RequestParam("subject") String subject,
                                             @RequestParam("mailBody") String mailBody,
                                             @RequestParam("count") String count,
                                             @Nullable @RequestParam("attachments") List<MultipartFile> attachments) {

        SendMailsService sendMailsService = (SendMailsService) context.getBean("sendMailService");
        sendMailsService.setFrom(from);
        sendMailsService.setTo(to);
        sendMailsService.setSubject(subject);
        sendMailsService.setBodyText(mailBody);
        //checks count value
        try {
            Integer.parseInt(count);
            if (Integer.parseInt(count) < 1)
                throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Number of emails should be a positive natural number", HttpStatus.BAD_REQUEST);
        }
        sendMailsService.setNumberOfEmails(count);
        sendMailsService.setFile(file);
        sendMailsService.setAttachments(attachments);
        //send emails
        try {
            sendMailsService.sendEmails();
        } catch (MessagingException | IOException e) {
            return new ResponseEntity<>("There is something wrong with the emails addresses", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(count + " Mails have been sent");
    }
}
