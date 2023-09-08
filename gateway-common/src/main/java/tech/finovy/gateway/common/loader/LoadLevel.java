package tech.finovy.gateway.common.loader;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface LoadLevel {
    /**
     * Name string.
     *
     * @return the string
     */
    String name();

    /**
     * Order int.
     *
     * @return the int
     */
    int order() default 0;

    /**
     * Scope enum.
     */
    Scope scope() default Scope.SINGLETON;
}
