package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils;

public interface TxTemplate<RT> {
    public RT execute() throws TxException;
}
