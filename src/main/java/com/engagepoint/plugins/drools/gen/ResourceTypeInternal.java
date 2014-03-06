package com.engagepoint.plugins.drools.gen;

import org.drools.builder.ResourceType;

enum ResourceTypeInternal {

    DRT("drt", ResourceType.DRL, "added rule"),
    PACKAGE("package", ResourceType.DRL, "added drools package"),
    FUNCTION("function", ResourceType.DRL, "added rule"),
    DRL(ResourceType.DRL.getDefaultExtension(), ResourceType.DRL, "added rule"),
    DRF(ResourceType.DRF.getDefaultExtension(), ResourceType.DRF, "added flow"),
    BRL(ResourceType.BRL.getDefaultExtension(), ResourceType.BRL, "added rule"),
    DECISION_TABLE(ResourceType.DTABLE.getDefaultExtension(), ResourceType.DTABLE, "added decision table"),
    CHANGE_SET("xml", ResourceType.CHANGE_SET, "added change set"),
    BPMN_2(ResourceType.BPMN2.getDefaultExtension(), ResourceType.BPMN2, "added change set");

    private String extension;
    private ResourceType resourceType;
    private String comment;

    private ResourceTypeInternal(String extension, ResourceType resourceType, String comment) {
        this.extension = extension;
        this.resourceType = resourceType;
        this.comment = comment;
    }

    public String getExtension() {
        return extension;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public String getComment() {
        return comment;
    }

    public static ResourceTypeInternal valueOfFile(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (extension.equalsIgnoreCase(DRT.getExtension())) {
            return DRT;
        }
        if (extension.equalsIgnoreCase(PACKAGE.getExtension())) {
            return PACKAGE;
        }
        if (extension.equalsIgnoreCase(FUNCTION.getExtension())) {
            return FUNCTION;
        }
        if (extension.equalsIgnoreCase(DRL.getExtension())) {
            return DRL;
        }
        if (extension.equalsIgnoreCase(DRF.getExtension())) {
            return DRF;
        }
        if (extension.equalsIgnoreCase(BRL.getExtension())) {
            return BRL;
        }
        if (extension.equalsIgnoreCase(DECISION_TABLE.getExtension())) {
            return DECISION_TABLE;
        }
        if (extension.equalsIgnoreCase(CHANGE_SET.getExtension())) {
            return CHANGE_SET;
        }
        if (extension.equalsIgnoreCase(BPMN_2.getExtension())) {
            return BPMN_2;
        }
        return null;
    }
}
