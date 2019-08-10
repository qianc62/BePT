package org.processmining.importing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Importer {
	String name();

	String help() default "";

	String sortName() default "";

	String extension();

	boolean connectToLog() default false;

	boolean useFuzzyMatching() default true;
}
