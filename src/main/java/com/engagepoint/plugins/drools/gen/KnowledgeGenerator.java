package com.engagepoint.plugins.drools.gen;

import com.engagepoint.plugins.drools.Util;
import org.apache.commons.io.FilenameUtils;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.lang.descr.PackageDescr;
import org.drools.repository.PackageItem;
import org.drools.util.ChainedProperties;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Utility class for creating KnowledgeBuilder by process package name and
 * package item for generation repository.
 */
public class KnowledgeGenerator extends AbstractKnowledgeGenerator {

    private static final Logger LOGGER = Logger.getLogger(KnowledgeGenerator.class.getSimpleName());

    // Suppresses default constructor, ensuring non-instantiability.
    KnowledgeGenerator(RepositoryGeneratorConfiguration configuration) {
        this.configuration = configuration;
    }

    private RepositoryGeneratorConfiguration configuration;

    /**
     * Method for creating KnowledgeBuilder instance by process package name and
     * package item for generation guvnor repository.
     *
     * @param packageName process package name
     * @param packageItem current guvnor repository package item (can be
     *                    <code>null</code> if create KnowledgeBuilder form class path
     *                    sources with out generation guvnor repository)
     * @return KnowledgeBuilder instance with all resources that stored in
     * common, generate and root process folders
     */
    public KnowledgeBuilderImpl getPackageKnowledge(final String packageName, PackageItem packageItem, Boolean required,
            Set<String> subPackages, Set<String> depPackages) {

        // See if we can find a packagebuilder.conf
        // We do this manually here, as we cannot rely on PackageBuilder doing
        // this correctly
        // note this chainedProperties already checks System properties too
        ChainedProperties chainedProperties = new ChainedProperties("packagebuilder.conf",
                RepositoryGeneratorConfiguration.class.getClassLoader(), // pass
                // this
                // as
                // it
                // searches
                // currentThread
                // anyway
                false); // false means it ignores any default values

        // the default compiler. This is nominally JANINO but can be overridden
        // by setting drools.dialect.java.compiler to ECLIPSE

        Properties properties = new Properties();
        properties.setProperty("drools.dialect.java.compiler",
                chainedProperties.getProperty("drools.dialect.java.compiler", "ECLIPSE"));
        PackageBuilderConfiguration pkgConf = new PackageBuilderConfiguration(properties);

        pkgConf.setAllowMultipleNamespaces(false);
        pkgConf.addSemanticModule(new BPMNSemanticModule());

        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(pkgConf);

        KnowledgeBuilderImpl kbuilder = (KnowledgeBuilderImpl) builder;
        kbuilder.getPackageBuilder().addPackage(new PackageDescr(packageName));
        if (depPackages != null) {
            for (String depPackage : depPackages) {
                LOGGER.log(Level.INFO, "Adding dep-package: " + depPackage);
                kbuilder = addPackageResource(kbuilder, packageItem, packageName, depPackage, true);
            }
        }
        kbuilder = addPackageResource(kbuilder, packageItem, packageName, packageName, required);
        if (subPackages != null) {
            for (String subPackage : subPackages) {
                LOGGER.log(Level.INFO, "Adding sub-package: " + subPackage);
                kbuilder = addPackageResource(kbuilder, packageItem, packageName, packageName + "." + subPackage, true);
            }
        }
        if (builder.getErrors().size() != 0) {
            throw new RuntimeException("Errors during package compilation!");
        }
        return kbuilder;
    }

    /**
     * Add resources from <code>fromPackage</code> to process package that
     * creating.
     *
     * @param kbuilder    KnowledgeBuilder instance that updated
     * @param packageItem drools guvnor repository package item
     * @param toPackage   package that creating
     * @param fromPackage package that merge with creating package
     * @param required    <code>true</code> - if resources that stored in
     *                    <code>fromPackage</code> must be in package that created
     * @return KnowledgeBuilder instance with <code>fromPackage</code> resources
     */
    private KnowledgeBuilderImpl addPackageResource(KnowledgeBuilderImpl kbuilder, PackageItem packageItem,
            String toPackage, String fromPackage, boolean required) {
        final String fromPackagePath = fromPackage.replace('.', '/');
        File directory = null;
        ClassLoader cld = null;
        boolean isJar = false;

        if (configuration.isSearchInClassPath()) {
            try {
                cld = KnowledgeGenerator.class.getClassLoader();
                if (cld == null) {
                    throw new RuntimeException("Can't get class loader.");
                }
                URL resource = cld.getResource(fromPackagePath);

                if (resource == null) {
                    if (required) {
                        throw new RuntimeException("No resource for " + fromPackagePath);
                    } else {
                        return kbuilder;
                    }
                }
                isJar = resource.getProtocol().equals("jar");
                directory = new File(resource.getFile());

            } catch (NullPointerException e) {
                throw new RuntimeException(fromPackage + " does not appear to be a valid package");
            }
        } else {
            String a = configuration.getPackagesDirectory();
            a = a.endsWith("/") ? a : a + "/";
            String b = (fromPackagePath.startsWith("/") ? fromPackagePath.substring(1) : fromPackagePath);
            directory = new File(a + b);
        }

        if (directory.exists() || isJar) {
            // Search for imports
            FilenameFilter filter = Util.getFilenameFilter(true, ".package");
            kbuilder = addResourceDirectory(kbuilder, packageItem, toPackage, fromPackagePath, directory, filter,
                    configuration.isSearchInClassPath(), isJar);
            // Search for templates
            filter = Util.getFilenameFilter(true, ".drt");
            kbuilder = addResourceDirectory(kbuilder, packageItem, toPackage, fromPackagePath, directory, filter,
                    configuration.isSearchInClassPath(), isJar);
            // Search for models
            filter = Util.getFilenameFilter(true, "model.drl");
            kbuilder = addResourceDirectory(kbuilder, packageItem, toPackage, fromPackagePath, directory, filter,
                    configuration.isSearchInClassPath(), isJar);
            // Search for functions
            filter = Util.getFilenameFilter(true, "functions.drl", ".function");
            kbuilder = addResourceDirectory(kbuilder, packageItem, toPackage, fromPackagePath, directory, filter,
                    configuration.isSearchInClassPath(), isJar);
            // Search for all other assets
            filter = Util.getFilenameFilter(false,
                    new String[]{".package", ".drt", "model.drl", "functions.drl", ".function"},
                    new String[]{"templates"});

            kbuilder = addResourceDirectory(kbuilder, packageItem, toPackage, fromPackagePath, directory, filter,
                    configuration.isSearchInClassPath(), isJar);

        } else {
            if (required) {
                throw new RuntimeException(fromPackage + " does not appear to be a valid package");
            } else {
                return kbuilder;
            }
        }
        return kbuilder;
    }

