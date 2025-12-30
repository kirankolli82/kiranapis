package com.kiran.musicapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MusicapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicapiApplication.class, args);
	}

}
