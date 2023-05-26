package org.octopusden.octopus.multichannelserver;

import org.octopusden.octopus.fileutils.FileUtils;
import org.octopusden.octopus.multichannelserver.config.*;
import org.octopusden.octopus.multichannelserver.config.interfaces.IParameterStorage;
import org.octopusden.octopus.multichannelserver.config.joiner.ConfigurationJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

/**
 * Date: 28.08.2009
 */
public class XMLConfJoiner {
  private static final String CONST_FILENAME_CHANNEL_DEFINITION_XML = "channel_definition.xml";
  private static final String CONST_FILENAME_SERVER_DEFINITION_XML = "server_definition.xml";
  private static final String CONST_FILENAME_CONNECT_DEFINITION_XML = "connect_definition.xml";
  private static final Logger logger = LoggerFactory.getLogger(XMLConfJoiner.class);

  private static void printHelp(boolean showUsage) throws IOException {
    InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("META-INF/maven/org.octopusden.octopus.multichannelserver/XMLConfJoiner/pom.properties");
    Properties mProps = new Properties();
    if (resourceAsStream != null) {
      mProps.load(resourceAsStream);
    }
    logger.info("Multichannel Server Configuration Joiner ({})", mProps.get("version"));
    if (showUsage) {
      logger.info("Usage: XMLConfJoiner [-servername SERVERNAME] [-join] <configuration_folder>");
      logger.info("Usage: use \"-join\" option to join separated configuration");
    }
  }

  @SuppressWarnings({ "CallToSystemExit" })
  public static void main(String[] args) throws Exception {

    // parse calling arguments
    boolean join = false;
    String serverName = null;
    String mcsFolderName = null;

    for(String arg : args) {
      if (arg.equals("-join")) {
        join = true;
        continue;
      }

      if (arg.equals("-servername")) {
        serverName = new String("");
        continue;
      }
      if (serverName != null && serverName.isEmpty()) {
        serverName = arg;
        continue;
      }
      mcsFolderName = arg;
    }

    logger.info("Servername " + (serverName == null ? "IS NOT SET" : ":'" + serverName + "'"));
    logger.info("Working in: '" + (mcsFolderName != null ? mcsFolderName : "UNKNOWN PATH") + "'");

    if (mcsFolderName == null) {
      printHelp(true);
      //noinspection CallToSystemExit
      System.exit(0);
    }

    printHelp(false);

    if (join) {
      joinConfigurations(mcsFolderName, logger);
      return;
    }

    File file = new File(mcsFolderName);
    if (!file.exists() || !file.isDirectory()) {
      logger.error("Directory ({}) doesn't exist!", file.getName());
      //noinspection CallToSystemExit
      System.exit(1);
    }

    // Load Program Options
    logger.info("Loading channel definition file: " + CONST_FILENAME_CHANNEL_DEFINITION_XML);
    ChannelDefinition programOptionsChannelDefinition = new ChannelDefinition(CONST_FILENAME_CHANNEL_DEFINITION_XML);
    logger.info("Loading server definition file: " + CONST_FILENAME_SERVER_DEFINITION_XML);
    ServerDefinition programOptionsServerDefinition = new ServerDefinition(CONST_FILENAME_SERVER_DEFINITION_XML);
    logger.info("Loading connect definition file: " + CONST_FILENAME_CONNECT_DEFINITION_XML);
    ConnectDefinition connectDefinition = new ConnectDefinition(CONST_FILENAME_CONNECT_DEFINITION_XML);

    if (!programOptionsChannelDefinition.isLoaded ||
      !programOptionsServerDefinition.isLoaded ||
      !connectDefinition.isLoaded ||
      ( !checkProgramOptions(programOptionsServerDefinition, programOptionsChannelDefinition, connectDefinition) ))
      System.exit(1);

    // Load Current Client Config
    logger.info("Loading MultiChannelServer configuration from folder: " + file.getPath());
    ConfigDefinition ClientConfigDefinition = new ConfigDefinition(mcsFolderName);

    // check preconditoins
    logger.info("Check preconditions...");
    if (!SeparateUtils.checkPreconditions(mcsFolderName, ClientConfigDefinition))
      System.exit(1);

    // Start Separating Client Config
    logger.info("Start separating client config");
    ConfigDefinition userConfigDefinition = new ConfigDefinition();
    if (!SeparateUtils.separateClientConfig(programOptionsServerDefinition,
      programOptionsChannelDefinition,
      connectDefinition,
      ClientConfigDefinition,
      userConfigDefinition))
      System.exit(1);

    // Backup Current Data
    if (!backupCurrentData(mcsFolderName)) System.exit(1);

    // Saving new 'init' configuration...
    logger.info(String.format("Saving new 'init' configuration to folder (%s)...", MCSUtils.getMCSConfigInitFolderName(mcsFolderName)));
    if (!ClientConfigDefinition.saveTofolder(MCSUtils.getMCSConfigInitFolderName(mcsFolderName), serverName))
      System.exit(1);

    // Saving new 'user' configuration
    logger.info(String.format("Saving new 'user' configuration to folder(%s)...", MCSUtils.getMCSConfigUserFolderName(mcsFolderName)));
    if (!userConfigDefinition.saveTofolder(MCSUtils.getMCSConfigUserFolderName(mcsFolderName), serverName))
      System.exit(1);

  }

