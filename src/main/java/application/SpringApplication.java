package application;

@org.springframework.cache.annotation.EnableCaching
@org.springframework.boot.autoconfigure.SpringBootApplication
public class SpringApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(SpringApplication.class, args);
    }
}