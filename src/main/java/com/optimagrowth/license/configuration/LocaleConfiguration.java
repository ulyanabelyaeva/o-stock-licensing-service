package com.optimagrowth.license.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleConfiguration {

    /**
     * Устанавливает локаль US по умолчанию
    */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.US);
        return localeResolver;
    }

    /**
     * Для локали US сообщение будет искаться из файла messages_es.properties
     * Если сообщения на выбранном языке не будет найдено в файле, то о будет предпринята попытка найти сообщение
     * в файле по умолчанию - messages.properties
     * properties.
     * */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        //Если сообщение не найдено в файле возвращается код сообщения, вместо выброса исключения
        messageSource.setUseCodeAsDefaultMessage(true);

        //Задает базовое имя файлов с переводами сообщений на разные языки
        messageSource.setBasenames("messages");
        return messageSource;
    }

}
