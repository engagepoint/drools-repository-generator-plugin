package com.engagepoint.plugins.mojo;

import com.engagepoint.plugins.drools.gen.GenerationException;
import com.engagepoint.plugins.drools.gen.RulesGenerator;
import com.engagepoint.plugins.drools.gen.RulesGeneratorConfiguration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.List;

/**
 * @goal generate-from-template
 */
public class SpreadsheetToRulesGeneratorMojo extends AbstractMojo {

    /**
     * @parameter
     */
    private List<RulesGeneratorConfiguration> packages;

    /**
     * @parameter
     */
    private String globalBaseDirPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        RulesGenerator generator = new RulesGenerator(globalBaseDirPath);
        for (RulesGeneratorConfiguration prc : packages) {
            try {
                generator.compileFromSpreadsheets(prc, true);
            } catch (GenerationException e) {
                getLog().info("Unable to compile rules from spreadsheets for package" + prc.getPackageName(), e);
            }
        }
    }
}
