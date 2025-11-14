package me.parhamziaei.practice.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.parhamziaei.practice.validation.annotation.EnumValue;

import java.lang.reflect.Method;
import java.util.Arrays;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

    private Class<? extends Enum<?>> enumClass;
    private String enumValueGetter;
    private boolean allowNull;

    @Override
    public void initialize(EnumValue annotation) {
        this.enumClass = annotation.enumClass();
        this.enumValueGetter = annotation.enumValueGetter();
        this.allowNull = annotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (allowNull && value == null) {
            return true;
        }
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            Method method = enumClass.getMethod(enumValueGetter);
            return Arrays.stream(enumClass.getEnumConstants())
                    .map(e -> {
                        try {
                            return method.invoke(e);
                        } catch (Exception ignored) {
                            return null;
                        }
                    }).anyMatch(value::equals);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

}
