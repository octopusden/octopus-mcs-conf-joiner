package org.octopusden.octopus.multichannelserver.config;

import org.octopusden.octopus.multichannelserver.config.interfaces.IParameterStorage;

import java.util.ArrayList;

/**
 * Date: 01.09.2009
 */
public abstract class AbstractParameterStorage implements IParameterStorage {
    protected ParameterList parameterList;

    protected AbstractParameterStorage() {
        parameterList = new ParameterList();
    }

    public boolean isParameterPresent(String name) {
        return (parameterList.isParameterPresent(name));
    }

    public void addParameter(Parameter parameter) {
        parameterList.addParameter(parameter);
    }

    @Override
    public void addParameter(int index, Parameter parameter) {
        parameterList.addParameter(index, parameter);
    }

    public void delParameter(Parameter parameter) {
        parameterList.delParameter(parameter);
    }

    public Parameter getParameter(String paramName) {
        return (parameterList.getParameter(paramName));
    }

    public int getParameterCount() {
        return (parameterList.getParameterCount());  //To change body of created methods use File | Settings | File Templates.
    }

    public ArrayList<Parameter> getParameterArrayList() {
        return parameterList.getParameterArrayList();
    }

    public String getParameterValue(String paramName) {
        return parameterList.getParameterValue(paramName);
    }

    public void clear() {
        parameterList.clear();
    }

    public boolean isEmpty() {
        return parameterList.isEmpty();
    }
}
