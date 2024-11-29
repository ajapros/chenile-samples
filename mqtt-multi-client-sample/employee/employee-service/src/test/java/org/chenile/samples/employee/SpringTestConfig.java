package org.chenile.samples.employee;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;


@Configuration
@PropertySource("classpath:org/chenile/samples/employee/TestService.properties")
@SpringBootApplication(scanBasePackages = {  "org.chenile.configuration",
        "org.chenile.samples.**.configuration"
        })
@ActiveProfiles("unittest")
public class SpringTestConfig extends SpringBootServletInitializer{
}

