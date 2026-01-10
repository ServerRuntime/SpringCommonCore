package io.commoncore.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark entities that should be audited
 * Entities annotated with @Auditable will have their changes tracked automatically
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    /**
     * Entity name for logging (optional, defaults to class simple name)
     */
    String value() default "";
}
