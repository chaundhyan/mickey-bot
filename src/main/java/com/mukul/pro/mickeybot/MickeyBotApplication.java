package com.mukul.pro.mickeybot;

import com.microsoft.bot.integration.AdapterWithErrorHandler;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.spring.BotController;
import com.microsoft.bot.integration.spring.BotDependencyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
// Use the default BotController to receive incoming Channel messages. A custom
// controller could be used by eliminating this import and creating a new
// RestController.
// The default controller is created by the Spring Boot container using
// dependency injection. The default route is /api/messages.
@Import({BotController.class})
public class MickeyBotApplication extends BotDependencyConfiguration {

	public static void main(String[] args) {
		SpringApplication.run(MickeyBotApplication.class, args);
	}

	/**
	 * Returns a custom Adapter that provides error handling.
	 *
	 * @param configuration The Configuration object to use.
	 * @return An error handling BotFrameworkHttpAdapter.
	 */
	@Override
	public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
		return new AdapterWithErrorHandler(configuration);
	}
}
