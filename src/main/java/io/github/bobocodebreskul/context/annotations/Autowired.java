package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used in a custom Inversion of Control (IoC) container and Dependency Injection (DI)
 * framework to indicate that a dependency should be automatically injected at runtime.
 *
 * <p>{@code @Autowired} can be applied to constructors within a class, enabling the IoC container
 * to automatically resolve and inject the required dependencies during the initialization of the
 * annotated component.
 *
 * <p>This annotation is a key element in achieving loose coupling and promoting
 * the principles of dependency injection, allowing components to be more modular, maintainable, and
 * easily testable.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 *   @BringComponent
 *   public class MyComponent {
 *
 *       @Autowired
 *       MyComponent(BeanClass beanToInject) {
 *          //your logic
 *       };
 *   }
 * }
 * </pre>
 *
 * @see BringComponent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.CONSTRUCTOR})
public @interface Autowired {

}
