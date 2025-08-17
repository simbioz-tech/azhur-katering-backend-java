package azhue.katering.azhurkateringbackendjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AzhurKateringBackendJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzhurKateringBackendJavaApplication.class, args);
    }
}
