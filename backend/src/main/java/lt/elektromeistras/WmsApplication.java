package lt.elektromeistras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application class for the Warehouse Management System (WMS).
 *
 * This application provides:
 * - Inventory management
 * - Order processing
 * - Returns management
 * - Credit transaction tracking
 * - Role-based access control (RBAC)
 * - JWT-based authentication
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class WmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
    }
}
