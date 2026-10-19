
package com.example.food_delivery_app.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Documented // @Documented indicates that this annotation should be included in the Javadoc.
@Constraint(validatedBy = EgyptianPhoneValidator.class) // @Constraint specifies the validator class that will handle the validation logic for this annotation.
@Target({FIELD, PARAMETER, ANNOTATION_TYPE}) // @Target specifies the kinds of program elements to which this annotation can be applied. In this case, it can be applied to fields, method parameters, and other annotations.
@Retention(RUNTIME) // @Retention specifies that this annotation will be available at runtime, which is necessary for validation frameworks to access it during the validation process.
public @interface EgyptianPhone {

    String message() default "Invalid Egyptian phone number";

    // The groups element is used to specify validation groups.
    // Validation groups allow you to group constraints and apply them conditionally based on the context in which the validation is being performed.
    // By default, this element is an empty array, meaning that the constraint belongs to the default validation group.
    // for example, you might have different validation groups for creating a new entity versus updating an existing one, and you can apply different constraints based on the group.
    Class<?>[] groups() default {};

    // The payload element is used to specify custom payload objects that can be associated with the constraint.
    // Payloads can be used to carry additional information about the validation failure, such as error codes or severity levels.
    Class<? extends Payload>[] payload() default {};
}
