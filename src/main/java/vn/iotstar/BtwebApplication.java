package vn.iotstar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import vn.iotstar.config.StorageProperties;
import vn.iotstar.service.StorageService;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "vn.iotstar.repository")
@EntityScan(basePackages = "vn.iotstar.entity")
@EnableConfigurationProperties(StorageProperties.class)
public class BtwebApplication implements CommandLineRunner {

    @Autowired
    private StorageService storageService;

    public static void main(String[] args) {
        SpringApplication.run(BtwebApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        storageService.init();
    }
}