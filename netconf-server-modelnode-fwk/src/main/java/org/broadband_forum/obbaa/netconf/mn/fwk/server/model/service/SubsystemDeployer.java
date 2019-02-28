package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import java.util.Map;

import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;

/**
 * Created by keshava on 12/9/15.
 */
public class SubsystemDeployer implements SchemaRegistryVisitor {
    private final Map<SchemaPath, SubSystem> m_subSystems;
    private final SubSystemRegistry m_subsystemRegistry;
    public SubsystemDeployer(SubSystemRegistry subsystemRegistry, Map<SchemaPath, SubSystem> subSystems) {
        m_subsystemRegistry = subsystemRegistry;
        m_subSystems = subSystems;
    }

    @Override
    public void visitLeafListNode(String componentId, SchemaPath parentSchemaPath, LeafListSchemaNode leafListNode) {
        registerSubsystem(leafListNode,componentId);
    }

    private void registerSubsystem(DataSchemaNode dataSchemaNode,String componentId) {
        SubSystem subSystem = getSubsystem(dataSchemaNode.getPath());
		if (subSystem == null && SchemaRegistryUtil.isMountPointEnabled()) {
			SchemaPath parentPath = (SchemaPath) RequestScope.getCurrentScope()
					.getFromCache(SchemaRegistryUtil.MOUNT_PATH);
			if (parentPath != null) {
				subSystem = m_subSystems.get(parentPath);
			}
		}
        if(subSystem != null){
            m_subsystemRegistry.register(componentId, dataSchemaNode.getPath(), subSystem);
        }
    }

    private SubSystem getSubsystem(SchemaPath dataSchemaPath) {
        SubSystem subSystem = m_subSystems.get(dataSchemaPath);
        if(subSystem != null){
            return subSystem;
        }else{
            if(dataSchemaPath == null){
                return null;
            }
            return getSubsystem(dataSchemaPath.getParent());
        }
    }

    /*private String getSchemaPathString(SchemaPath dataSchemaPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        Iterable<QName> pathFromRoot = dataSchemaPath.getPathFromRoot();
        Iterator<QName> iterator = pathFromRoot.iterator();
        while(iterator.hasNext()){
            sb.append(iterator.next().getLocalName()).append("/");
        }
        return sb.toString();
    }
*/
    @Override
    public void visitLeafNode(String componentId, SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode) {
        registerSubsystem(leafSchemaNode,componentId);
    }

    @Override
    public void visitChoiceCaseNode(String componentId, SchemaPath parentPath, CaseSchemaNode choiceCaseNode) {
        registerSubsystem(choiceCaseNode,componentId);
    }

    @Override
    public void visitChoiceNode(String componentId, SchemaPath parentPath, ChoiceSchemaNode choiceSchemaNode) {
        for(CaseSchemaNode caseNode : choiceSchemaNode.getCases().values()){
            //let the helper be registered at the grand-parent level
            visitChoiceCaseNode(componentId, parentPath, caseNode);
        }
    }

    @Override
    public void visitAnyXmlNode(String componentId, SchemaPath parentPath, AnyXmlSchemaNode anyXmlSchemaNode) {
        registerSubsystem(anyXmlSchemaNode,componentId);
    }

    @Override
    public void visitListNode(String componentId, SchemaPath parentPath, ListSchemaNode listSchemaNode) {
        registerSubsystem(listSchemaNode, componentId);
    }

    @Override
    public void visitContainerNode(String componentId, SchemaPath parentSchemaPath, ContainerSchemaNode containerSchemaNode) {
        registerSubsystem(containerSchemaNode, componentId);
    }

    @Override
    public void visitIdentityNode(String componentId, IdentitySchemaNode identitySchemaNode) {

    }

    @Override
    public void visitEnter(String componentId, SchemaPath parentPath, SchemaPath schemaPath) {

    }

    @Override
    public void visitLeave(String componentId, SchemaPath parentPath, SchemaPath schemaPath) {

    }
}
