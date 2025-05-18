package es.swapsounds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication

// Enable serialization of Page<T> via DTO
@EnableSpringDataWebSupport(
		pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)

public class DwsProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(DwsProjectApplication.class, args);
	}

}



