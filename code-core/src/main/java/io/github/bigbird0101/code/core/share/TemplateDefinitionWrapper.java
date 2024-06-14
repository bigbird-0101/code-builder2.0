package io.github.bigbird0101.code.core.share;

import io.github.bigbird0101.code.core.config.FileUrlResource;
import io.github.bigbird0101.code.core.context.GenericTemplateContext;
import io.github.bigbird0101.code.core.context.aware.AbstractTemplateContextProvider;
import io.github.bigbird0101.code.core.factory.DefaultListableTemplateFactory;
import io.github.bigbird0101.code.core.factory.config.TemplateDefinition;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.bigbird0101.code.core.config.AbstractEnvironment.putTemplateContent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;

/**
 * @author bigbird-0101
 * @date 2024-06-11 22:40
 */
public class TemplateDefinitionWrapper extends AbstractTemplateContextProvider {
    private TemplateDefinition templateDefinition;
    private String templateName;
    private List<TemplateDefinitionWrapper> dependTemplateDefinitionList;

    public TemplateDefinitionWrapper(TemplateDefinition templateDefinition, String templateName, List<TemplateDefinitionWrapper> dependTemplateDefinitionList) {
        this.templateDefinition = templateDefinition;
        this.templateName = templateName;
        this.dependTemplateDefinitionList = dependTemplateDefinitionList;
    }

    public TemplateDefinition getTemplateDefinition() {
        return templateDefinition;
    }

    public void setTemplateDefinition(TemplateDefinition templateDefinition) {
        this.templateDefinition = templateDefinition;
    }

    public List<TemplateDefinitionWrapper> getDependTemplateDefinitionList() {
        return dependTemplateDefinitionList;
    }

    public void setDependTemplateDefinitionList(List<TemplateDefinitionWrapper> dependTemplateDefinitionList) {
        this.dependTemplateDefinitionList = dependTemplateDefinitionList;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public void registerAndRefreshTemplate() {
        List<TemplateDefinitionWrapper> sortWrapper = new ArrayList<>();
        sortGetDependTemplateDefinitionList(this, sortWrapper);
        GenericTemplateContext templateContext = (GenericTemplateContext) getTemplateContext();
        DefaultListableTemplateFactory defaultListableTemplateFactory = templateContext.getTemplateFactory();
        for (TemplateDefinitionWrapper templateDefinitionWrapper : sortWrapper) {
            try {
                TemplateDefinition templateDefinition = templateDefinitionWrapper.getTemplateDefinition();
                FileUrlResource templateResource = (FileUrlResource) templateDefinition.getTemplateResource();
                File newFile = templateResource.getFile();
                putTemplateContent(newFile.getAbsolutePath(), IOUtils.toString(newInputStream(newFile.toPath()), UTF_8));
                String templateName = templateDefinitionWrapper.getTemplateName();
                templateContext.registerTemplateDefinition(templateName, templateDefinition);
                defaultListableTemplateFactory.preInstantiateTemplates();
                defaultListableTemplateFactory.refreshTemplate(templateContext.getTemplate(templateName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sortGetDependTemplateDefinitionList(TemplateDefinitionWrapper templateDefinitionWrapper,
                                                    List<TemplateDefinitionWrapper> sortWrapper) {
        for (TemplateDefinitionWrapper templateDefinitionWrapperTemp : templateDefinitionWrapper.getDependTemplateDefinitionList()) {
            sortGetDependTemplateDefinitionList(templateDefinitionWrapperTemp, sortWrapper);
        }
        sortWrapper.add(templateDefinitionWrapper);
    }
}