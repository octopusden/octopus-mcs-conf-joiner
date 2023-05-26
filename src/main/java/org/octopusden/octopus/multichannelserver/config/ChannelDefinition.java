package org.octopusden.octopus.multichannelserver.config;

import org.octopusden.octopus.fileutils.FileUtils;
import org.octopusden.octopus.multichannelserver.XMLConfJoiner;
import org.octopusden.octopus.util.xml.XmlParser;
import org.octopusden.octopus.util.xml.XmlStringBuffer;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.octopusden.octopus.multichannelserver.config.XMLConst.*;

/**
 * Date: 28.08.2009
 */
public class ChannelDefinition extends AbstractParameterStorage {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(XMLConfJoiner.class);
  public boolean isLoaded = false;
  public String fileName;
  public String channelName;

  private ArrayList<ModuleDefinition> moduleDefinitions = new ArrayList<ModuleDefinition>(10);
  private HashMap<String, ModuleDefinition> moduleDefinitionsByType = new HashMap<String, ModuleDefinition>(10);
  private HashMap<String, ModuleDefinition> moduleDefinitionsByName = new HashMap<String, ModuleDefinition>(10);

  public ChannelDefinition(String channelConfigFileName) throws Exception {
    loadFromFile(channelConfigFileName);
  }

  public ChannelDefinition(String mcsFolderName, ServerChannelDefinition serverChannelDefinition) throws Exception {
    String channelConfigFileName = mcsFolderName + File.separator + serverChannelDefinition.getParameterValue(CONST_XML_ATTR_CFILE);
    loadFromFile(channelConfigFileName);
  }

  protected final void loadFromFile(String channelConfigFileName) throws Exception {
    isLoaded = false;

    Document document;
    try {
      document = XmlParser.parse(channelConfigFileName);
    } catch (Exception e) {
      logger.error(String.format("Failed to parse file(%s)! Exception(%s)", channelConfigFileName, e.getMessage()));
      return;
    }

    loadFromChannelNode(channelConfigFileName, document.getDocumentElement());
    isLoaded = true;
  }


  public ChannelDefinition(String fileName, String channelName) {
    this.fileName = fileName;
    this.channelName = channelName;
    parameterList = new ParameterList();
  }

  public final void loadFromChannelNode(String fileName, Node channelNode) throws Exception {
    // Channel Parameters
    parameterList = new ParameterList(channelNode);
    this.fileName = fileName.toLowerCase();
    this.channelName = XmlParser.safeGetAttribute(channelNode, CONST_XML_ATTR_NAME);

    // Load Module Definitions
    ArrayList<Element> modulesElementList = XmlParser.getByTagName(channelNode, CONST_XML_PARAM_MODULE);
    // Load USER Module Definitions
    modulesElementList.addAll(XmlParser.getByTagName(channelNode, CONST_XML_PARAM_USER_MODULE));

    // Load all module defintions
    for (Element element : modulesElementList)
      addModuleDefinition(new ModuleDefinition(element));

    // Resolve included Modules
    for (ModuleDefinition moduleDefinition : moduleDefinitions)
      moduleDefinition.resolveIncludedModules(moduleDefinitionsByName);
  }

  public ArrayList<ModuleDefinition> getModuleDefinitionArrayList() {
    return new ArrayList<ModuleDefinition>(moduleDefinitions);
  }

  public void addModuleDefinition(ModuleDefinition moduleDefinition) {
    moduleDefinitions.add(moduleDefinition);
    moduleDefinitionsByType.put(moduleDefinition.type, moduleDefinition);
    moduleDefinitionsByName.put(moduleDefinition.moduleName, moduleDefinition);
  }

  public int getModuleCount() {
    return moduleDefinitions.size();  //To change body of created methods use File | Settings | File Templates.
  }

  public boolean saveToFile(String folderName) {

    String channelFileName = folderName + File.separator + FileUtils.getFileName(fileName);

    if (logger.isDebugEnabled())
      logger.debug(String.format("Saving channel '%s' to file(%s)", channelName, channelFileName));

    // Serialize
    XmlStringBuffer buffer = new XmlStringBuffer(16000);

    // Add Header
    buffer.addStartElement(XMLConst.CONST_XML_PARAM_CHANNEL, false);
    buffer.addAttribute(CONST_XML_ATTR_NAME, channelName).closeAngleBrace().addCR();

    // Add common Channel Parameters
    parameterList.serialize(1, buffer);

    // Add Modules
    for (ModuleDefinition moduleDefinition : moduleDefinitions)
      moduleDefinition.serialize(1, buffer);

    // Finish channel
    buffer.addFinishElement(XMLConst.CONST_XML_PARAM_CHANNEL);


    // Save to File
    FileOutputStream fileOutputStream;
    try {
      fileOutputStream = new FileOutputStream(channelFileName);
      fileOutputStream.write(buffer.toString().getBytes());
      fileOutputStream.close();
    } catch (Exception e) {
      logger.error(String.format("Exception during saving file(%s). Exception(%s)", channelFileName, e));
      return ( false );
    }

    return true;  //To change body of created methods use File | Settings | File Templates.
  }


  @Override
  public String getDescription() {
    return "CHANNEL:" + channelName;
  }

  @Override
  public boolean isEmpty() {
    return ( super.isEmpty() && moduleDefinitions.isEmpty() );    //To change body of overridden methods use File | Settings | File Templates.
  }

  public ModuleDefinition getModuleByType(String moduleType) {
    return moduleDefinitionsByType.get(moduleType);
  }
}
