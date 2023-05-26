package org.octopusden.octopus.multichannelserver.config;

import org.octopusden.octopus.fileutils.FileUtils;
import org.octopusden.octopus.multichannelserver.XMLConfJoiner;
import org.octopusden.octopus.util.xml.XmlParser;
import org.octopusden.octopus.util.xml.XmlStringBuffer;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Date: 02.09.2009
 */
public class ConnectDefinition extends AbstractParameterStorage {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(XMLConfJoiner.class);
  public boolean isLoaded = false;
  public String fileName;

  public ConnectDefinition(String mcsFolderName, String connectFileName) {
    this.fileName = connectFileName.toLowerCase();
    loadFromFile(mcsFolderName + File.separator + connectFileName);
  }

  public ConnectDefinition() {

  }

  private void loadFromFile(String connectFileName) {
    Document document;
    try {
      document = XmlParser.parse(connectFileName);
    } catch (Exception e) {
      logger.error(String.format("Failed to parse file(%s)! Exception(%s)", connectFileName, e.getMessage()));
      return;
    }

    parameterList = new ParameterList(document.getDocumentElement());
    isLoaded = true;
  }

  public ConnectDefinition(String connectFileName) {
    this.fileName = connectFileName;
    loadFromFile(connectFileName);
  }


  @Override
  public String getDescription() {
    return "CONNECTION";
  }

  public boolean saveToFile(String folderName) {
    String connectFileName = folderName + File.separator + FileUtils.getFileName(fileName);

    if (logger.isDebugEnabled())
      logger.debug(String.format("Saving connect to file(%s)", connectFileName));

    // Serialize
    XmlStringBuffer buffer = new XmlStringBuffer(2000);

    // Add Header
    buffer.addStartElement(XMLConst.CONST_XML_PARAM_INCLUDE).addCR();

    // Add common connect Parameters
    parameterList.serialize(1, buffer);

    // Finish channel
    buffer.addFinishElement(XMLConst.CONST_XML_PARAM_INCLUDE);

    // Save to File
    FileOutputStream fileOutputStream;
    try {
      fileOutputStream = new FileOutputStream(connectFileName);
      fileOutputStream.write(buffer.toString().getBytes());
      fileOutputStream.close();
    } catch (Exception e) {
      logger.error(String.format("Exception during saving file(%s). Exception(%s)", connectFileName, e));
      return ( false );
    }

    return true;  //To change body of created methods use File | Settings | File Templates.
  }
}
