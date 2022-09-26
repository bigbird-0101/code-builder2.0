package com.fpp.code.core.template.targetfile;

import com.fpp.code.core.template.AbstractTemplate;
import com.fpp.code.core.template.Template;
import com.fpp.code.core.template.TemplateResolver;
import com.fpp.code.exception.TemplateResolveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Administrator
 */
public class PatternTargetFilePrefixNameStrategy implements TargetFilePrefixNameStrategy {
    private Logger logger = LogManager.getLogger(getClass());

    private static final int TYPE_VALUE=3;

    private String pattern;
    /**
     * 获取命名策略代表值
     *
     * @return
     */
    @Override
    public int getTypeValue() {
        return TYPE_VALUE;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 命名策略
     *
     * @param template  模板
     * @param srcSource 源资源 比如表名
     * @return
     */
    @Override
    public String prefixStrategy(Template template, String srcSource) {
        AbstractTemplate abstractTemplate= (AbstractTemplate) template;
        TemplateResolver templateResolver = abstractTemplate.getTemplateResolver();
        String resolver = null;
        try {
            resolver = templateResolver.resolver(getPattern(), template.getTemplateVariables());
        } catch (TemplateResolveException e) {
            logger.error(e);
        }
        return resolver;
    }
}