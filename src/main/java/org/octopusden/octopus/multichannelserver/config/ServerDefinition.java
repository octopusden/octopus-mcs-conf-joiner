package org.octopusden.octopus.multichannelserver.config;

import org.octopusden.octopus.multichannelserver.XMLConfJoiner;
import org.octopusden.octopus.multichannelserver.MCSUtils;
import org.octopusden.octopus.stringutils.StringUtils;
import org.octopusden.octopus.util.xml.XmlParser;
import org.octopusden.octopus.util.xml.XmlStringBuffer;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileOutputStream;
import java.util.ArrayList;

import static org.octopusden.octopus.multichannelserver.config.XMLConst.*;

/**
 * Date: 01.09.2009
 */
public class ServerDefinition extends AbstractParameterStorage {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(XMLConfJoiner.class);

  public boolean isLoaded = false;

  // Server channels
  private ArrayList<ServerChannelDefinition> serverChannelDefinitions = new ArrayList<ServerChannelDefinition>(MCSUtils.CONST_MAX_CHANNEL_COUNT);

  public ServerDefinition(String serverXmlFileName) {
    super();

    Document document;
    try {
      document = XmlParser.parse(serverXmlFileName);
    } catch (Exception e) {
      logger.error(String.format("Failed to parse file (%s)! Exception(%s)", serverXmlFileName, e.getMessage()));
      return;
    }

    // Read Server
    Element serverElement = XmlParser.safeGetFirstChild(document.getDocumentElement(), XMLConst.CONST_XML_PARAM_SERVER);
    if (serverElement == null) {
      logger.error("Didnt find SERVER section in server.xml");
      return;
    }

    // Read Server Parameters
    String serverID = XmlParser.safeGetAttribute(serverElement, XMLConst.CONST_XML_ATTR_ID);
    parameterList = new ParameterList(serverElement);
    // Check if parameter SERVER_ID is already present
    if (!StringUtils.isEmptyOrNull(serverID)) {
      if (isParameterPresent(CONST_XML_PARAM_SERVER_ID))
        logger.warn(String.format("Parameter (%s) with value (%s) is found in SERVER section. Ignore obsolete attribute ID with value(%s)",
          CONST_XML_PARAM_SERVER_ID, getParameterValue(CONST_XML_PARAM_SERVER_ID), serverID));
        // Put Server Id from XMl attribute to User Param
      else {
        addParameter(0, new Parameter(false, CONST_XML_PARAM_SERVER_ID, serverID, null));
        logger.info(String.format("Server attribute (ID) moved to Server Parameter (%s)", CONST_XML_PARAM_SERVER_ID));
      }
    }


    // Read Channels
    ArrayList<Element> channelList = XmlParser.getByTagName(document.getDocumentElement(), CONST_XML_PARAM_CHANNEL);

    if (logger.isDebugEnabled())
      logger.debug(String.format("Found %d channels", channelList.size()));

    for (Element node : channelList) {
      ParameterList channelParameterList = new ParameterList(node);
      String channelName = XmlParser.safeGetAttribute(node, CONST_XML_ATTR_NAME);
      addChannel(new ServerChannelDefinition(channelName, channelParameterList));
    }

    isLoaded = true;
  }

  public ServerDefinition() {
    super();
  }

  public final void addChannel(ServerChannelDefinition serverChannelDefinition) {
    serverChannelDefinitions.add(serverChannelDefinition);
  }


  public ArrayList<ServerChannelDefinition> getChannels() {
    return ( new ArrayList<ServerChannelDefinition>(serverChannelDefinitions) );

  }

  @Override
  public String getDescription() {
    return "SERVER.XML:" + getParameterValue(CONST_XML_PARAM_SERVER_ID);
  }

  public boolean saveToFile(String serverXmlFileName, String serverName) {

    if (isEmpty()) return ( true );

    if (serverName == null || serverName.isEmpty() ) {
      serverName = XMLConst.CONST_XML_PARAM_MULTICHANNELSERVER;
    }

    if (logger.isDebugEnabled())
      logger.debug("Save server.xml file:" + serverXmlFileName);

    // Serialize Server XML
    XmlStringBuffer buffer = new XmlStringBuffer(16000);
    buffer.addStartElement(serverName).addCR();

    // Add Server Parameters Part
    if (( parameterList != null ) && ( !parameterList.isEmpty() )) {
      buffer.addStartElement(1, XMLConst.CONST_XML_PARAM_SERVER).addCR();
      parameterList.serialize(2, buffer);
      buffer.addFinishElement(1, XMLConst.CONST_XML_PARAM_SERVER).addCR();
    }

    // Add Server Channels Part
    for (ServerChannelDefinition serverChannelDefinition : serverChannelDefinitions)
      serverChannelDefinition.serialize(buffer);

    buffer.addFinishElement(serverName);

    // Save Server XML
    FileOutputStream fileOutputStream;
    try {
      fileOutputStream = new FileOutputStream(serverXmlFileName);
      fileOutputStream.write(buffer.toString().getBytes());
      fileOutputStream.close();
    } catch (Exception e) {
      logger.error(String.format("Exception during saving file(%s). Exception(%s)", serverXmlFileName, e));
      return ( false );
    }

    return ( true );
  }

  public boolean isEmpty() {
    return ( serverChannelDefinitions.isEmpty() && parameterList.isEmpty() );
  }

  public void clear() {
    isLoaded = false;
    serverChannelDefinitions.clear();
    super.clear();
  }
}
