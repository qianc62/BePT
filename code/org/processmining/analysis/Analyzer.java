package org.processmining.analysis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Analyzer {
	String name();

	String help() default "";

	String sortName() default "";

	boolean connected() default true; // set to true if objects to analyze need

	// to be offered in a single provided
	// object,

	// set to false if the objects to analyze should come from separate provided
	// objects

	String[] names(); // names of the parameters (as they should be shown in the
	// user interface)
}
