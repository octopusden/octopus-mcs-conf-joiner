package org.octopusden.octopus.multichannelserver.config;

import org.octopusden.octopus.fileutils.FileUtils;
import org.octopusden.octopus.multichannelserver.XMLConfJoiner;
import org.octopusden.octopus.multichannelserver.MCSUtils;
import org.octopusden.octopus.stringutils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Date: 28.08.2009
 */
public class ConfigDefinition {
  private static final Logger logger = LoggerFactory.getLogger(XMLConfJoiner.class);
  public boolean isLoaded = false;

  private ServerDefinition serverDefinition;
  private HashMap<String, ChannelDefinition> channelDefinitionHashMap = new HashMap<String, ChannelDefinition>(MCSUtils.CONST_MAX_CHANNEL_COUNT);
  private ArrayList<ChannelDefinition> channelDefinitions = new ArrayList<ChannelDefinition>(MCSUtils.CONST_MAX_CHANNEL_COUNT);
  private ArrayList<ConnectDefinition> connectDefinitions = new ArrayList<ConnectDefinition>(5);


  public ConfigDefinition() {
    serverDefinition = new ServerDefinition();
  }

  public ConfigDefinition(String mcsFolderName) throws Exception {
    // Load Server.XML
    String serverXmlFileName = MCSUtils.getMCSConfigInitFolderName(mcsFolderName) + File.separator + MCSUtils.CONST_FILENAME_SERVER_XML;
    serverDefinition = new ServerDefinition(serverXmlFileName);
    if (!serverDefinition.isLoaded) return;
    if (logger.isDebugEnabled())
      logger.debug(String.format("Server '%s' loaded from file(%s)", serverDefinition.getDescription(), serverXmlFileName));


    // Read Channels
    ArrayList<ServerChannelDefinition> serverChannelDefinitions = serverDefinition.getChannels();

    if (logger.isDebugEnabled())
      logger.debug(String.format("Found %d channels", serverChannelDefinitions.size()));

    for (ServerChannelDefinition serverChannelDefinition : serverChannelDefinitions) {
      ChannelDefinition channelDefinition = new ChannelDefinition(mcsFolderName, serverChannelDefinition);

      if (!channelDefinition.isLoaded) {
        logger.error(String.format("Failed to load Channel %s", serverChannelDefinition.channelName));
        return;
      }

      addChannel(serverChannelDefinition.channelName, channelDefinition);
      if (logger.isDebugEnabled())
        logger.debug(String.format("Channel '%s' loaded from file(%s)", serverChannelDefinition.channelName, channelDefinition.fileName));

    }

    // Read connect.xml
    if (!loadConnectFiles(mcsFolderName)) return;

    isLoaded = true;
  }


  public boolean saveTofolder(String folderName, String serverName) {

    String serverXmlFileName = folderName + File.separator + MCSUtils.CONST_FILENAME_SERVER_XML;

    if (!FileUtils.makeRecursiveFolders(folderName)) {
      logger.error("Failed to create folder:" + folderName);
      return ( false );
    }

    // Save Server.XML
    if (!serverDefinition.saveToFile(serverXmlFileName, serverName)) return ( false );

    // Save Channels files
    for (ChannelDefinition channelDefinition : channelDefinitions)
      if (!channelDefinition.saveToFile(folderName)) return ( false );

    // Save Connects
    for (ConnectDefinition connectDefinition : connectDefinitions)
      if (!connectDefinition.saveToFile(folderName)) return ( false );


    return ( true );
  }

  public ArrayList<ChannelDefinition> getChannelDefinitionArrayList() {
    return new ArrayList<ChannelDefinition>(channelDefinitions);
  }

  public void clear() {
    isLoaded = false;
    serverDefinition.clear();
    channelDefinitions.clear();
    channelDefinitionHashMap.clear();
    connectDefinitions.clear();
  }

  public final void addChannel(String channelName, ChannelDefinition channelDefinition) {
    channelDefinitions.add(channelDefinition);
    channelDefinitionHashMap.put(channelName, channelDefinition);
  }

  public int getChannelCount() {
    return channelDefinitionHashMap.size();
  }

  public void setServerDefinition(ServerDefinition userServerDefinition) {
    serverDefinition = userServerDefinition;
  }

  public ServerDefinition getServerDefinition() {
    return serverDefinition;
  }

  public boolean isEmpty() {
    return ( serverDefinition.isEmpty() && channelDefinitions.isEmpty() && connectDefinitions.isEmpty() );
  }


  private boolean loadConnectFiles(String mcsFolderName) {
    if (logger.isDebugEnabled())
      logger.debug("  Looking for connect files in configuration");

    connectDefinitions.clear();

    HashSet<String> connectFileNames = new HashSet<String>(3);

    // Iterate Channels
    for (ChannelDefinition channelDefinition : channelDefinitions) {
      ModuleDefinition dbAccessModule = channelDefinition.getModuleByType(XMLConst.CONST_XML_MODULE_TYPE_DBACCESS);
      if (dbAccessModule != null) {
        String connectFileName = dbAccessModule.getParameterValue(XMLConst.CONST_XML_PARAM_INCLUDE);
        if (!StringUtils.isEmptyOrNull(connectFileName))
          connectFileNames.add(connectFileName);
      }
    }

    if (logger.isTraceEnabled())
      logger.trace(String.format("  Found %d connect files", connectFileNames.size()));

    for (String connectFileName : connectFileNames) {
      if (logger.isTraceEnabled())
        logger.trace(String.format("  Loading connect file(%s)", connectFileName));

      ConnectDefinition connectDefinition = new ConnectDefinition(mcsFolderName, connectFileName);
      if (!connectDefinition.isLoaded) return ( false );
      connectDefinitions.add(connectDefinition);
    }

    if (logger.isDebugEnabled())
      logger.debug(String.format("  Loaded %d connect files", connectDefinitions.size()));

    return true;
  }

  public ArrayList<ConnectDefinition> getConnectDefinitions() {
    return new ArrayList<ConnectDefinition>(connectDefinitions);
  }

  public void addConnect(ConnectDefinition connectDefinition) {
    connectDefinitions.add(connectDefinition);
  }
}
