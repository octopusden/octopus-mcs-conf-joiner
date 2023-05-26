package org.octopusden.octopus.multichannelserver.config;

import org.octopusden.octopus.util.xml.XmlParser;
import org.octopusden.octopus.util.xml.XmlStringBuffer;
import org.w3c.dom.Node;

import static org.octopusden.octopus.multichannelserver.config.XMLConst.*;

/**
 * Date: 31.08.2009
 */
public class Parameter {
    public boolean isUser;
    public boolean isObsolete = false;
    public String paramName;
    public String value;
    public String comment;

    public Parameter( boolean user, String paramName, String value, String comment )
    {
        isUser = user;
        this.paramName = paramName;
        this.value = value;
        this.comment = comment;
    }

    public Parameter( Node parameterNode )
    {
        String nodeName = parameterNode.getNodeName();
        isUser = nodeName.equals( CONST_XML_ATTR_USER_PARAMETER );
        isObsolete = nodeName.equals( CONST_XML_ATTR_OBSOLETE_PARAMETER );
        paramName = XmlParser.safeGetAttribute( parameterNode, CONST_XML_ATTR_NAME );
        value = XmlParser.safeGetAttribute( parameterNode, CONST_XML_ATTR_VALUE );
        comment = XmlParser.safeGetAttribute( parameterNode, XMLConst.CONST_XML_ATTR_COMMENT );
    }

    @SuppressWarnings({ "CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException" })
    public Parameter clone()
    {
        return ( new Parameter( isUser, paramName, value, comment ) );
    }

    public XmlStringBuffer serialize( int level, XmlStringBuffer buffer )
    {
        buffer.addStartElement( level, ( isUser ? XMLConst.CONST_XML_ATTR_USER_PARAMETER : XMLConst.CONST_XML_ATTR_PARAMETER ), false );
        buffer.addAttribute( XMLConst.CONST_XML_ATTR_NAME, paramName );
        if ( value != null )
            buffer.addAttribute( XMLConst.CONST_XML_ATTR_VALUE, value );
        if ( comment != null )
            buffer.addAttribute( XMLConst.CONST_XML_ATTR_COMMENT, comment );
        buffer.closeAngleBraceElement().addCR();
        return ( buffer );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Parameter parameter = ( Parameter ) o;

        if ( isUser != parameter.isUser ) return false;
        if ( comment != null ? !comment.equals( parameter.comment ) : parameter.comment != null ) return false;
        if ( paramName != null ? !paramName.equals( parameter.paramName ) : parameter.paramName != null ) return false;
        //noinspection RedundantIfStatement
        if ( value != null ? !value.equals( parameter.value ) : parameter.value != null ) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = ( isUser ? 1 : 0 );
        result = 31 * result + ( paramName != null ? paramName.hashCode() : 0 );
        result = 31 * result + ( value != null ? value.hashCode() : 0 );
        result = 31 * result + ( comment != null ? comment.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString()
    {
        return paramName + ':' + value;
    }
}
