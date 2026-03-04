package com.roadmap.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.roadmap.backend.admin.entity",
        "com.roadmap.backend.auth.entity",
        "com.roadmap.backend.consultation.entity",
        "com.roadmap.backend.waitlist.entity",
        "com.roadmap.backend.review.entity"
})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
