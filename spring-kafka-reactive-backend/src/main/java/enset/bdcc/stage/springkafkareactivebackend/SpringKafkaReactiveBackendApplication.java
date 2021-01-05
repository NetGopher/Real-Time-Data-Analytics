package enset.bdcc.stage.springkafkareactivebackend;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringKafkaReactiveBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringKafkaReactiveBackendApplication.class, args);
    }

}