    /**
     * Add resources from <code>directory</code> to process package that creating.
     *
     * @param kbuilder    KnowledgeBuilder instance that updated
     * @param packageItem drools guvnor repository package item
     * @param packageName creating package name
     * @param path        to process resource directory
     * @param directory   process resource directory
     * @param filter      file name filter
     * @return KnowledgeBuilder instance with <code>directory</code> resources
     */
    private KnowledgeBuilderImpl addResourceDirectory(KnowledgeBuilderImpl kbuilder, PackageItem packageItem,
            String packageName, String path, File directory, FilenameFilter filter, boolean isPackage, boolean isJar) {

        String[] files;
        if (!isJar) {
            files = directory.list(filter);
        } else {
            try {
                files = getJarResourceListing(path.replace('.', '/') + "/", filter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (String fileName : files) {
            final String filePath = path + "/" + fileName;
            final String fileNameNoExtension = FilenameUtils.removeExtension(fileName);

            Resource resource;
            if (isPackage) {
                resource = ResourceFactory.newClassPathResource(filePath);
            } else {
                resource = ResourceFactory.newFileResource(directory + "/" + fileName);
            }

            ResourceTypeInternal resourceTypeInternal = ResourceTypeInternal.valueOfFile(fileName);
            Object content = resource;

            if (ResourceTypeInternal.DRT.equals(resourceTypeInternal)) {
                content = Util.getCommonResource(resource, packageName);
                resource = ResourceFactory.newReaderResource(new StringReader((String) content), "UTF-8");
                fileName = fileName.replaceAll("\\.drt", "\\.drl");
            }

            if (resourceTypeInternal != null) {
                LOGGER.log(Level.INFO, "->> Add new resource: " + filePath);
                if (ResourceTypeInternal.DECISION_TABLE.equals(resourceTypeInternal)) {
                    DecisionTableConfiguration config = KnowledgeBuilderFactory.newDecisionTableConfiguration();
                    config.setInputType(DecisionTableInputType.XLS);
                    kbuilder.add(resource, resourceTypeInternal.getResourceType(), config);
                } else {
                    kbuilder.add(resource, resourceTypeInternal.getResourceType());
                }
            }

            if (resourceTypeInternal != null && packageItem != null) {
                try {
                    if (content instanceof Resource) {
                        addAsset(packageItem, fileNameNoExtension, fileName + " " + configuration.getAppVersion(),
                                resourceTypeInternal.getExtension(), (Resource) content,
                                resourceTypeInternal.getComment());
                    } else if (content instanceof String) {
                        addAsset(packageItem, fileNameNoExtension, fileName + " " + configuration.getAppVersion(),
                                resourceTypeInternal.getExtension(), (String) content,
                                resourceTypeInternal.getComment());
                    } else {
                        throw new IllegalArgumentException("Unknown resource type for " + fileName + ". Exist type = " +
                                content.getClass().getName() + ", expected String or Resource.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }
        if (kbuilder.getErrors().size() != 0) {
            List<String> errors = new ArrayList<String>();
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                LOGGER.log(Level.SEVERE, error.toString());
                errors.add(error.getMessage());
            }
            throw new KnowledgeProcessingException("Errors during package compilation!", errors);
        }
        return kbuilder;
    }

    private String[] getJarResourceListing(String path, FilenameFilter filter) throws URISyntaxException, IOException {
        ClassLoader cld = KnowledgeGenerator.class.getClassLoader();
        if (cld == null) {
            throw new RuntimeException("Can't get class loader.");
        }
        URL dirURL = cld.getResource(path);

        /* A JAR path */
        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // strip
        // out
        // only
        // the
        // JAR
        // file
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
        Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
        Set<String> result = new HashSet<String>(); // avoid duplicates in case it is a subdirectory
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            if (name.startsWith(path) && !name.equals(path) &&
                    filter.accept(new File(dirURL.getFile()), name)) { // filter
                // according
                // to
                // the
                // path
                String entry = name.substring(path.length());
                int checkSubdir = entry.indexOf('/');
                if (checkSubdir == -1) {
                    result.add(entry);
                }

            }
        }
        return result.toArray(new String[result.size()]);
    }
}
