package com.engagepoint.plugins;

import com.engagepoint.plugins.drools.gen.KnowledgeProcessingException;
import com.engagepoint.plugins.drools.gen.RepositoryGenerator;
import com.engagepoint.plugins.drools.gen.RepositoryGeneratorConfiguration;
import com.engagepoint.plugins.drools.gen.RepositoryManager;
import com.engagepoint.plugins.drools.gen.RepositoryPackage;
import org.apache.commons.io.FileUtils;
import org.drools.KnowledgeBase;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RepositoryGeneratorTest {

    @Test
    public void test() throws Exception {
        Set<RepositoryPackage> packages4Processing = new HashSet<RepositoryPackage>();
        packages4Processing.add(createProcessedPackage(TestConstants.PACKAGE_1, "p1"));
        packages4Processing.add(createProcessedPackage(TestConstants.PACKAGE_2));
        packages4Processing.add(createProcessedPackage(TestConstants.PACKAGE_3));

        RepositoryManager repositoryManager = generateRepository(packages4Processing);

        KnowledgeBase knowledgeBase = repositoryManager.loadFromRepository(TestConstants.PACKAGE_1);
        assertNotNull(knowledgeBase);

        assertEquals(1, knowledgeBase.getKnowledgePackages().size());
        assertNotNull(knowledgeBase.getProcess(TestConstants.PROCESS_1));
        assertNotNull(knowledgeBase.getProcess(TestConstants.PROCESS_2));

        knowledgeBase = repositoryManager.loadFromRepository(TestConstants.PACKAGE_2);
        assertNotNull(knowledgeBase);
        assertEquals(1, knowledgeBase.getKnowledgePackages().size());
        assertNotNull(knowledgeBase.getProcess(TestConstants.PROCESS_3));

        knowledgeBase = repositoryManager.loadFromRepository(TestConstants.PACKAGE_3);
        assertNotNull(knowledgeBase);
        assertEquals(1, knowledgeBase.getKnowledgePackages().size());
        assertNotNull(knowledgeBase.getProcess(TestConstants.PROCESS_4));
    }

    @Test(expected = KnowledgeProcessingException.class)
    public void test_DuplicateRulesNames() throws Exception {
        Set<RepositoryPackage> packages4Processing = new HashSet<RepositoryPackage>();
        packages4Processing.add(createProcessedPackage(TestConstants.PACKAGE_DUPLICATES_RULE_NAME));

        generateRepository(packages4Processing);
    }

    private RepositoryManager generateRepository(Set<RepositoryPackage> packages4Processing) throws Exception {
        String targetRepositoryFile = new File(".").getAbsolutePath() + "/target/repository_export.xml";
        String modelDirectory = new File(".").getAbsolutePath() + "/target/test-classes/models";
        String packagesDirectory = new File(".").getAbsolutePath() + "/target/test-classes";
        String buildVersion = "version";
        String buildDate = "01.01.01";

        FileUtils.deleteQuietly(new File(targetRepositoryFile));

        RepositoryGeneratorConfiguration configuration = new RepositoryGeneratorConfiguration();
        configuration.setTargetRepositoryFile(targetRepositoryFile);
        configuration.setPackagesDirectory(packagesDirectory);
        configuration.setPackagesMapping(packages4Processing);
        configuration.setModelDirectory(modelDirectory);
        configuration.setBuildVersion(buildVersion);
        configuration.setBuildDate(buildDate);

        RepositoryGenerator repositoryGenerator = new RepositoryGenerator(configuration);
        repositoryGenerator.generate();

        assertTrue(new File(targetRepositoryFile).exists());

        RepositoryManager repositoryManager = new RepositoryManager();
        repositoryManager.loadRepository(targetRepositoryFile, null);
        return repositoryManager;
    }

    private RepositoryPackage createProcessedPackage(String packageName, String... subPackages) {
        RepositoryPackage processedPackage = new RepositoryPackage();
        processedPackage.setRequired(true);
        processedPackage.setName(packageName);
        Set<String> subPacks = new HashSet<String>();
        Set<String> depPacks = new HashSet<String>();
        for (String s : subPackages) {
            subPacks.add(s);
        }
        processedPackage.setSubPackages(subPacks);
        processedPackage.setDepPackages(depPacks);
        return processedPackage;
    }
}
