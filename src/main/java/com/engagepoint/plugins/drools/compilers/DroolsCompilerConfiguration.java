package com.engagepoint.plugins.drools.compilers;

import org.drools.io.Resource;

import java.util.List;

import static com.engagepoint.plugins.drools.gen.SpreadsheetTemplateMapping.TemplateMappingItem;

public class DroolsCompilerConfiguration {

    private String packageName;
    private String xlsName;
    private Resource xlsResource;
    private String outputPath;
    private String templatesPath;
    private List<TemplateMappingItem> templatesMapping;
    private boolean overwrite;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getXlsName() {
        return xlsName;
    }

    public void setXlsName(String xlsName) {
        this.xlsName = xlsName;
    }

    public Resource getXlsResource() {
        return xlsResource;
    }

    public void setXlsResource(Resource xlsResource) {
        this.xlsResource = xlsResource;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getTemplatesPath() {
        return templatesPath;
    }

    public void setTemplatesPath(String templatesPath) {
        this.templatesPath = templatesPath;
    }

    public List<TemplateMappingItem> getTemplatesMapping() {
        return templatesMapping;
    }

    public void setTemplatesMapping(List<TemplateMappingItem> templatesMapping) {
        this.templatesMapping = templatesMapping;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
