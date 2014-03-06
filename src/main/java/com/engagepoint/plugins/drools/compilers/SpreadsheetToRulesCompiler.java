package com.engagepoint.plugins.drools.compilers;

import com.engagepoint.plugins.drools.Constants;
import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;

import static com.engagepoint.plugins.drools.gen.SpreadsheetTemplateMapping.TemplateMappingItem;

public class SpreadsheetToRulesCompiler extends AbstractDroolsCompiler implements DroolsCompiler {

    @Override
    public void compile(DroolsCompilerConfiguration inputParameters) throws DroolsCompilerException {
        List<TemplateMappingItem> mappingItems = inputParameters.getTemplatesMapping();

        final ExternalSpreadsheetCompiler converter = new ExternalSpreadsheetCompiler();
        Resource template;
        String drl;
        File result;

        for (TemplateMappingItem mappingItem : mappingItems) {
            String templateFileName = mappingItem.getDrtFileName();
            result = new File(inputParameters.getOutputPath() + "/" + mappingItem.getDrlFileName(true));

            if (!inputParameters.isOverwrite() && result.exists()) {
                LOGGER.log(Level.WARNING,
                        "File already exists. Generated rules [" + templateFileName + "] not stored.");
                continue;
            }

            template = ResourceFactory.newFileResource(inputParameters.getTemplatesPath() + "/" + templateFileName);
            try {
                // The data starts at row 3, column 1 (e.g. A3)
                LOGGER.info("Compiling DRL out from " + mappingItem);
                if (mappingItem.getWorksheetName() != null) {
                    drl = converter
                            .compile(inputParameters.getXlsResource().getInputStream(), mappingItem.getWorksheetName(),
                                    template.getInputStream(), mappingItem.getRow(), mappingItem.getColumn());
                } else {
                    drl = converter
                            .compile(inputParameters.getXlsResource().getInputStream(), template.getInputStream(),
                                    mappingItem.getRow(), mappingItem.getColumn());
                }
                // Complete rule by editing package and agenda-group
                drl = updatePackage(drl, inputParameters.getPackageName());
                drl = drl.replaceAll(Constants.AGENDA_GROUP, mappingItem.getEscapedAgendaGroupName());
                // Hack for working with spreadsheets that contains arrays
                drl = drl.replaceAll("\\. ", "\\.");
                // Delete empty lines
                drl = drl.replaceAll("(?m)^[ \t]*\r?\n", "");
                // Storing result in file
                Writer out = new OutputStreamWriter(new FileOutputStream(result), "UTF-8");
                try {
                    out.write(drl);
                    LOGGER.info("Generated rules [" + templateFileName + "] was stored successfully.");
                } finally {
                    out.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Cannot to store generated rules to file.", e);
                throw new DroolsCompilerException(e);
            }
        }
    }
}
