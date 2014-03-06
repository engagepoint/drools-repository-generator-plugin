package com.engagepoint.plugins.mojo;

import com.engagepoint.plugins.drools.gen.RepositoryGenerator;
import com.engagepoint.plugins.drools.gen.RepositoryGeneratorConfiguration;
import com.engagepoint.plugins.drools.gen.RepositoryPackage;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.Set;

/**
 * @goal generate-repository
 */
public class DroolsRepositoryGeneratorMojo extends AbstractMojo {

    /**
     * @parameter
     */
    private String targetRepositoryFile;

    /**
     * @parameter
     */
    private String modelDirectory;

    /**
     * @parameter
     */
    private String buildVersion;

    /**
     * @parameter
     */
    private String buildDate;

    /**
     * @parameter
     */
    private String packagesDirectory;

    /**
     * @parameter
     */
    private Set<RepositoryPackage> packages4Processing;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            RepositoryGeneratorConfiguration configuration = new RepositoryGeneratorConfiguration();
            configuration.setTargetRepositoryFile(targetRepositoryFile);
            configuration.setPackagesDirectory(packagesDirectory);
            configuration.setPackagesMapping(packages4Processing);
            configuration.setModelDirectory(modelDirectory);
            configuration.setBuildVersion(buildVersion);
            configuration.setBuildDate(buildDate);
            configuration.setAppVersion(configuration.getBuildVersion()); // + " " + buildDate; // TODO?

            RepositoryGenerator repositoryGenerator = new RepositoryGenerator(configuration);
            repositoryGenerator.generate();
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
