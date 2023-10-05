package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.annotation;

import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums.EsIndexNameEnum;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * es 文档注解
 *
 * @author rothscode
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EsDocument {

    /**
     * index 索引名称
     */
    EsIndexNameEnum index();
}
