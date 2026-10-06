package br.com.alura.screenmatch;

import br.com.alura.screenmatch.principal.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Autowired
	private Environment environment;

	@Override
	public void run(String... args) {
		Principal principal = new Principal();
		principal.exibeMenu(environment.getProperty("OMDBAPI_KEY"));
	}
}
