package org.octopusden.octopus.multichannelserver.config.joiner.xmlutils;


import org.octopusden.octopus.multichannelserver.XMLConfJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XMLUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(XMLConfJoiner.class);

  public static boolean compareNodeAttributes(Node node1, Node node2) {

    NamedNodeMap originalMap = node1.getAttributes();
    NamedNodeMap map = node2.getAttributes();
    int n = originalMap.getLength();
    int m = map.getLength();
    for (int i = 0; i < n; i++) {
      String name = originalMap.item(i).getNodeName();
      String value = originalMap.item(i).getNodeValue();
      Node attribute = map.getNamedItem(name);
      if (attribute == null || !value.equals(attribute.getNodeValue()))
        return false;
    }
    for (int i = 0; i < m; i++) {
      String name = map.item(i).getNodeName();
      String value = map.item(i).getNodeValue();
      Node attribute = originalMap.getNamedItem(name);
      if (attribute == null || !value.equals(attribute.getNodeValue()))
        return false;
    }

    return true;
  }

  public static Node findNodeCopy(Node node, NodeList list) {
    String name = node.getNodeName();
    int n = list.getLength();
    for (int i = 0; i < n; i++) {
      Node temp = list.item(i);
      if (name.equals(temp.getNodeName()) && compareNodeAttributes(node, temp))
        return temp;
    }
    return null;
  }

  public static Node findNodeCopyByArgument(Node node, NodeList list, String nodeName, String argument) {
    if (list == null)
      return null;
    String value = node.getAttributes().getNamedItem(argument).getNodeValue();
    String name = nodeName;
    int n = list.getLength();
    for (int i = 0; i < n; i++) {
      Node temp = list.item(i);
      if (name.equals(temp.getNodeName()) && value.equals(temp.getAttributes().getNamedItem(argument).getNodeValue()))
        return temp;
    }
    return null;
  }

  public static boolean compareXMLFiles(String filename1, String filename2) throws Exception {
    return compareXMLFiles(new File(filename1), new File(filename2));
  }

  public static boolean compareXMLFiles(File file1, File file2) throws Exception {
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document dom1 = db.parse(file1);
    Document dom2 = db.parse(file2);
    return compareNodes(dom1.getChildNodes().item(0), dom2.getChildNodes().item(0));
  }

  public static boolean compareNodes(Node node1, Node node2) {


    if (LOGGER.isDebugEnabled())
      LOGGER.debug("comparing " + node1.getNodeName() + "(" + printAttributes(node1) + ")" + " and " + node1.getNodeName() + "(" + printAttributes(node2) + ")");
    if (!node1.getNodeName().equals(node1.getNodeName())) {
      LOGGER.warn("names doesn't match");
      return false;
    }
    if (!compareNodeAttributes(node1, node2)) {
      LOGGER.warn("attributes doesn't match");
      return false;
    }
    NodeList list1 = node1.getChildNodes();
    NodeList list2 = node2.getChildNodes();
    int n = list1.getLength();
    LOGGER.debug("comparing children of one with two");
    for (int i = 0; i < n; i++) {
      Node temp1 = list1.item(i);
      if (temp1.getNodeType() != Node.ELEMENT_NODE)
        continue;
      Node temp2 = findNodeCopy(temp1, list2);
      if (temp2 == null) {
        LOGGER.warn("no match for child of 1 " + temp1.getNodeName() + " " + printAttributes(temp1));
        return false;
      }
      if (( temp1.getChildNodes().getLength() != 0 || temp2.getChildNodes().getLength() != 0 ) &&
        !compareNodes(temp1, temp2))
        return false;
    }
    n = list2.getLength();
    LOGGER.debug("comparing children of two with one");
    for (int i = 0; i < n; i++) {
      Node temp2 = list2.item(i);
      if (temp2.getNodeType() != Node.ELEMENT_NODE)
        continue;
      Node temp1 = findNodeCopy(temp2, list1);
      if (temp1 == null) {
        LOGGER.warn("no match for child of 2 " + temp2.getNodeName() + " " + printAttributes(temp2));
        return false;
      }
      if (( temp1.getChildNodes().getLength() != 0 || temp2.getChildNodes().getLength() != 0 ) &&
        !compareNodes(temp1, temp2))
        return false;
    }
    return true;
  }

  private static String printAttributes(Node node) {
    StringBuilder builder = new StringBuilder();
    NamedNodeMap map = node.getAttributes();
    int n = map.getLength();
    for (int i = 0; i < n; i++) {
      builder.append(map.item(i).getNodeName());
      builder.append("=\"");
      builder.append(map.item(i).getNodeValue());
      builder.append("\" ");
    }
    return builder.toString();

  }
}
