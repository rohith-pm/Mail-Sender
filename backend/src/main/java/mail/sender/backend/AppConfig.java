package mail.sender.backend;

import mail.sender.backend.service.SendMailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConfig {

    @Bean(name = "sendMailService")
    @Scope(value = "prototype")
    public SendMailsService gteSendMailsService() {
        return new SendMailsService();
    }
}
