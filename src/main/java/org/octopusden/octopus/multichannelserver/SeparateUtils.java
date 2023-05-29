package org.octopusden.octopus.multichannelserver;

import org.octopusden.octopus.fileutils.FileUtils;
import org.octopusden.octopus.multichannelserver.config.*;
import org.octopusden.octopus.multichannelserver.config.interfaces.IParameterStorage;
import org.octopusden.octopus.stringutils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Date: 31.08.2009
 */
@SuppressWarnings({ "JavaDoc" })
public class SeparateUtils {
  private static final Logger logger = LoggerFactory.getLogger(XMLConfJoiner.class);

    /**
     * Procedure Split Client Config
     *
     * @param programOptionsChannelDefinition
     *                     - Program Options
     * @param programOptionsConnectDefinition ToDo
     * @param programOptionsServerDefinition ToDo
     * @param clientConfig - Client Config for splitting. Resulting config will be placed back in this variable
     * @param userConfig   - User Folder Config
     * @return ToDo
     */
    public static boolean separateClientConfig(ServerDefinition programOptionsServerDefinition,
                                               ChannelDefinition programOptionsChannelDefinition,
                                               ConnectDefinition programOptionsConnectDefinition,
                                               ConfigDefinition clientConfig,
                                               ConfigDefinition userConfig) {
        userConfig.clear();

        // Separate Server XML
        if (!separateServer(programOptionsServerDefinition, clientConfig.getServerDefinition(), userConfig))
            return (false);

        // Iterate Client channels
        ArrayList<ChannelDefinition> channelDefinitions = clientConfig.getChannelDefinitionArrayList();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            if (!separateClientChannel(programOptionsChannelDefinition, channelDefinition, userConfig)) return (false);
        }

        // Separate Connects
        ArrayList<ConnectDefinition> connectDefinitions = clientConfig.getConnectDefinitions();
        for (ConnectDefinition connectDefinition : connectDefinitions) {
            separateConnect(programOptionsConnectDefinition, connectDefinition, userConfig);
        }


        if (!userConfig.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("Split client configuration completed successfully. Produced {} user channels", userConfig.getChannelCount());
        }

