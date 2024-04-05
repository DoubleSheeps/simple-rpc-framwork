package ustc.young.annotations;

import java.lang.annotation.*;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 16:49
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}
