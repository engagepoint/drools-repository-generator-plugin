package com.engagepoint.plugins.drools.gen;

import java.util.Set;

public class RepositoryGeneratorConfiguration {

    /**
     * Package Mapping contains:
     *
     * <pre>
     *   Key: Asset path/package with slashes replaced by dots
     *   Value: Asset name/nameSpace in Guvnor repository (when created/updated)
     * </pre>
     */
    private Set<RepositoryPackage> packagesMapping;

    private String targetRepositoryFile;
    private String packagesDirectory;
    private String modelDirectory;
    private String buildVersion;
    private String buildDate;
    private String appVersion;
    private boolean searchInClassPath;

    public Set<RepositoryPackage> getPackagesMapping() {
        return packagesMapping;
    }

    public void setPackagesMapping(Set<RepositoryPackage> packagesMapping) {
        this.packagesMapping = packagesMapping;
    }

    public String getTargetRepositoryFile() {
        return targetRepositoryFile;
    }

    public void setTargetRepositoryFile(String targetRepositoryFile) {
        this.targetRepositoryFile = targetRepositoryFile;
    }

    public String getPackagesDirectory() {
        return packagesDirectory;
    }

    public void setPackagesDirectory(String packagesDirectory) {
        this.packagesDirectory = packagesDirectory;
    }

    public String getModelDirectory() {
        return modelDirectory;
    }

    public void setModelDirectory(String modelDirectory) {
        this.modelDirectory = modelDirectory;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isSearchInClassPath() {
        return searchInClassPath;
    }

    public void setSearchInClassPath(boolean searchInClassPath) {
        this.searchInClassPath = searchInClassPath;
    }
}
