package application;

@org.springframework.cache.annotation.EnableCaching
@org.springframework.boot.autoconfigure.SpringBootApplication
class SpringApplication {
    static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(SpringApplication.class, args);
    }
}