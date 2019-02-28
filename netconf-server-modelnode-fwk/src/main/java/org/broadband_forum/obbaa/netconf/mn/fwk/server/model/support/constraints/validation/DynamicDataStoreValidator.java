package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;

public interface DynamicDataStoreValidator extends DataStoreValidator {
    List<Notification> validate(RootModelNodeAggregator rootModelNodeAggregator, List<EditContainmentNode> editTrees, EditConfigRequest request, NetconfClientInfo clientInfo);
}
