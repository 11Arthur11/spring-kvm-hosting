package me.parhamziaei.practice.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.parhamziaei.practice.validation.validator.EnumValueValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValueValidator.class)
public @interface EnumValue {
    String message() default "the value doesn't match any values in enum";
    Class<? extends Enum<?>> enumClass();
    String enumValueGetter() default "value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    boolean allowNull() default false;
}
