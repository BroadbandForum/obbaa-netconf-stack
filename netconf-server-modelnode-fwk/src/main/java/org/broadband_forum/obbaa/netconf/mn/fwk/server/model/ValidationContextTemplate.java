package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;


public interface ValidationContextTemplate<T> {
    <T> T validate();
}
