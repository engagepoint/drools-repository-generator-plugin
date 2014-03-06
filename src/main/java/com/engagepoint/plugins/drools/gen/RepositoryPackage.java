package com.engagepoint.plugins.drools.gen;

import java.util.Set;

public class RepositoryPackage {

    private String name;

    private Boolean required;

    private Set<String> subPackages;

    private Set<String> depPackages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getSubPackages() {
        return subPackages;
    }

    public void setSubPackages(Set<String> subPackages) {
        this.subPackages = subPackages;
    }

    public Set<String> getDepPackages() {
        return depPackages;
    }

    public void setDepPackages(Set<String> depPackages) {
        this.depPackages = depPackages;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return "ProcessedPackage [name=" + name + "]";
    }

}
