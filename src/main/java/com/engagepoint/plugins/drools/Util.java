package com.engagepoint.plugins.drools;

import org.drools.io.Resource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class Util {

    private Util() {
    }

    /**
     * Method for patching common resources with right package.
     *
     * @param resource    resource for patching
     * @param packageName package name that must have resource source after update
     * @return resource source with update package name
     */
    public static String getCommonResource(Resource resource, String packageName) {
        String source;
        try {
            source = inputStreamAsString(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        source = updatePackage(source, packageName);
        return source;
    }

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

    /**
     * Method for getting resource string from input stream.
     *
     * @param is input stream for converting
     * @return string with input stream source
     */
    private static String inputStreamAsString(InputStream is) throws IOException {
        final char[] buffer = new char[1024];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, "UTF-8");
        int read;
        do {
            read = in.read(buffer, 0, buffer.length);
            if (read > 0) {
                out.append(buffer, 0, read);
            }
        } while (read >= 0);
        return out.toString();
    }

    /**
     * Generate file name filter for files with different extensions. For
     * getting include/exclude filtering use include flag.
     * <p/>
     * Example:
     * <p/>
     * <pre>
     *  For file name filter that show only files with extensions <code>.txt</code>
     *  and <code>.log</code>:
     *      getFilenameFilter(true, ".txt", ".log")
     *
     *  For file name filter that show all files except <code>.txt</code>:
     *      getFilenameFilter(false, ".txt")
     * </pre>
     *
     * @param include    flag for name filter
     * @param extensions array of file extensions that must includes in filter
     * @return file name filter that includes input parameters
     */
    public static FilenameFilter getFilenameFilter(final boolean include, final String... extensions) {
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                for (String extension : extensions) {
                    if (name.toLowerCase().endsWith(extension)) {
                        return include;
                    }
                }
                return !include;
            }
        };
    }

    public static FilenameFilter getFilenameFilter(final boolean include, final String[] extensions,
            final String[] subfolders) {
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (extensions != null) {
                    for (String extension : extensions) {
                        if (name.toLowerCase().endsWith(extension)) {
                            return include;
                        }
                    }
                }

                if (subfolders != null) {
                    for (String subfolder : subfolders) {
                        /*
                         * The dir is really has the subfolder if it ends with '/'+ subfolder
                         * or contains '/'+ subfolder +'/'.
                         * The '/' here means the host system file separator.
                         * */
                        subfolder = System.getProperty("file.separator") + subfolder;
                        if (dir.getAbsolutePath().endsWith(subfolder) ||
                                dir.getAbsolutePath().indexOf(subfolder + System.getProperty("file.separator")) != -1) {
                            return include;
                        }
                    }
                }

                return !include;
            }
        };
    }
}
