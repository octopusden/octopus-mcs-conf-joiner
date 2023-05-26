package org.octopusden.octopus.util.xml;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class XmlParser {
  public static final String ENTITY_AMP = "&amp;";
  public static final String ENTITY_LF = "&#xA;";
  public static final String ENTITY_CR = "&#xD;";
  public static final String ENTITY_GT = "&gt;";
  public static final String ENTITY_LT = "&lt;";
  public static final String ENTITY_QUOT = "&quot;";
  public static final String ENTITY_APOS = "&apos;";

  private static final int[] XML_CHARS = new int[ 0x9f + 1 ];
  public static final String[] STANDARD_ENTITIES = new String[]{
    null, ENTITY_AMP, ENTITY_LF, ENTITY_CR, ENTITY_GT, ENTITY_LT, ENTITY_QUOT, ENTITY_APOS
  };

  private static DocumentBuilder builder;

  static {

    for (int i = 0x20; i < 0x7f; i++) {
      XML_CHARS[ i ] = -i;
    }

    XML_CHARS[ 0x09 ] = -0x09;
    XML_CHARS[ 0x0a ] = -0x0a;
    XML_CHARS[ 0x0d ] = -0x0d;
    XML_CHARS[ 0x85 ] = -0x85;
  }


  public static final String safeGetAttribute(Node e, String attrName) {
    NamedNodeMap attr = e.getAttributes();
    Node node = attr.getNamedItem(attrName);
    return ( node != null ? node.getNodeValue() : null );
  }

  /**
   * Retrieves the first child element that matches the tag and whose value of
   * the attrName attribute is attrValue.
   *
   * @param parent    The parent node.
   * @param nodeName  The tag.
   * @param attrName  The name of the attribute.
   * @param attrValue The value of the attribute.
   * @return The first child Element that matches the tag and whose value of the
   *         attrName attribute is attrValue. If the appropriate element is not found null
   *         is returned.
   */
  public static final Element safeGetFirstChild(Node parent, String nodeName, String attrName, String attrValue) {
    NodeList nl = parent.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n instanceof Element && n.getNodeName().equals(nodeName)) {
        Element e = (Element) n;
        if (e.getAttribute(attrName).equals(attrValue)) {
          return e;
        }
      }
    }
    return null;
  }

  /**
   * Creates an ArrayList instance and then adds the children of the parent node
   * to it. The child is added if it is an Element, the tag is a non-null value
   * and the tag equals to the child's node name.
   *
   * @param parent The parent node.
   * @param tag    The tag.
   * @return The ArrayList object containing the nodes that fit the tag.
   * @see org.octopusden.octopus.util.xml.XmlParser#addByTagName(java.util.Collection, org.w3c.dom.Node, java.lang.String)
   */
  public static final ArrayList<Element> getByTagName(Node parent, String tag) {
    @SuppressWarnings({ "CollectionWithoutInitialCapacity" })
    ArrayList<Element> ret = new ArrayList<Element>();
    addByTagName(ret, parent, tag);
    return ret;
  }

  /**
   * Adds the children of the parent node to the collection. The child is added
   * if it is an Element, the tag is a non-null value and the tag equals to the
   * child's node name.
   *
   * @param coll   The Collection that the nodes will be added to.
   * @param parent The parent node.
   * @param tag    The tag.
   * @return The coll Collection.
   * @see org.octopusden.octopus.util.xml.XmlParser#getByTagName(org.w3c.dom.Node, java.lang.String)
   */
  public static final Collection<Element> addByTagName(Collection<Element> coll, Node parent, String tag) {
    if (parent == null) {
      return coll;
    }
    NodeList nl = parent.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n instanceof Element
        && ( ( tag == null ) || n.getNodeName().equals(tag) )) {
        if (coll == null) {
          //noinspection AssignmentToMethodParameter
          coll = new ArrayList<Element>(10);
        }
        coll.add((Element) n);
      }
    }
    return coll;
  }

  public static Document parse(String fileName) throws ParserConfigurationException, IOException, SAXException {
    if (builder == null) {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      builder = documentBuilderFactory.newDocumentBuilder();
    }
    return ( builder.parse(new File(fileName)) );
  }

  public static StringBuffer appendEscaped(String value, StringBuffer result) {
    if (value == null) {
      return null;
    }
    final char[] ch = value.toCharArray();
    final int limit = ch.length;
    int offset = 0;
    for (int i = 0; i < limit; i++) {
      char c = ch[ i ];
      int code = 0;
      if (c >= XML_CHARS.length) {
        if (c <= 0xD7FF || ( c >= 0xE000 && c <= 0xFFFD ) || ( c >= 0x10000 && c <= 0x10FFFF )) {
          continue;
        }
      } else {
        code = XML_CHARS[ c ];
        if (code < 0) { // ok
          continue;
        }
      }
      if (result == null) {
        //noinspection AssignmentToMethodParameter
        result = new StringBuffer(value.length() + 2);
      }
      result.append(ch, offset, i - offset);
      offset = i + 1;
      if (code == 0) { // restricted
        result.append(' ');
      } else {
        result.append(STANDARD_ENTITIES[ code ]);
      }
    }
    if (result == null) {
      return null;
    }
    if (offset < limit) {
      result.append(ch, offset, limit - offset);
    }
    return result;
  }


  public static final Element safeGetFirstChild(Node parent, String tag) {
    NodeList nl = parent.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n instanceof Element && ( ( tag == null ) || n.getNodeName().equals(tag) )) {
        return (Element) n;
      }
    }
    return null;
  }


}
