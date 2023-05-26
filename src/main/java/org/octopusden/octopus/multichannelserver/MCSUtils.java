package org.octopusden.octopus.multichannelserver;

import java.io.File;

/**
 * Date: 31.08.2009
 */
public class MCSUtils {
    public static final String CONST_FILENAME_SERVER_XML = "server.xml";
    public static final int CONST_MAX_CHANNEL_COUNT = 99;

    public static String getMCSConfigFolderName(String mcsFolderName) {
        return (mcsFolderName + File.separator + "conf");
    }

    public static String getMCSConfigUserFolderName(String mcsFolderName) {
        return (getMCSConfigFolderName(mcsFolderName) + File.separator + "user");
    }

    public static String getMCSProfilesFolderName(String mcsFolderName) {
        return (mcsFolderName + File.separator + "profiles");
    }

    public static String getMCSBackupFolderName(String mcsFolderName) {
        return (getMCSProfilesFolderName(mcsFolderName) + File.separator + "backup");
    }

    public static String getMCSConfigInitFolderName(String mcsFolderName) {
        return getMCSConfigFolderName(mcsFolderName) + File.separator + "init";
    }
}
