package org.octopusden.octopus.multichannelserver.config;

import static org.octopusden.octopus.multichannelserver.config.XMLConst.CONST_XML_PARAM_CHANNEL;
import org.octopusden.octopus.util.xml.XmlStringBuffer;

/**
 * Date: 01.09.2009
 */
public class ServerChannelDefinition extends AbstractParameterStorage {
    public String channelName;

    public ServerChannelDefinition(String channelName) {
        super();
        this.channelName = channelName;
    }

    public ServerChannelDefinition(String channelName, ParameterList parameterList) {
        this.channelName = channelName;
        this.parameterList = parameterList;
    }

    public ServerChannelDefinition() {
        super();
    }

    @Override
    public String getDescription() {
        return "SERVER.XML:" + channelName;
    }

    public XmlStringBuffer serialize(XmlStringBuffer buffer) {
        buffer.addStartElement(1, XMLConst.CONST_XML_PARAM_CHANNEL, false);
        buffer.addAttribute(XMLConst.CONST_XML_ATTR_NAME, channelName).closeAngleBrace().addCR();

        parameterList.serialize(2, buffer);

        buffer.addFinishElement(1, CONST_XML_PARAM_CHANNEL).addCR();
        return buffer;
    }

}
