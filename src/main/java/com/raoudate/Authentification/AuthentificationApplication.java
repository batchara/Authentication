package com.raoudate.Authentification;

import com.raoudate.Authentification.repository.RoleRepository;
import com.raoudate.Authentification.repository.UserRepository;
import com.raoudate.Authentification.role.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class AuthentificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthentificationApplication.class, args);
	}

	@Bean
	public CommandLineRunner Runner(RoleRepository roleRepository) {
		return args -> {

			if(roleRepository.findByName("ROLE_USER").isEmpty()) {
				roleRepository.save(
						Role.builder().name("ROLE_USER").build()
				);
			}

		};
	}

}
