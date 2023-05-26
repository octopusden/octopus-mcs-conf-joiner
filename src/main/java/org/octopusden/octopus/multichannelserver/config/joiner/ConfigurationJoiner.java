package org.octopusden.octopus.multichannelserver.config.joiner;


import org.octopusden.octopus.fileutils.FileUtils;
import org.octopusden.octopus.multichannelserver.XMLConfJoiner;
import org.octopusden.octopus.multichannelserver.config.joiner.xmlutils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Set;
import java.util.TreeSet;

public class ConfigurationJoiner {
  private static final Logger LOGGER = LoggerFactory.getLogger(XMLConfJoiner.class);

  private final static String INIT = "init";
  private final static String USER = "user";
  private final static String NAME = "NAME";
  private final static String TYPE = "TYPE";
  private final static String USER_PARAMETER = "USER_PARAMETER";
  private final static String USER_MODULE = "USER_MODULE";
  private final static String MODULE = "MODULE";
  private final static String PARAMETER = "PARAMETER";

  private static DocumentBuilder ourDocumentBuilder;
  private static Transformer ourTransformer;

  static {
    try {
      ourDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute("indent-number", 2);
      ourTransformer = transformerFactory.newTransformer();
      ourTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void join(String src, String dest) throws FileNotFoundException {
    File init = new File(src + File.separator + INIT);
    File user = new File(src + File.separator + USER);
    if (!checkSplitted(src)) {
      throw new FileNotFoundException("unable to find directories" + new File(src).getAbsolutePath());
    }
    //creating necessary directories
    File destFolder = new File(dest);
    if (!destFolder.exists())
      if (!destFolder.mkdirs()) {
        LOGGER.debug(String.format("unable to create directory %s", dest));
        return;
      }
    File initJoined = new File(dest + File.separator + INIT);
    if (!initJoined.exists()) {
      if (!initJoined.mkdirs()) {
        LOGGER.debug(String.format("unable to create directory %s", dest + File.separator + INIT));
        return;
      }
    } else {
      LOGGER.debug(String.format("clearing %s", dest + File.separator + INIT));

      if (!FileUtils.deleteDir(initJoined)) {
        LOGGER.error(String.format("unable to delete directory %s", dest + File.separator + INIT));
        return;
      }
      if (!initJoined.mkdirs()) {
        LOGGER.error(String.format("unable to create directory %s", dest + File.separator + INIT));
        return;
      }
    }

    Set<String> xmlNames = getXMLFileNames(init, user);
    for (String name : xmlNames) {
      try {
        joinXML(src + File.separator + INIT + File.separator + name, src + File.separator + USER + File.separator + name, dest + File.separator + INIT + File.separator + name);
      } catch (Exception e) {
        LOGGER.debug(String.format("Unable to join %s. Exception: %s Message: %s. Skipped!", name, e.getClass().getName(), e.getMessage()));
        return;

      }
    }
  }

  private static Set<String> getXMLFileNames(File init, File user) {
    Set<String> result = new TreeSet<String>();
    String[] initList = init.list();
    for (String name : initList) {
      result.add(name);
    }
    String[] userList = user.list();
    for (String name : userList) {
      result.add(name);
    }
    return result;
  }


  public static boolean checkSplitted(String src) {
    File init = new File(src + File.separator + INIT);
    File user = new File(src + File.separator + USER);
    return ( init.isDirectory() && init.exists() && user.isDirectory() && user.exists() );
  }

  private static void joinXML(String init, String user, String dest) throws IOException, SAXException, TransformerException {
    File initFile = new File(init);
    File userFile = new File(user);
    LOGGER.debug(String.format("Joining %s", init));
    if (initFile.isDirectory() || userFile.isDirectory()) {
      LOGGER.warn("No subdirectories in conf ");
      return;
    }
    File destFile = new File(dest);
    if (!initFile.exists()) {
      copyXML(userFile, destFile);
      return;
    }
    if (!userFile.exists()) {
      copyXML(initFile, destFile);
      return;
    }
    joinXML(initFile, userFile, destFile);

  }

  private static void copyXML(File src, File dest) throws IOException, SAXException, TransformerException {
    Document document = ourDocumentBuilder.parse(src);
    ourTransformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream(dest)));
  }

