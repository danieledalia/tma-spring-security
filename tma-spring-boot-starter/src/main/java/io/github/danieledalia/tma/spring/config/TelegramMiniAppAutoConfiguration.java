package io.github.danieledalia.tma.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.danieledalia.tma.core.validation.TelegramInitDataParser;
import io.github.danieledalia.tma.core.validation.TelegramInitDataValidator;
import io.github.danieledalia.tma.spring.argument.TelegramMiniAppArgumentResolver;
import io.github.danieledalia.tma.spring.filter.TelegramMiniAppFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(TelegramMiniAppProperties.class)
@ConditionalOnProperty(prefix = "telegram.miniapp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TelegramMiniAppAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TelegramInitDataParser telegramInitDataParser(ObjectMapper objectMapper) {
        return new TelegramInitDataParser(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public TelegramInitDataValidator telegramInitDataValidator(
            TelegramMiniAppProperties properties,
            TelegramInitDataParser parser
    ) {
        return new TelegramInitDataValidator(
                properties.getBotToken(),
                properties.getMaxAuthAge(),
                java.time.Clock.systemUTC(),
                parser
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public TelegramMiniAppFilter telegramMiniAppFilter(
            TelegramInitDataValidator validator,
            TelegramMiniAppProperties properties
    ) {
        return new TelegramMiniAppFilter(validator, properties);
    }

    @Bean
    public FilterRegistrationBean<TelegramMiniAppFilter> telegramMiniAppFilterRegistration(TelegramMiniAppFilter filter) {
        FilterRegistrationBean<TelegramMiniAppFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registration;
    }

    @Bean
    public WebMvcConfigurer telegramMiniAppWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new TelegramMiniAppArgumentResolver());
            }
        };
    }
}
