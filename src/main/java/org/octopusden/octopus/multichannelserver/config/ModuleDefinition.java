package org.octopusden.octopus.multichannelserver.config;

import org.octopusden.octopus.stringutils.StringUtils;
import org.octopusden.octopus.util.xml.XmlParser;
import org.octopusden.octopus.util.xml.XmlStringBuffer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.Map;

import static org.octopusden.octopus.multichannelserver.config.XMLConst.*;

/**
 * Date: 28.08.2009
 */
public class ModuleDefinition extends AbstractParameterStorage {
    public String type;
    public String moduleName;
    public boolean isUser;
    public String dll;
    public String function;
    public String dependOnModule;
    public String includeModule;

    // List of possibly name for different platforms
    private HashSet<String> libNames;


    public ModuleDefinition( Node node )
    {
        isUser = node.getNodeName().equals( CONST_XML_PARAM_USER_MODULE );
        type = XmlParser.safeGetAttribute(node, CONST_XML_ATTR_TYPE);
        moduleName = XmlParser.safeGetAttribute( node, CONST_XML_ATTR_NAME );
        dll = XmlParser.safeGetAttribute( node, CONST_XML_PARAM_DLL );
        function = XmlParser.safeGetAttribute( node, CONST_XML_PARAM_FUNCTION );
        dependOnModule = XmlParser.safeGetAttribute( node, CONST_XML_ATTR_DEPEND_ON_MODULE );

        parameterList = new ParameterList( node );

        // Load Include Module Name
        Element includeModuleElement = XmlParser.safeGetFirstChild( node, CONST_XML_PARAM_INCLUDE );
        if ( includeModuleElement != null )
            includeModule = XmlParser.safeGetAttribute( includeModuleElement, CONST_XML_ATTR_MODULE_NAME );

        // Libs
        if ( dll != null )
        {
            libNames = new HashSet<String>( 5 );
            dll = dll.toUpperCase();
            libNames.add( dll + ".DLL" );
            libNames.add( "LIB" + dll + ".SO" );

        }
    }

    public ModuleDefinition( String type, String moduleName, boolean isUser )
    {
        this.type = type;
        this.moduleName = moduleName;
        this.isUser = isUser;
        parameterList = new ParameterList();
    }

    public String getParameterValue( String name )
    {
        return ( parameterList.getParameterValue( name ) );
    }

    public boolean isMatchedOptionDll( String dllName )
    {
        // For any libs
        if ( libNames == null ) return ( true );
        else if ( StringUtils.isEmptyOrNull(dllName) ) return ( true );
        else
            return ( libNames.contains( dllName.toUpperCase() ) );
    }

    public XmlStringBuffer serialize( int level, XmlStringBuffer buffer )
    {
        String xmlTagName = isUser ? XMLConst.CONST_XML_PARAM_USER_MODULE : XMLConst.CONST_XML_PARAM_MODULE;
        buffer.addStartElement( level, xmlTagName, false );
        buffer.addAttribute( XMLConst.CONST_XML_ATTR_TYPE, type );
        // Save Parameters
        if ( !parameterList.isEmpty() )
        {
            buffer.closeAngleBrace().addCR();
            parameterList.serialize( level + 1, buffer );
            buffer.addFinishElement( level, xmlTagName ).addCR();
        } else
            buffer.closeAngleBraceElement().addCR();
        return ( buffer );
    }

    @Override
    public String getDescription()
    {
        return ( "MODULE:" + type );  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void resolveIncludedModules( Map<String, ModuleDefinition> moduleDefinitionsByName ) throws Exception
    {
        if ( includeModule == null ) return;
        ModuleDefinition includedModuleDefinition = moduleDefinitionsByName.get( includeModule );
        if ( includedModuleDefinition == null )
            throw new Exception( String.format( "Failed to find included module[%s] for module[%s]", includeModule, moduleName ) );

        // resolve inner dependencies
        includedModuleDefinition.resolveIncludedModules( moduleDefinitionsByName );

        // now merge Modules (include modules)
        includeModule( includedModuleDefinition );

        includeModule = null; // clear flag after merging
    }

    private void includeModule( ModuleDefinition moduleDefinition )
    {
        for ( Parameter parameter : moduleDefinition.getParameterArrayList() )
            // Include only not present parameters
            if ( !isParameterPresent( parameter.paramName ) ) addParameter( parameter );
    }

    @Override
    public String toString()
    {
        return moduleName;
    }
}
