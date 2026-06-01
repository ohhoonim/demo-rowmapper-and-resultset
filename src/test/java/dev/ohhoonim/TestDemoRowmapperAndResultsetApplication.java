package dev.ohhoonim;

import org.springframework.boot.SpringApplication;

public class TestDemoRowmapperAndResultsetApplication {

	public static void main(String[] args) {
		SpringApplication.from(DemoRowmapperAndResultsetApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
