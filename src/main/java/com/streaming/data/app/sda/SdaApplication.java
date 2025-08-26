package com.streaming.data.app.sda;

import com.streaming.data.app.sda.config.StreamingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StreamingConfig.class)
public class SdaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SdaApplication.class, args);
	}

}
