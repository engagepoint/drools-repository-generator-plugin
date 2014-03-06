package com.engagepoint.plugins.drools.gen;

import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.repository.PackageItem;
import org.drools.repository.PackageIterator;
import org.drools.repository.RulesRepository;
import org.drools.repository.RulesRepositoryAdministrator;
import org.drools.repository.RulesRepositoryConfigurator;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepositoryManager {

    private static final Logger LOGGER = Logger.getLogger(RepositoryManager.class.getSimpleName());

    private RulesRepository repository;

    public void loadRepository(File repositoryFile, File repositoryConfigFile) throws RepositoryManagementException {
        InputStream repositoryXml;
        try {
            repositoryXml = new FileInputStream(repositoryFile);
        } catch (Exception e) {
            throw new RepositoryManagementException(e);
        }
        RulesRepository rulesRepository = (repositoryConfigFile == null) ?
                createRepository() :
                createRepository(repositoryConfigFile);
        rulesRepository.importRepository(repositoryXml);
    }

    public void loadRepository(String repositoryFilePath, String repositoryConfigFilePath)
            throws RepositoryManagementException {
        File repositoryConfigFile = (repositoryConfigFilePath != null) ? new File(repositoryConfigFilePath) : null;
        loadRepository(new File(repositoryFilePath),
                (repositoryConfigFile != null && repositoryConfigFile.exists()) ? repositoryConfigFile : null);
    }

    public KnowledgeBase loadFromRepository() throws RepositoryManagementException {
        if (repository == null) {
            throw new IllegalStateException("Rules repository is NULL. You have to load repository first");
        }

        KnowledgeBuilder knowledgeBuilder = createKnowledgeBuilder();

        PackageIterator packageIterator = repository.listPackages();
        while (packageIterator.hasNext()) {
            PackageItem packageItem = packageIterator.next();
            try {
                knowledgeBuilder.add(loadPackageResource(packageItem.getName()), ResourceType.PKG);
            } catch (ResourceNotFoundException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return loadFromRepository(knowledgeBuilder);
    }

    public KnowledgeBase loadFromRepository(String packageName) throws RepositoryManagementException {
        if (repository == null) {
            throw new IllegalStateException("Rules repository is NULL. You have to load repository first");
        }

        KnowledgeBuilder knowledgeBuilder = createKnowledgeBuilder();

        Resource resource = loadPackageResource(packageName);
        knowledgeBuilder.add(resource, ResourceType.PKG);
        return loadFromRepository(knowledgeBuilder);
    }

    private KnowledgeBase loadFromRepository(KnowledgeBuilder knowledgeBuilder) throws RepositoryManagementException {
        KnowledgeBuilderErrors errors = knowledgeBuilder.getErrors();
        if (errors.size() > 0) {
            for (KnowledgeBuilderError error : errors) {
                LOGGER.log(Level.WARNING, error.getMessage(), error);
            }
            throw new RepositoryManagementException("Could not parse knowledge");
        }
        KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
        knowledgeBase.addKnowledgePackages(knowledgeBuilder.getKnowledgePackages());
        return knowledgeBase;
    }

    public synchronized RulesRepository createRepository(File repositoryConfigFile)
            throws RepositoryManagementException {
        RepositoryConfig repositoryConfig;
        try {
            InputStream repositoryConfigXml = new FileInputStream(repositoryConfigFile);
            repositoryConfig = RepositoryConfig.create(repositoryConfigXml, "root");
        } catch (Exception e) {
            throw new RepositoryManagementException(e);
        }
        TransientRepository rawRepository = new TransientRepository(repositoryConfig);
        RulesRepositoryConfigurator config;
        try {
            config = getRepositoryConfigurator();
        } catch (RepositoryException e) {
            throw new RepositoryManagementException("Unable to create repository", e);
        }
        return createRepository(rawRepository, config);
    }

    public synchronized RulesRepository createRepository(String repositoryConfigFilePath)
            throws RepositoryManagementException {
        return createRepository(new File(repositoryConfigFilePath));
    }

    public synchronized RulesRepository createRepository() throws RepositoryManagementException {
        RulesRepositoryConfigurator config;
        Repository rawRepository;
        try {
            config = getRepositoryConfigurator();
            rawRepository = config.getJCRRepository();
        } catch (RepositoryException e) {
            throw new RepositoryManagementException("Unable to create repository", e);
        }
        return createRepository(rawRepository, config);
    }

    private RulesRepositoryConfigurator getRepositoryConfigurator() throws RepositoryException {
        Properties prop = new Properties();
        prop.setProperty("org.drools.repository.configurator",
                "org.drools.repository.jackrabbit.JackrabbitRepositoryConfigurator");
        return RulesRepositoryConfigurator.getInstance(prop);
    }

    private RulesRepository createRepository(Repository rawRepository, RulesRepositoryConfigurator configurator)
            throws RepositoryManagementException {
        try {
            Session session = rawRepository
                    .login(new SimpleCredentials("project_build_system", "password".toCharArray()));
            RulesRepositoryAdministrator admin = new RulesRepositoryAdministrator(session);
            if (admin.isRepositoryInitialized()) {
                admin.clearRulesRepository();
            }
            configurator.setupRepository(session);
            repository = new RulesRepository(session);
            return repository;
        } catch (Exception e) {
            throw new RepositoryManagementException(e);
        }
    }

    private KnowledgeBuilder createKnowledgeBuilder() {
        return KnowledgeBuilderFactory.newKnowledgeBuilder();
    }

    private Resource loadPackageResource(String packageName) throws ResourceNotFoundException {
        if (!repository.containsPackage(packageName)) {
            throw new ResourceNotFoundException("Unable to find package with name " + packageName);
        }

        PackageItem packageItem = repository.loadPackage(packageName);

        if (repository.containsSnapshot(packageName, "Release")) {
            packageItem = repository.loadPackageSnapshot(packageName, "Release");
        }

        return ResourceFactory.newByteArrayResource(packageItem.getCompiledPackageBytes());
    }
}
