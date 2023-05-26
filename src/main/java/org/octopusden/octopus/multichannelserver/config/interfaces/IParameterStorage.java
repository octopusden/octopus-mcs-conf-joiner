package org.octopusden.octopus.multichannelserver.config.interfaces;

import org.octopusden.octopus.multichannelserver.config.Parameter;

import java.util.ArrayList;


/**
 * Interface for all Parameters storage
 */
public interface IParameterStorage {
    void addParameter(Parameter parameter);

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     *
     * @param index
     * @param parameter
     */
    void addParameter(int index, Parameter parameter);

    void delParameter(Parameter parameter);

    ArrayList<Parameter> getParameterArrayList();

    boolean isParameterPresent(String name);

    /**
     * get Parameter from storage
     *
     * @param paramName
     * @return Parameter if found, null otherwise
     */
    Parameter getParameter(String paramName);

    String getDescription();

    int getParameterCount();

    /**
     * @param paramName
     * @return param Value if present, null otherwise
     */
    String getParameterValue(String paramName);
}
