package com.engagepoint.plugins.mojo;

import com.engagepoint.plugins.drools.gen.RepositoryManagementException;
import com.engagepoint.plugins.drools.gen.RepositoryManager;
import com.engagepoint.plugins.drools.io.DefaultFlowSerializer;
import com.engagepoint.plugins.drools.io.FlowSerializer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.drools.KnowledgeBase;

import java.io.File;

/**
 * @goal serialize-repository
 */
public class FlowSerializerMojo extends AbstractMojo {

    /**
     * @parameter expression="${drools.repositoryFile}" default-value="knowledgeRepository.xml"
     */
    private File repositoryFile;

    /**
     * @parameter expression="${drools.repositoryConfigFile}" default-value="jackrabbit-repository.xml"
     */
    private File repositoryConfigFile;

    /**
     * @parameter expression="${drools.targetFilePath}" default-value="./output.repository"
     */
    private String targetFilePath;

    /**
     * @parameter expression="${drools.overwriteTargetFile}" default-value="true"
     */
    private boolean overwriteTargetFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        RepositoryManager repositoryManager = new RepositoryManager();
        KnowledgeBase knowledgeBase;
        try {
            repositoryManager.loadRepository(repositoryFile, repositoryConfigFile);
            knowledgeBase = repositoryManager.loadFromRepository();
        } catch (RepositoryManagementException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        FlowSerializer flowSerializer = new DefaultFlowSerializer(knowledgeBase);
        try {
            flowSerializer.serializeToFile(targetFilePath, overwriteTargetFile);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
