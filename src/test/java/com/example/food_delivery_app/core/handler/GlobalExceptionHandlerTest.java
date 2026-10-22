package com.example.food_delivery_app.core.handler;

import com.example.food_delivery_app.exceptions.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)

class GlobalExceptionHandlerTest {

    @Test
    void everyErrorCodeHasAMessage() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        for (ErrorCode code : ErrorCode.values()) {
            assertThat(bundle.containsKey(code.messageKey())).as(code.name()).isTrue();
        }
    }

}