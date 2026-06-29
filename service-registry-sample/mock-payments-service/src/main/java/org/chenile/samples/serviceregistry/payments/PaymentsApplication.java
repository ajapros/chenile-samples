package org.chenile.samples.serviceregistry.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        DataJpaRepositoriesAutoConfiguration.class
}, scanBasePackages = {
        "org.chenile.configuration",
        "org.chenile.service.registry",
        "org.chenile.samples.serviceregistry.payments"
})
public class PaymentsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentsApplication.class, args);
    }
}