        return (true);
    }

    private static void separateConnect(ConnectDefinition programOptionsConnectDefinition, ConnectDefinition clientConnectDefinition, ConfigDefinition userConfig) {

        ConnectDefinition userConnectDefinition = new ConnectDefinition();
        userConnectDefinition.fileName = clientConnectDefinition.fileName;

        splitParameters(false, programOptionsConnectDefinition, clientConnectDefinition, userConnectDefinition);
        if (!userConnectDefinition.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("    Split connect '{}' completed successfully. Produced {} user parameters",
                        clientConnectDefinition.fileName,
                        userConnectDefinition.getParameterCount());
            userConfig.addConnect(userConnectDefinition);
        }
    }

    private static boolean separateServer(ServerDefinition programOptionsServerDefinition,
                                          ServerDefinition clientServerDefinition,
                                          ConfigDefinition userConfig) {

        if (logger.isDebugEnabled())
            logger.debug("  Split server '{}'", clientServerDefinition.getDescription());


        // Split server Parameters
        if (logger.isTraceEnabled())
            logger.trace("    Split server parameters");
        ServerDefinition userServerDefinition = new ServerDefinition();
        splitParameters(false, programOptionsServerDefinition, clientServerDefinition, userServerDefinition);

        // Split channels
        ArrayList<ServerChannelDefinition> clientServerChannelDefinitions = clientServerDefinition.getChannels();
        for (ServerChannelDefinition clientServerChannelDefinition : clientServerChannelDefinitions)
            if (!separateServerChannel(programOptionsServerDefinition, clientServerChannelDefinition, userServerDefinition))
                return (false);

        // if new user channels or parameters is present, add server to user config
        if (!userServerDefinition.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("    Split server '{}' completed successfully. Produced {} user parameters and {} user channels",
                        new Object[]{clientServerDefinition.getDescription(),
                        userServerDefinition.getParameterCount(),
                        userServerDefinition.getChannels().size()});

            userConfig.setServerDefinition(userServerDefinition);
        }
        return (true);
    }

    private static boolean separateServerChannel(ServerDefinition programOptionsServerDefinition,
                                                 ServerChannelDefinition clientServerChannelDefinition,
                                                 ServerDefinition userServerDefinition) {
        ServerChannelDefinition userServerChannelDefinition = new ServerChannelDefinition(clientServerChannelDefinition.channelName);

        if (logger.isTraceEnabled())
            logger.trace(String.format("    Split server channel '%s'", clientServerChannelDefinition.getDescription()));

        // Find program Options Server Channel
        ServerChannelDefinition programOptionsServerChannelDefinition = findServerChannelDefinition(programOptionsServerDefinition, clientServerChannelDefinition);

        if (programOptionsServerChannelDefinition == null) return (true);

        if (logger.isTraceEnabled())
            logger.trace(String.format("    Found matching server channel with name '%s' in program options",
              programOptionsServerChannelDefinition.channelName));

        splitParameters(false, programOptionsServerChannelDefinition, clientServerChannelDefinition, userServerChannelDefinition);

        // If we have new created user parameters, add it to user configuration
        if (!userServerChannelDefinition.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("    Split server channel '%s' completed successfully. Produced %d user parameters",
                  clientServerChannelDefinition.channelName,
                  userServerChannelDefinition.getParameterCount()));

            userServerDefinition.addChannel(userServerChannelDefinition);
        }

        return (true);
    }

    private static ServerChannelDefinition findServerChannelDefinition(ServerDefinition programOptionsServerDefinition,
                                                                       ServerChannelDefinition clientServerChannelDefinition) {
        ArrayList<ServerChannelDefinition> programOptServerChannelDefinitions = programOptionsServerDefinition.getChannels();
        for (ServerChannelDefinition serverChannelDefinition : programOptServerChannelDefinitions) {
            if (serverChannelDefinition.channelName == null ||
                    clientServerChannelDefinition.channelName.equals(serverChannelDefinition.channelName))
                return (serverChannelDefinition);
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }


    private static boolean separateClientChannel(ChannelDefinition programOptionsChannelDefinition, ChannelDefinition clientChannelDefinition, ConfigDefinition userConfig) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("  Split channel '%s'", clientChannelDefinition.channelName));

        ChannelDefinition userChannelDefinition = new ChannelDefinition(clientChannelDefinition.fileName, clientChannelDefinition.channelName);

        // Split Channel Parameters
        if (logger.isTraceEnabled())
            logger.trace("    Split channel parameters");
        splitParameters(false, programOptionsChannelDefinition, clientChannelDefinition, userChannelDefinition);


        // Split Module Definitions
        if (logger.isTraceEnabled())
            logger.trace("    Split channel modules");
        ArrayList<ModuleDefinition> moduleDefinitions = clientChannelDefinition.getModuleDefinitionArrayList();
        // Name of matched modules, need for modules who depend on another
        HashSet<String> programOptionsMatchedModulesNames = new HashSet<String>(moduleDefinitions.size());
        for (ModuleDefinition moduleDefinition : moduleDefinitions) {
            if (!separateClientModule(programOptionsChannelDefinition, moduleDefinition, userChannelDefinition, programOptionsMatchedModulesNames))
                return (false);
        }

        // if new user modules or parameters is present, add channel to user config
        if (!userChannelDefinition.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("    Split channel '%s' completed successfully. Produced %d user parameters and %d user modules",
                  clientChannelDefinition.channelName, userChannelDefinition.getParameterCount(), userChannelDefinition.getModuleCount()));
            userConfig.addChannel(clientChannelDefinition.channelName, userChannelDefinition);
        }

        return (true);
    }

    /**
     * @param programOptionsChannelDefinition
     *
     * @param clientModuleDefinition
     * @return User Module Defintion
     */
    private static boolean separateClientModule(ChannelDefinition programOptionsChannelDefinition,
                                                ModuleDefinition clientModuleDefinition,
                                                ChannelDefinition userChannelDefinition,
                                                HashSet<String> programOptionsMatchedModulesNames) {
        ModuleDefinition userModuleDefinition = new ModuleDefinition(clientModuleDefinition.type, clientModuleDefinition.moduleName, false);

        if (logger.isTraceEnabled())
            logger.trace(String.format("    Split module '%s'", clientModuleDefinition.type));

        // Find program Options Module
        ModuleDefinition programOptionsModuleDefinition = findModuleDefinition(programOptionsChannelDefinition, clientModuleDefinition, programOptionsMatchedModulesNames);
        if (programOptionsModuleDefinition == null) {
            return (true);
        } else if (logger.isTraceEnabled())
            logger.trace(String.format("    Found matching module with name '%s' in program options (TYPE:%s DLL:%s FUNCTION:%s DEPEND_ON:%s)",
              programOptionsModuleDefinition.moduleName,
              programOptionsModuleDefinition.type,
              programOptionsModuleDefinition.dll,
              programOptionsModuleDefinition.function,
              programOptionsModuleDefinition.dependOnModule));

        // Add matched module
        programOptionsMatchedModulesNames.add(programOptionsModuleDefinition.moduleName);

        // If User Module, fill mandatory parameters
        if (programOptionsModuleDefinition.isUser) {
            // Add to User Channel User Module
            userChannelDefinition.addModuleDefinition(userModuleDefinition);
            // Make Client Module with User Flag
            clientModuleDefinition.isUser = true;

            splitParameters(programOptionsModuleDefinition.isUser, programOptionsModuleDefinition, clientModuleDefinition, userModuleDefinition);
        }
        // if not User Module, make User Params
        else {

            splitParameters(programOptionsModuleDefinition.isUser, programOptionsModuleDefinition, clientModuleDefinition, userModuleDefinition);

            // If we have new created user parameters, add it to user configuration
            if (!userModuleDefinition.isEmpty())
                userChannelDefinition.addModuleDefinition(userModuleDefinition);
        }


        return (true);
    }


    private static void splitParameters(boolean isUserParameterStorage, IParameterStorage programOptionsParameterStorage, IParameterStorage clientParameterStorage, IParameterStorage userParameterStorage) {
        // Get client Parameters
        ArrayList<Parameter> parameterArrayList = clientParameterStorage.getParameterArrayList();

        // Remove obsolete Parameters
        for (Parameter parameter : parameterArrayList) {
            // Take corresponding program options parameter
            Parameter programOptionsParameter = programOptionsParameterStorage.getParameter(parameter.paramName);

            // if this parameter obolete, remove it!
            if ((programOptionsParameter != null) && (programOptionsParameter.isObsolete)) {
                clientParameterStorage.delParameter(parameter);
                if (logger.isTraceEnabled()) {
                    logger.trace(String.format("      Found obsolete param(%s) in user data(%s). Removed from configuration", parameter.paramName, clientParameterStorage.getDescription()));
                    logger.trace(String.format("      Was init:%s=\"%s\" --> Now deleted",
                      parameter.paramName, parameter.value));
                }
            }
        }

        // Get client Parameters Again without obsolete params
        parameterArrayList = clientParameterStorage.getParameterArrayList();


        // If User Module, fill mandatory parameters
        // User Module definition can contain only non user parameter (PARAMETER)
        if (isUserParameterStorage) {
            // Now work with Mandatory Parameters
            for (Parameter parameter : parameterArrayList) {
                // if param not present , move to user Module
                if (!programOptionsParameterStorage.isParameterPresent(parameter.paramName)) {
                    userParameterStorage.addParameter(parameter);
                    clientParameterStorage.delParameter(parameter);
                    if (logger.isTraceEnabled()) {
                        logger.trace(String.format("      Found user param(%s) in user data(%s). Moved to user configuration", parameter.paramName, clientParameterStorage.getDescription()));
                        logger.trace(String.format("      Was init:%s=\"%s\" --> Now user:%s=\"%s\"",
                          parameter.paramName, parameter.value,
                          parameter.paramName, parameter.value));
                    }

                }
            }
        }
        // if not User Module, make User Params
        // Not User Module definition can contain only user parameter (USER_PARAMETER)
        else {
            // Now work with User Parameters
            for (Parameter parameter : parameterArrayList) {
                // if param present, it's user parameter do special things
                if (programOptionsParameterStorage.isParameterPresent(parameter.paramName)) {
                    // Move original to user folder
                    userParameterStorage.addParameter(parameter);
                    // Remove original from client folder
                    clientParameterStorage.delParameter(parameter);
                    // Add our user Parameter from option to client folder instead of existing
                    Parameter programOptionParameter = programOptionsParameterStorage.getParameter(parameter.paramName);
                    clientParameterStorage.addParameter(programOptionParameter);
                    if (logger.isTraceEnabled()) {
                        logger.trace(String.format("      Found user param(%s) in non user data(%s). Moved original to user configuration, replace in client config by item from program options", parameter.paramName, clientParameterStorage.getDescription()));
                        logger.trace(String.format("      Was init:%s=\"%s\" --> Now init:%s=\"%s\" user:%s=\"%s\"",
                          parameter.paramName, parameter.value,
                          programOptionParameter.paramName, programOptionParameter.value,
                          parameter.paramName, parameter.value));
                    }
                }
            }
        }
    }


    /**
     * Find module definition in program options for converting needs
     *
     * @param programOptionsChannelDefinition ToDo
     * @param clientModuleDefinition ToDo
     * @param programOptionsMatchedModulesNames ToDo
     * @return ToDo
     */

    public static ModuleDefinition findModuleDefinition(ChannelDefinition programOptionsChannelDefinition,
                                                        ModuleDefinition clientModuleDefinition,
                                                        HashSet<String> programOptionsMatchedModulesNames) {
        ArrayList<ModuleDefinition> programOptionModuleDefinitions = programOptionsChannelDefinition.getModuleDefinitionArrayList();

        // Take key options from client module
        String clientDLL = FileUtils.getFileName(clientModuleDefinition.getParameterValue(XMLConst.CONST_XML_PARAM_DLL));
        String clientFunction = clientModuleDefinition.getParameterValue(XMLConst.CONST_XML_PARAM_FUNCTION);

        // Looking for program options module
        for (ModuleDefinition moduleDefinition : programOptionModuleDefinitions) {
            if ((moduleDefinition.type.equals(clientModuleDefinition.type)) &&
                    (moduleDefinition.isMatchedOptionDll(clientDLL)) &&
                    ( StringUtils.isEmptyOrNull(moduleDefinition.function) || moduleDefinition.function.equals(clientFunction))
                    ) {
                // Found program Options module

                // Is it Dependent on another?
                if (!StringUtils.isEmptyOrNull(moduleDefinition.dependOnModule)) {
                    // if we already have parent module, return it, otherwise continue searching
                    if (programOptionsMatchedModulesNames.contains(moduleDefinition.dependOnModule))
                        return (moduleDefinition);
                } else {
                    return (moduleDefinition);
                }

            }
        }

        return (null);
    }


    public static boolean checkPreconditions(String mcsFolderName, ConfigDefinition clientConfigDefinition) {
        if (FileUtils.isFolderExist(MCSUtils.getMCSConfigUserFolderName(mcsFolderName))) {
            logger.error(String.format("User folder ('user') already exists in client config folder. User folder(%s)", MCSUtils.getMCSConfigUserFolderName(mcsFolderName)));
            return (false);
        }

        // Check Client Config for user parameters and modules
        ArrayList<ChannelDefinition> channelDefinitions = clientConfigDefinition.getChannelDefinitionArrayList();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            ArrayList<ModuleDefinition> moduleDefinitions = channelDefinition.getModuleDefinitionArrayList();
            for (ModuleDefinition moduleDefinition : moduleDefinitions) {
                // Check user module
                if (moduleDefinition.isUser) {
                    logger.error(String.format("User module is already present in client config. Channel(%s), module(%s)", channelDefinition.channelName, moduleDefinition.type));
                    return (false);
                }

                ArrayList<Parameter> parameterArrayList = moduleDefinition.getParameterArrayList();
                for (Parameter parameter : parameterArrayList) {
                    // Check user parameter
                    if (parameter.isUser) {
                        logger.error(String.format("User parameter is already present in client config. Channel(%s), module(%s), parameter(%s)", channelDefinition.channelName, moduleDefinition.type, parameter.paramName));
                        return (false);
                    }
                }
            }
        }

        return (true);
    }
}
