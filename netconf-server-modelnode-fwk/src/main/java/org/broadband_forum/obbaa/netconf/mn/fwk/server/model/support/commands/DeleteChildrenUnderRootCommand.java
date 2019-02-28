package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;

public class DeleteChildrenUnderRootCommand implements Command{
    private ModelNodeHelperRegistry m_modelHelperRegistry;
    private SchemaPath m_schemaPath;
    private ModelNode m_modelNode;

    public DeleteChildrenUnderRootCommand(ModelNodeHelperRegistry modelHelperRegistry, SchemaPath schemaPath, ModelNode modelNode) {
        this.m_modelHelperRegistry = modelHelperRegistry;
        this.m_schemaPath = schemaPath;
        this.m_modelNode = modelNode;
    }

    @Override public void execute() throws CommandExecutionException {

        try {
            //delete leaves
            final Map<QName, ConfigAttributeHelper> leafHelper = m_modelHelperRegistry.getConfigAttributeHelpers(m_schemaPath);
            Map<QName, ConfigAttributeHelper> notKeyLeafHelper = new HashMap<>(leafHelper);
            Map<QName, ConfigAttributeHelper> keys = m_modelHelperRegistry.getNaturalKeyHelpers(m_schemaPath);
            for (QName qName : keys.keySet()) {
                notKeyLeafHelper.remove(qName);
            }
            for (Map.Entry<QName, ConfigAttributeHelper> leafHelperEntry : notKeyLeafHelper.entrySet()) {
                ConfigAttributeHelper configAttributeHelper = leafHelperEntry.getValue();
                configAttributeHelper.setValue(m_modelNode, null);
            }

            //delete leaf list
            final Map<QName, ChildLeafListHelper> leafListHelper = m_modelHelperRegistry.getConfigLeafListHelpers(m_schemaPath);
            for (Map.Entry<QName, ChildLeafListHelper> leafHelperEntry : leafListHelper.entrySet()) {
                ChildLeafListHelper helper = leafHelperEntry.getValue();
                helper.removeAllChild(m_modelNode);
            }

            //delete container
            final Map<QName, ChildContainerHelper> containerHelper = m_modelHelperRegistry.getChildContainerHelpers(m_schemaPath);
            for (Map.Entry<QName, ChildContainerHelper> containerHelperEntry : containerHelper.entrySet()) {
                ChildContainerHelper helper = containerHelperEntry.getValue();
                helper.deleteChild(m_modelNode);
            }

            //delete list
            final Map<QName, ChildListHelper> listHelper = m_modelHelperRegistry.getChildListHelpers(m_schemaPath);
            for (Map.Entry<QName, ChildListHelper> qNameChildListHelperEntry : listHelper.entrySet()) {
                ChildListHelper helper = qNameChildListHelperEntry.getValue();
                helper.removeAllChild(m_modelNode);
            }
        } catch (ModelNodeDeleteException | SetAttributeException e) {
            throw new CommandExecutionException(e);
        }

    }
}
