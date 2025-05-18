package es.swapsounds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(
		pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO) // Enable serialization of Page<T> via DTO

public class DwsProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(DwsProjectApplication.class, args);
	}

}



