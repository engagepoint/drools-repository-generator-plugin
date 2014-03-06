package com.engagepoint.plugins.drools.compilers;

import com.engagepoint.plugins.drools.Constants;

import java.util.logging.Logger;

/**
 * Abstract class for compiling strategy
 */
abstract class AbstractDroolsCompiler {

    protected static final Logger LOGGER = Logger.getLogger("DroolsCompiler");

    /**
     * Method for patching resource source with right package.
     *
     * @param source      resource source
     * @param packageName package name that must have source after update
     * @return source with update package name
     */
    public static String updatePackage(String source, String packageName) {
        return source.replaceAll(Constants.PACKAGE_NAME, packageName);
    }
}