package dev.ohhoonim.component.model.unit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jmolecules.stereotype.Stereotype;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Documented
@Stereotype(groups = "5sa")
public @interface ValueObject {
    String value() default "";     
}
