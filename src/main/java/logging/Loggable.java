package logging;

import logging.enums.MethodType;
import logging.enums.ServiceType;
import logging.enums.UserIdentifierType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Loggable {

	UserIdentifierType userIdentifier();

	ServiceType serviceType() default ServiceType.REST;

	MethodType methodType() default MethodType.GET;
	
	String userId();

}
