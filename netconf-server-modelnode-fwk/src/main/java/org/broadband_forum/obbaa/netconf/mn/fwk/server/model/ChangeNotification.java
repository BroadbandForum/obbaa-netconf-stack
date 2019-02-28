package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

public interface ChangeNotification {
    public ChangeType getType();
    public ModelNodeId getModelNodeId();
    String toString(boolean printChangeSource);
}