package io.commoncore.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPassword.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Password must be at least 8 characters long and contain uppercase, lowercase, digit and special character";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    class Validator implements ConstraintValidator<StrongPassword, String> {
        @Override
        public boolean isValid(String password, ConstraintValidatorContext context) {
            if (password == null) return false;
            return password.length() >= 8 &&
                   password.matches(".*[A-Z].*") &&
                   password.matches(".*[a-z].*") &&
                   password.matches(".*[0-9].*") &&
                   password.matches(".*[!@#$%^&*].*");
        }
    }
}
