package com.engagepoint.plugins.drools.gen;

import org.apache.commons.io.FileUtils;
import org.drools.builder.impl.KnowledgeBuilderImpl;
import org.drools.common.DroolsObjectOutputStream;
import org.drools.definitions.impl.KnowledgePackageImp;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.repository.AssetItem;
import org.drools.repository.PackageItem;
import org.drools.repository.RulesRepository;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class RepositoryGenerator extends AbstractKnowledgeGenerator {

    private static final Logger LOGGER = Logger.getLogger(RepositoryGenerator.class.getSimpleName());

    private RulesRepository repository;

    private int lastPackageRevision;

    private RepositoryGeneratorConfiguration configuration;
    private KnowledgeGenerator knowledgeGenerator;

    public RepositoryGenerator(RepositoryGeneratorConfiguration configuration) {
        this.configuration = configuration;
        this.knowledgeGenerator = new KnowledgeGenerator(configuration);
    }

    public void generate() throws Exception {

        LOGGER.info("Target repository file: " + configuration.getTargetRepositoryFile());
        LOGGER.info("Model directory: " + configuration.getModelDirectory());
        LOGGER.info("Packages directory: " + configuration.getPackagesDirectory());
        LOGGER.info("Packages: " + configuration.getPackagesMapping());

        if (configuration.getPackagesDirectory() != null && !new File(configuration.getPackagesDirectory()).exists()) {
            throw new RuntimeException("Packages directory does not exists: " + configuration.getPackagesDirectory());
        }

        if (configuration.getModelDirectory() != null && !new File(configuration.getModelDirectory()).exists()) {
            throw new RuntimeException("Model directory does not exists: " + configuration.getModelDirectory());
        }

        if (configuration.getPackagesMapping().isEmpty()) {
            LOGGER.info("Packages is empty - import will not be executed...");
            return;
        }

        if (configuration.getTargetRepositoryFile() == null) {
            configuration.setTargetRepositoryFile(new File(".").getAbsolutePath() + "/target/repository_export.xml");
        }

        configuration.setSearchInClassPath(
                configuration.getPackagesDirectory() == null || configuration.getPackagesDirectory().trim().equals(""));
        if (configuration.getTargetRepositoryFile() != null) {
            FileUtils.deleteQuietly(new File(configuration.getTargetRepositoryFile()));
        }

        RepositoryManager repositoryManager = new RepositoryManager();
        repository = repositoryManager.createRepository();

        Properties packageProperties = new Properties();

        try {
            for (RepositoryPackage processedPackage : configuration.getPackagesMapping()) {
                LOGGER.info("->>");
                LOGGER.info("->> Add new package: " + processedPackage.getName());
                addClassPathPackage(repository, processedPackage.getName(), processedPackage.getRequired(),
                        processedPackage.getSubPackages(), processedPackage.getDepPackages());
                LOGGER.info("->> New package created: " + processedPackage.getName());
                LOGGER.info("->>");
                packageProperties.put(processedPackage.getName(), "1");
            }

            if (repository.containsPackage(RulesRepository.DEFAULT_PACKAGE)) {
                PackageItem defaultPackage = repository.loadDefaultPackage();
                String repoVersion = configuration.getAppVersion() + "." + lastPackageRevision;
                Resource resource = ResourceFactory.newReaderResource(new StringReader(repoVersion));
                addAsset(defaultPackage, "version", "Repository version: " + repoVersion, "txt", resource,
                        "Repository Version added");
            }

            repository.save();

            FileUtils.forceMkdir(new File(new File(configuration.getTargetRepositoryFile()).getParent()));
            packageProperties.store(new FileOutputStream(new File(configuration.getTargetRepositoryFile()).getParent() +
                    "/rule-packages-to-import.properties"), null);
            FileOutputStream file = new FileOutputStream(configuration.getTargetRepositoryFile());
            repository.exportRulesRepositoryToStream(file);
        } finally {
            repository.logout();
            repository = null;
        }

    }

    /**
     * Add process resources from classpath to repository by package name
     */
    private void addClassPathPackage(RulesRepository repository, String packageName, Boolean required,
            Set<String> subPackages, Set<String> depPackages) throws Exception {

        PackageItem packageItem = repository
                .createPackage(packageName, "Package version: " + configuration.getAppVersion());

        KnowledgeBuilderImpl kbuilder = knowledgeGenerator
                .getPackageKnowledge(packageName, packageItem, required, subPackages, depPackages);

        addModelResource(repository, packageName, configuration.getModelDirectory());

        PackageRevision packageRevision = getPackageRevision(packageItem);

        LOGGER.info("->> Last revision: " + packageRevision.getRevision());
        LOGGER.info("->> Modify date: " + packageRevision.getModifyDate());

        StringBuilder revisionContent = new StringBuilder();
        revisionContent.append(packageRevision.getRevision());
        revisionContent.append("\n");
        revisionContent.append(packageRevision.getModifyDate());

        addAsset(packageItem, "revision", "Revision: " + packageRevision, "txt", revisionContent.toString(),
                "Revision: " + packageRevision);
        String packageVersion = configuration.getAppVersion() + "." + packageRevision;
        packageItem.updateDescription("Package version: " + packageRevision);
        lastPackageRevision = lastPackageRevision > packageRevision.getRevision() ?
                lastPackageRevision :
                packageRevision.getRevision();
        if (kbuilder.getErrors().size() != 0) {
            throw new RuntimeException("Errors during package compilation!");
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutput out = new DroolsObjectOutputStream(bout);
        // Write compiled package into asset and skip default empty package
        out.writeObject(((KnowledgePackageImp) kbuilder.newKnowledgeBase().getKnowledgePackage(packageName)).pkg);
        packageItem.updateCompiledPackage(new ByteArrayInputStream(bout.toByteArray()));

        out.flush();
        out.close();

        // repository.save();

        repository.createPackageSnapshot(packageName, "Release");
        PackageItem snapshot = repository.loadPackageSnapshot(packageName, "Release");
        snapshot.updateStringProperty(packageVersion, "project.build.version");
        snapshot.checkin(packageVersion);
        // repository.loadPackageSnapshot(packageName,
        // "Release").checkin("asasdsad");
        // repository.save();
    }

    private PackageRevision getPackageRevision(PackageItem packageItem) {
        int revision = 0;
        String modifyDate = null;
        RuleStringContentParser ruleStringContentParser = new RuleStringContentParser();
        Iterator<AssetItem> itr = packageItem.getAssets();
        while (itr.hasNext()) {
            AssetItem assetItem = itr.next();
            if (!assetItem.isBinary()) {
                String assetContent = assetItem.getContent();
                int assetRevision = ruleStringContentParser.getRevisionFromString(assetContent);
                LOGGER.info("---> Asset: " + assetItem.getName() + " Revision: " + assetRevision);
                if (revision < assetRevision) {
                    revision = assetRevision;
                    modifyDate = ruleStringContentParser.getModifyDateFromString(assetContent);
                }
                revision = revision < assetRevision ? assetRevision : revision;
                if (revision > 0) {
                    // Update asset description
                    assetItem.updateDescription(assetItem.getDescription() + " Revision: " + revision);
                    assetItem.checkin(assetItem.getCheckinComment());
                }
            }
        }
        return new PackageRevision(revision, modifyDate);
    }

    private void addModelResource(RulesRepository repository, String packageName, String modelDirectory)
            throws Exception {
        if (modelDirectory != null && !modelDirectory.trim().equals("")) {
            File modelDir = new File(modelDirectory);

            File[] files = modelDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    LOGGER.info("->> Add model: " + file.getName());
                    // String fileNameNoExtension =
                    // FilenameUtils.removeExtension(file.getName());
                    AssetItem model = repository.loadPackage(packageName).addAsset("model", file.getName());
                    model.updateBinaryContentAttachment(new FileInputStream(file));
                    model.updateFormat("jar");
                    model.checkin("model");
                }
            }
        }

    }

    private static class PackageRevision {

        int revision;
        String modifyDate;

        public PackageRevision(int revision, String modifyDate) {
            this.revision = revision;
            this.modifyDate = modifyDate;
        }

        public int getRevision() {
            return revision;
        }

        public String getModifyDate() {
            return modifyDate;
        }

        @Override
        public String toString() {
            return revision + " (" + modifyDate + ")";
        }

    }
}
