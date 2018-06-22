package cn.itcast.util;

import java.lang.annotation.*;


@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SolrField {

    //域的名字
    String name() default "";

    //是否存储
    boolean storead() default true;

    //是否高亮
    boolean ishl() default false;

    //前缀
    String prefix() default "<span style=\"color:red;\">";

    //后缀
    String suffix() default "</span>";

}
