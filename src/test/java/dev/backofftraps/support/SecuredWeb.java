package dev.backofftraps.support;

import dev.backofftraps.app.HelloController;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * A minimal secured web context: Boot's MVC and security auto-configuration plus {@link HelloController}.
 * No component scan, no embedded server, every trap is added explicitly by the test.
 */
public final class SecuredWeb {

    private SecuredWeb() {
    }

    public static WebApplicationContextRunner runner() {
        return new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JacksonAutoConfiguration.class,
                        HttpMessageConvertersAutoConfiguration.class,
                        DispatcherServletAutoConfiguration.class,
                        WebMvcAutoConfiguration.class,
                        SecurityAutoConfiguration.class,
                        OAuth2ResourceServerAutoConfiguration.class))
                .withUserConfiguration(HelloController.class);
    }

    public static MockMvc mockMvc(WebApplicationContext context) {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
}
