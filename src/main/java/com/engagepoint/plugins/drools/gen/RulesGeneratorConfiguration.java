package com.engagepoint.plugins.drools.gen;

import java.util.List;

public class RulesGeneratorConfiguration {

    private String packageName;
    private String localBaseDirPath;
    private String generatedPath;
    private List<SpreadsheetTemplateMapping> templatesSheetMappings;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getLocalBaseDirPath() {
        return localBaseDirPath;
    }

    public void setLocalBaseDirPath(String localBaseDirPath) {
        this.localBaseDirPath = localBaseDirPath;
    }

    public String getGeneratedPath() {
        return generatedPath;
    }

    public void setGeneratedPath(String generatedPath) {
        this.generatedPath = generatedPath;
    }

    public List<SpreadsheetTemplateMapping> getTemplatesSheetMappings() {
        return templatesSheetMappings;
    }

    public void setTemplatesSheetMappings(List<SpreadsheetTemplateMapping> templatesSheetMappings) {
        this.templatesSheetMappings = templatesSheetMappings;
    }
}
