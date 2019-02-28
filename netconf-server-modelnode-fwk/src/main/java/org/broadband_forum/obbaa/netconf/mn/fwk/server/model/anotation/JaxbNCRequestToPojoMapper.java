package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.DataType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.RpcArgsInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.RpcArgumentInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vishal on 18/8/16.
 */
public class JaxbNCRequestToPojoMapper implements NCRequestToPojoMapper {
	public static Object getPojoObject(Node node, Class classType) throws NCRequestToPojoMapperException {
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(classType);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement jaxbElement = jaxbUnmarshaller.unmarshal(node, classType);
            if (jaxbElement != null) {
                return jaxbElement.getValue();
            }
            throw new NCRequestToPojoMapperException("Unmarshalled object is null");
        } catch (JAXBException e) {
            throw new NCRequestToPojoMapperException(e);
        }

    }

    @Override
    public List<Object> getRpcArguments(NetconfRpcRequest request, RpcArgsInfo rpcArgsInfo) throws NCRequestToPojoMapperException {
        List<Object> arguments = new ArrayList<>();
        try{
            Document requestDocument = request.getRequestDocument();
            Element docElement = requestDocument.getDocumentElement();
            List<RpcArgumentInfo> rpcArguments = rpcArgsInfo.getRpcArguments();

            for (RpcArgumentInfo rpcArgument : rpcArguments) {
                Class classType = rpcArgument.getType();
                String argumentName = rpcArgument.getArgName();
                String argNamespace = rpcArgument.getNamespace();
                if (DataType.isSingleValueType(classType)) {
                    Node singeValueNode = DocumentUtils.getDescendant(docElement, argumentName, argNamespace);
                    if (singeValueNode!= null) {
                        String argumentValue = singeValueNode.getTextContent().trim();
                        Object value = DataType.createInstanceFrom(classType, argumentValue);
                        arguments.add(value);
                    }
                }else{//not a simple type so should be a pojo
                    Node argumentNode = DocumentUtils.getDescendant(docElement, argumentName, argNamespace);
                    if (argumentNode != null) {
                        Object argumentValue = JaxbNCRequestToPojoMapper.getPojoObject(argumentNode, classType);
                        arguments.add(argumentValue);
                    }
                }
            }
        } catch (NetconfMessageBuilderException e) {
            throw new NCRequestToPojoMapperException(e);
        }
        return arguments;
    }
}
