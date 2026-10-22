package com.example.food_delivery_app.common;

import com.example.food_delivery_app.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Messages {

    private final MessageSource messageSource;

    public String get(ErrorCode code, Object... args) {
        return resolve(code.messageKey(), code.name(), args);
    }

    public String get(SuccessCode code, Object... args) {
        return resolve(code.messageKey(), code.name(), args);
    }

    private String resolve(String key, String fallback, Object[] args) {
        return messageSource.getMessage(key, args, fallback, LocaleContextHolder.getLocale());
    }
}