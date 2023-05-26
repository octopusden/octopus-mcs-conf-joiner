package org.octopusden.octopus.multichannelserver.config;

import static org.octopusden.octopus.multichannelserver.config.XMLConst.*;
import org.octopusden.octopus.multichannelserver.config.interfaces.IParameterStorage;
import org.octopusden.octopus.util.xml.XmlParser;
import org.octopusden.octopus.util.xml.XmlStringBuffer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Date: 28.08.2009
 */
public class ParameterList implements IParameterStorage {

    HashMap<String, Parameter> parameterHashMap = new HashMap<String, Parameter>(20);
    ArrayList<Parameter> parameters = new ArrayList<Parameter>(20);

    public ParameterList(Node parentNode) {
        // Read Usual Parameters
        ArrayList<Element> elementArrayList = XmlParser.getByTagName(parentNode, CONST_XML_ATTR_PARAMETER);
        // Read user parameters Parameters
        elementArrayList.addAll(XmlParser.getByTagName(parentNode, CONST_XML_ATTR_USER_PARAMETER));
        // Read obsolete parameters
        elementArrayList.addAll(XmlParser.getByTagName(parentNode, CONST_XML_ATTR_OBSOLETE_PARAMETER));
        for (Element element : elementArrayList) {
            Parameter parameter = new Parameter(element);
            addParameter(parameter);
        }

    }

    public ParameterList() {

    }

    public Parameter getParameter(String name) {
        return (parameterHashMap.get(name));
    }

    @Override
    public String getDescription() {
        return "ParameterList";
    }

    public String getParameterValue(String name) {
        Parameter parameter = getParameter(name);
        return (parameter != null ? parameter.value : null);
    }

    public ArrayList<Parameter> getParameterArrayList() {
        return new ArrayList<Parameter>(parameters);
    }

    public boolean isParameterPresent(String name) {
        return (parameterHashMap.containsKey(name));
    }

    public final void addParameter(Parameter parameter) {
        parameters.add(parameter);
        parameterHashMap.put(parameter.paramName, parameter);
    }

    @Override
    public void addParameter(int index, Parameter parameter) {
        parameters.add(index, parameter);
        parameterHashMap.put(parameter.paramName, parameter);
    }

    public void delParameter(Parameter parameter) {
        parameters.remove(parameter);
        parameterHashMap.remove(parameter.paramName);
    }


    public int getParameterCount() {
        return (parameters.size());
    }

    public XmlStringBuffer serialize(int level, XmlStringBuffer buffer) {
        for (Parameter parameter : parameters)
            parameter.serialize(level, buffer);

        return (buffer);
    }

    public boolean isEmpty() {
        return (parameters.isEmpty());
    }

    public void clear() {
        parameters.clear();
    }
}
