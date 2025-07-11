package org.chenile.samples;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;


@PropertySource("classpath:org/chenile/samples/TestService.properties")
@SpringBootApplication(scanBasePackages = { "org.chenile", "org.chenile.samples" })
@EntityScan({"org.chenile"})
@EnableJpaRepositories(basePackages = {"org.chenile"})
public class IngestionApplication extends SpringBootServletInitializer {

	private static final Logger log = LoggerFactory.getLogger(IngestionApplication.class);

	private final Environment env;

	public IngestionApplication(Environment env) {
		this.env = env;
	}




	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(IngestionApplication.class);
		//app.setAdditionalProfiles(args[0]);
		Environment env = app.run(args).getEnvironment();
		logApplicationStartup(env);
	}
	private static void logApplicationStartup(Environment env) {
		String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
		String applicationName = env.getProperty("spring.application.name");
		String serverPort = env.getProperty("server.port");
		String contextPath = Optional.ofNullable(env.getProperty("server.servlet.context-path"))
				.filter(StringUtils::isNotBlank)
				.orElse("/");
		String hostAddress = "localhost";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("The host name could not be determined, using `localhost` as fallback");
		}
		log.info(
				"""
    
                ----------------------------------------------------------
                \tApplication '{}' is running! Access URLs:
                \tLocal: \t\t{}://localhost:{}{}
                \tExternal: \t{}://{}:{}{}
                \tProfile(s): \t{}
                ----------------------------------------------------------""",
				applicationName,
				protocol,
				serverPort,
				contextPath,
				protocol,
				hostAddress,
				serverPort,
				contextPath,
				env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles()
		);
	}


}
