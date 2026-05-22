package io.github.danieledalia.tma.spring.argument;

import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import io.github.danieledalia.tma.core.model.TelegramUser;
import io.github.danieledalia.tma.spring.annotation.TelegramMiniApp;
import io.github.danieledalia.tma.spring.annotation.TelegramMiniAppUser;
import io.github.danieledalia.tma.spring.context.TelegramMiniAppContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TelegramMiniAppArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TelegramMiniApp.class)
                && TelegramMiniAppContext.class.isAssignableFrom(parameter.getParameterType())
                || parameter.hasParameterAnnotation(TelegramMiniAppUser.class)
                && TelegramUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        TelegramMiniAppContext context = TelegramMiniAppContextHolder.getRequiredContext();

        if (parameter.hasParameterAnnotation(TelegramMiniAppUser.class)) {
            return context.user();
        }

        return context;
    }
}
