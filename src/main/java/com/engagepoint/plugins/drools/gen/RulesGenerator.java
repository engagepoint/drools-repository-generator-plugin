package com.engagepoint.plugins.drools.gen;

import com.engagepoint.plugins.drools.compilers.DroolsCompilerConfiguration;
import com.engagepoint.plugins.drools.compilers.DroolsCompilerException;
import com.engagepoint.plugins.drools.compilers.SpreadsheetToRulesCompiler;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;

import java.io.File;
import java.util.logging.Logger;

/**
 * Utility class for generation rules from spreadsheet and rule templates. And
 * for overwriting common templates.
 */
public class RulesGenerator {

    private static final Logger LOGGER = Logger.getLogger(RulesGenerator.class.getSimpleName());

    public static final String PACKAGE_PATH_LABEL = "${packagePath}";

    private final String globalBaseDirPath;

    public RulesGenerator(String globalBaseDirPath) {
        this.globalBaseDirPath = globalBaseDirPath;
    }

    /**
     * <p/>
     * Method for generation rules from spreadsheets and templates.
     * <p/>
     * Spreadsheets for generation must stored in folder input
     * <code>object</code> package name /drools/templates. And after generation
     * rules storing in folder input <code>object</code> package name
     * /drools/generated.
     *
     * @param input     - configuration input parameters
     * @param overwrite - if rule file already exists
     */
    public void compileFromSpreadsheets(RulesGeneratorConfiguration input, final boolean overwrite)
            throws GenerationException {
        String localBaseDirPath = input.getLocalBaseDirPath();
        String packageName = input.getPackageName();

        String baseDirPath = (localBaseDirPath == null) ? globalBaseDirPath : localBaseDirPath;

        for (SpreadsheetTemplateMapping tsm : input.getTemplatesSheetMappings()) {
            tsm.setSpreadsheetPath(baseDirPath.replace('\\', '/') + "/" +
                    tsm.getSpreadsheetPath().replace(PACKAGE_PATH_LABEL, packageName.replace('.', '/')) + "/");
            tsm.setTemplatesPath(baseDirPath.replace('\\', '/') + "/" +
                    tsm.getTemplatesPath().replace(PACKAGE_PATH_LABEL, packageName.replace('.', '/')) + "/");
        }

        LOGGER.info("------------------------------------------------------------------");
        LOGGER.info("Starting rule compilation from spreadsheet's");
        // Use Case package name
        LOGGER.info("  Target package: " + input.getPackageName());
        // Path to folder for storing rules after compilation
        String outputPath = baseDirPath.replace('\\', '/') + "/" + input.getPackageName().replace('.', '/') +
                input.getGeneratedPath();
        // create directory if it not exists
        new File(outputPath).mkdir();
        LOGGER.info("  Output directory: " + outputPath);
        for (SpreadsheetTemplateMapping tsm : input.getTemplatesSheetMappings()) {
            File file;
            String filePath = tsm.getSpreadsheetPath();
            try {
                // 	URL spreadsheets = new URL(spdshtsPath);
                file = new File(filePath);
            } catch (Exception x) {
                throw new GenerationException(
                        input.getPackageName() + " (" + filePath + ") does not appear to be a valid package");
            }
            if (file.exists()) {
                Resource xlsResource = ResourceFactory.newFileResource(file);
                SpreadsheetToRulesCompiler compiler = new SpreadsheetToRulesCompiler();

                DroolsCompilerConfiguration compilerInputParameters = new DroolsCompilerConfiguration();
                compilerInputParameters.setOutputPath(outputPath);
                compilerInputParameters.setOverwrite(overwrite);
                compilerInputParameters.setPackageName(input.getPackageName());
                compilerInputParameters.setTemplatesMapping(tsm.getItems());
                compilerInputParameters.setTemplatesPath(tsm.getTemplatesPath());
                compilerInputParameters.setXlsName(file.getName());
                compilerInputParameters.setXlsResource(xlsResource);

                try {
                    compiler.compile(compilerInputParameters);
                } catch (DroolsCompilerException e) {
                    throw new GenerationException(e);
                }
            }
        }
        LOGGER.info("Compiling rules finished successfully");
        LOGGER.info("------------------------------------------------------------------");
    }
}
