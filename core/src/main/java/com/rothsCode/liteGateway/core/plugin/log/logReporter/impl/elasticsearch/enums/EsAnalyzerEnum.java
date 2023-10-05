package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums;

import lombok.Getter;

/**
 * es分词器枚举
 *
 * @author rothscode
 * @version 1.0
 */
@Getter
public enum EsAnalyzerEnum {
    /**
     * 不使用分词
     */
    NO(""),
    /**
     * 标准分词，默认分词器
     */
    STANDARD("standard"),

    /**
     * ik_smart：会做最粗粒度的拆分；已被分出的词语将不会再次被其它词语占有
     */
    IK_SMART("ik_smart"),

    /**
     * ik_max_word ：会将文本做最细粒度的拆分；尽可能多的拆分出词语
     */
    IK_MAX_WORD("ik_max_word"),

    /**
     * ik_max_word+ngram：先进行ik_max_word分词，然后再进行ngram的拆分，用于前缀模糊搜索
     */
    IK_MAX_WORD_NGRAM("ik_max_word_ngram");

    private final String type;

    EsAnalyzerEnum(String type) {
        this.type = type;
    }
}
