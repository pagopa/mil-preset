package it.pagopa.swclient.mil.preset.validation.constraints;

import it.pagopa.swclient.mil.preset.bean.Role;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Constraint(validatedBy = {
	TerminalIdNotNullForRoleValidator.class
})
public @interface TerminalIdNotNullForRole {

	String message() default "";

	Role[] roles() default {};

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