  private static void joinXML(File init, File user, File dest) throws IOException, SAXException, TransformerException {
    Document docInit = ourDocumentBuilder.parse(init);
    Document docUser = ourDocumentBuilder.parse(user);
    Document document = ourDocumentBuilder.newDocument();
    document.appendChild(document.importNode(docInit.getChildNodes().item(0), true));
    Node root = document.getChildNodes().item(0);
    Node userRoot = docUser.getChildNodes().item(0);
    copyDoc(root, userRoot, document);

    ourTransformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(new FileOutputStream(dest))));
  }

  private static void copyDoc(Node root, Node userRoot, Document document) {
    NodeList originalList = root.getChildNodes();
    NodeList list = userRoot.getChildNodes();
    int n = originalList.getLength();
    for (int i = 0; i < n; i++) {
      Node node = originalList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if (USER_PARAMETER.equals(node.getNodeName())) {
          root.replaceChild(copyParameter(node, list, document), node);
          continue;
        }
        if (USER_MODULE.equals(node.getNodeName())) {
          root.replaceChild(copyModule(node, list, document), node);
          continue;
        }
        Node copy = XMLUtils.findNodeCopy(node, list);
        if (copy != null)
          copyDoc(node, copy, document);
      }
    }

  }

  private static Node copyModule(Node original, NodeList list, Document document) {
    Element element = document.createElement(MODULE);
    Node user_module = XMLUtils.findNodeCopyByArgument(original, list, MODULE, TYPE);
    NamedNodeMap map = original.getAttributes();
    int n = map.getLength();
    for (int i = 0; i < n; i++) {
      element.setAttribute(map.item(i).getNodeName(), map.item(i).getNodeValue());
    }
    //copy attributes from original
    if (user_module != null) {
      //copy attributes from user module
      map = user_module.getAttributes();
      n = map.getLength();
      for (int i = 0; i < n; i++) {
        element.setAttribute(map.item(i).getNodeName(), map.item(i).getNodeValue());
      }
    }
    NodeList childList = original.getChildNodes();
    NodeList userChildList = user_module != null ? user_module.getChildNodes() : null;
    Set<String> parameterNames = new TreeSet<String>();
    int m = childList.getLength();
    //filling with original parameters
    for (int i = 0; i < m; i++) {
      Node temp = childList.item(i);
      if (temp.getNodeType() == Node.ELEMENT_NODE && temp.getNodeName().equals(PARAMETER)) {
        //replace if this parameter exists in USER_MODULE
        element.appendChild(copyParameter(temp, userChildList, document));
        parameterNames.add(temp.getAttributes().getNamedItem(NAME).getNodeValue());

      } else {
        element.appendChild(document.importNode(temp, true));
      }
    }
    //filling with user parameters
    if (userChildList != null)
      m = userChildList.getLength();
    else
      m = 0;
    for (int i = 0; i < m; i++) {
      Node temp = userChildList.item(i);
      if (temp.getNodeType() == Node.ELEMENT_NODE && temp.getNodeName().equals(PARAMETER)
        && !parameterNames.contains(temp.getAttributes().getNamedItem(NAME).getNodeValue())) {
        element.appendChild(document.importNode(temp, true));
      }
    }
    return element;
  }

  private static Node copyParameter(Node node, NodeList list, Document document) {
    Element element = document.createElement(PARAMETER);
    Node user_parameter = XMLUtils.findNodeCopyByArgument(node, list, PARAMETER, NAME);
    NamedNodeMap map = node.getAttributes();
    int n = map.getLength();
    for (int i = 0; i < n; i++) {
      element.setAttribute(map.item(i).getNodeName(), map.item(i).getNodeValue());
    }
    //copy attributes from original
    if (user_parameter != null) {
      //copy attributes from user parameter
      map = user_parameter.getAttributes();
      n = map.getLength();
      for (int i = 0; i < n; i++) {
        element.setAttribute(map.item(i).getNodeName(), map.item(i).getNodeValue());
      }
    }


    return element;
  }


}

