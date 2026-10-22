
package com.example.food_delivery_app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EgyptianPhoneValidator
        implements ConstraintValidator<EgyptianPhone, String> {

    private static final Pattern EGYPTIAN_PHONE_PATTERN =
            Pattern.compile("^(?:01[0125]\\d{8}|(?:\\+20|0020)1[0125]\\d{8})$");

    @Override
    public boolean isValid(
            String phone,
            ConstraintValidatorContext context) {

        // Let @NotBlank handle missing or blank values.
        if (phone == null || phone.isBlank()) {
            return true;
        }

        return EGYPTIAN_PHONE_PATTERN.matcher(phone).matches();
    }
}
