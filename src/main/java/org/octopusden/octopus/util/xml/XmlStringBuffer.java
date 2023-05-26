package org.octopusden.octopus.util.xml;

/**
 * Date: 31.08.2009
 */
public class XmlStringBuffer {
    private StringBuffer stringBuffer;
    private String defaultNamespacePrefix;

    public XmlStringBuffer() {
        setStringBuffer(new StringBuffer(64));
    }

    public XmlStringBuffer(int initialBufferSize) {
        setStringBuffer(new StringBuffer(initialBufferSize));
    }

    public XmlStringBuffer(StringBuffer stringBuffer) {
        this.setStringBuffer(stringBuffer);
    }

    public XmlStringBuffer(XmlStringBuffer xmlStringBuffer) {
        this.setStringBuffer(xmlStringBuffer.stringBuffer);
    }

    public final XmlStringBuffer add(String string) {
        stringBuffer.append(string);
        return this;
    }

    public final XmlStringBuffer addCR() {
        stringBuffer.append("\n");
        return this;
    }

    public final XmlStringBuffer add(long l) {
        stringBuffer.append(l);
        return this;
    }

    public final XmlStringBuffer add(int l) {
        stringBuffer.append(l);
        return this;
    }

    public final XmlStringBuffer add(char c) {
        stringBuffer.append(c);
        return this;
    }

    public final XmlStringBuffer add(char c, int count) {
        for (int i = 0; i < count; i++)
            stringBuffer.append(c);
        return this;
    }


    public final XmlStringBuffer add(byte b) {
        stringBuffer.append(b);
        return this;
    }

    public final XmlStringBuffer add(float f) {
        stringBuffer.append(f);
        return this;
    }

    public final XmlStringBuffer add(double f) {
        stringBuffer.append(f);
        return this;
    }

    public final XmlStringBuffer add(boolean b) {
        stringBuffer.append(b);
        return this;
    }

    public final XmlStringBuffer addValue(String string) {
        XmlParser.appendEscaped(string, stringBuffer);
        return this;
    }

    public final XmlStringBuffer addStartElement(int level, String elementName, boolean closeAngleBrace) {
        StringBuffer stringBuffer = this.stringBuffer;
        if (level > 0) {
            add(' ', level * 2);
        }
        stringBuffer.append('<');
        if (defaultNamespacePrefix != null) {
            stringBuffer.append(defaultNamespacePrefix).append(':');
        }
        if (closeAngleBrace) {
            stringBuffer.append(elementName).append('>');
        } else {
            stringBuffer.append(elementName);
        }
        return this;
    }


    public final XmlStringBuffer addStartElement(String elementName, boolean closeAngleBrace) {
        return (addStartElement(0, elementName, closeAngleBrace));
    }


    public final XmlStringBuffer addStartElement(int level, String elementName) {
        return addStartElement(level, elementName, true);
    }

    public final XmlStringBuffer addStartElement(String elementName) {
        return addStartElement(elementName, true);
    }

    public final XmlStringBuffer addFinishElement(String elementName) {
        return addFinishElement(0, elementName);
    }

    public final XmlStringBuffer addFinishElement(int level, String elementName) {
        StringBuffer stringBuffer = this.stringBuffer;
        if (level > 0) {
            add(' ', level * 2);
        }
        stringBuffer.append("</");
        if (defaultNamespacePrefix != null) {
            stringBuffer.append(defaultNamespacePrefix).append(':');
        }
        stringBuffer.append(elementName).append('>');
        return this;
    }

    public final XmlStringBuffer addElement(String elementName, String content, boolean escapeContent) {
        addStartElement(elementName, true);
        if (escapeContent) {
            addValue(content);
        } else {
            add(content);
        }
        addFinishElement(elementName);
        return this;
    }

    public final XmlStringBuffer addElement(String elementName, String content) {
        return addElement(elementName, content, true);
    }

    /**
     * Adds element if it has not null content.
     *
     * @param elementName name of the element to be added.
     * @param content     content of the element to be added.
     * @return this XmlStringBuffer object
     */
    public final XmlStringBuffer addOptionalElement(String elementName, String content) {
        return (content != null) ? addElement(elementName, content) : this;
    }

    /**
     * Adds element, in case content is null, adds empty element
     *
     * @param elementName name of the element to be added.
     * @param content     content of the element to be added.
     * @return this XmlStringBuffer object
     */
    public final XmlStringBuffer addMandatoryElement(String elementName, String content) {
        return addElement(elementName, content == null ? "" : content);
    }

    public final XmlStringBuffer addAttribute(String name, String value) {
        StringBuffer stringBuffer = this.stringBuffer;
        stringBuffer.append(" ").append(name).append("=\"");
        XmlParser.appendEscaped(value, stringBuffer);
        stringBuffer.append("\"");
        return this;
    }

    public final XmlStringBuffer addOptionalAttribute(String name, String value) {
        if (value != null) {
            addAttribute(name, value);
        }
        return this;
    }

    public final XmlStringBuffer addAttribute(String name, int value) {
        StringBuffer stringBuffer = this.stringBuffer;
        stringBuffer.append(" ").append(name).append("=\"");
        stringBuffer.append(value);
        stringBuffer.append("\"");
        return this;
    }

    public final XmlStringBuffer addAttribute(String name, long value) {
        StringBuffer stringBuffer = this.stringBuffer;
        stringBuffer.append(" ").append(name).append("=\"");
        stringBuffer.append(value);
        stringBuffer.append("\"");
        return this;
    }

    public final XmlStringBuffer addAttribute(String name, boolean value) {
        StringBuffer stringBuffer = this.stringBuffer;
        stringBuffer.append(" ").append(name).append("=\"");
        stringBuffer.append(value);
        stringBuffer.append("\"");
        return this;
    }

    public final XmlStringBuffer closeAngleBrace() {
        stringBuffer.append('>');
        return this;
    }

    public final XmlStringBuffer closeAngleBraceElement() {
        stringBuffer.append("/>");
        return this;
    }

    public final StringBuffer getStringBuffer() {
        return stringBuffer;
    }

    public final void setStringBuffer(StringBuffer stringBuffer) {
        this.stringBuffer = stringBuffer;
    }

    public final String getDefaultNamespacePrefix() {
        return defaultNamespacePrefix;
    }

    public final void setDefaultNamespacePrefix(String defaultNamespacePrefix) {
        this.defaultNamespacePrefix = defaultNamespacePrefix;
    }

    public final String toString() {
        return stringBuffer == null ? null : stringBuffer.toString();
    }

    public final void reset() {
        if (stringBuffer != null) {
            stringBuffer.setLength(0);
        }
    }

//  public final XmlStringBuffer addXmlObject(org.apache.xmlbeans.XmlObject xmlObject) {
//      if(xmlObject == null) {
//          return this;
//      }
//      return add(xmlObject.xmlText(XMLOPTIONS_SAVE_OUTER));
//  }


}