  private static void joinConfigurations(String mcsFolderName, Logger logger) {
    File file = new File(mcsFolderName);
    if (!file.exists() || !file.isDirectory()) {
      logger.error(String.format("Directory (%s) doesn't exist!", file.getName()));
      //noinspection CallToSystemExit
      System.exit(1);
    }
    String confDirName = MCSUtils.getMCSConfigFolderName(mcsFolderName);
    if (!ConfigurationJoiner.checkSplitted(confDirName)) {
      logger.info("Configuration already joined nothing to do.");
      return;
    }
    logger.info("Start joining client config");

    String backup = MCSUtils.getMCSBackupFolderName(mcsFolderName);
    int i = 0;
    while (new File(backup + "." + i).exists()) {
      i++;
    }
    backup = backup + "." + i;
    if (!backupCurrentData(mcsFolderName)) System.exit(1);
    try {
      logger.info(String.format("Saving new 'init' configuration to folder (%s)...", MCSUtils.getMCSConfigInitFolderName(mcsFolderName)));
      ConfigurationJoiner.join(backup, confDirName);
    } catch (FileNotFoundException e) {
      logger.error("unable to join configurations");
      System.exit(1);
    }
  }

  private static boolean checkProgramOptions(ServerDefinition serverDefinition,
                                             ChannelDefinition channelDefinition,
                                             ConnectDefinition connectDefinition) {
    logger.info("Check program options files");

    // Check Server
    if (!checkProgramOptionsParameters(false, serverDefinition)) return ( false );

    // Check Server Channels
    ArrayList<ServerChannelDefinition> serverChannelDefinitions = serverDefinition.getChannels();
    for (ServerChannelDefinition serverChannelDefinition : serverChannelDefinitions) {
      if (!checkProgramOptionsParameters(false, serverChannelDefinition)) return ( false );
    }

    // Check Channel
    if (!checkProgramOptionsParameters(false, channelDefinition)) return ( false );

    // Check channel modules
    ArrayList<ModuleDefinition> moduleDefinitions = channelDefinition.getModuleDefinitionArrayList();
    for (ModuleDefinition moduleDefinition : moduleDefinitions) {
      if (!checkProgramOptionsParameters(moduleDefinition.isUser, moduleDefinition)) return ( false );
    }

    // Check Connect
    if (!checkProgramOptionsParameters(false, connectDefinition)) return ( false );

    return ( true );
  }

  private static boolean checkProgramOptionsParameters(boolean isUser, IParameterStorage parameterStorage) {
    ArrayList<Parameter> parameterList = parameterStorage.getParameterArrayList();
    HashSet<String> parameterNameList = new HashSet<String>(parameterList.size());
    for (Parameter parameter : parameterList) {

      // Check for duplicated parameter name
      if (parameterNameList.contains(parameter.paramName)) {
        logger.error(String.format("Duplicated parameter(%s) in module definition(%s).Error in program options file",
          parameter.paramName, parameterStorage.getDescription()));
        return ( false );

      }
      parameterNameList.add(parameter.paramName);

      // Skip Obsolete Parameter
      if (parameter.isObsolete) continue;

      if (( !isUser ) && ( !parameter.isUser )) {
        logger.error(String.format("Non user parameter(%s) in non user module definition(%s).Error in program options file",
          parameter.paramName, parameterStorage.getDescription()));
        return ( false );
      }

      if (( isUser ) && ( parameter.isUser )) {
        logger.error(String.format("User parameter(%s) in user module definition(%s).Error in program options file",
          parameter.paramName, parameterStorage.getDescription()));
        return ( false );
      }
    }
    return ( true );
  }

  public static boolean backupCurrentData(String mcsFolderName) {
    String backupFolderName = FileUtils.getFirstUnexistFolderWithNumericPrefix(MCSUtils.getMCSBackupFolderName(mcsFolderName));
    logger.info(String.format("Backup old client data to folder(%s)", backupFolderName));

    File backupFolder = new File(backupFolderName);
    if (!backupFolder.mkdirs()) {
      logger.error(String.format("Failed to create backup folder(%s)", backupFolder));
      return ( false );
    }

    File initFolder = new File(MCSUtils.getMCSConfigInitFolderName(mcsFolderName));
    File initBackupFolder = new File(backupFolder + File.separator + "init");
    if (!initFolder.renameTo(initBackupFolder)) {
      logger.error(String.format("Failed to backup folder (%s) to (%s)", initFolder.getPath(), initBackupFolder.getPath()));
      return ( false );
    }

    File userFolder = new File(MCSUtils.getMCSConfigUserFolderName(mcsFolderName));
    File userBackupFolder = new File(backupFolder + File.separator + "user");
    if (userFolder.exists() && !userFolder.renameTo(userBackupFolder)) {
      logger.error(String.format("Failed to backup folder (%s) to (%s)", userFolder.getPath(), userBackupFolder.getPath()));
      return ( false );
    }


    return ( true );
  }


}
