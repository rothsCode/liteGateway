package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.annotation;


import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums.EsAnalyzerEnum;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums.EsDataTypeEnum;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * es field
 *
 * @author rothscode
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface EsField {

    /**
     * 数据类型
     *
     * @return
     */
    EsDataTypeEnum type() default EsDataTypeEnum.TEXT;

    /**
     * 指定分词器
     *
     * @return
     */
    EsAnalyzerEnum analyzer() default EsAnalyzerEnum.STANDARD;
}
