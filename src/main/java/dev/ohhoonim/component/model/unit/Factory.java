package dev.ohhoonim.component.model.unit;

import org.jmolecules.stereotype.Stereotype;

@Stereotype(groups = "5sa")
public @interface Factory {
   String value() default "";  
}
