package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation;

/**
 * Created by vishal on 18/8/16.
 */
public class NCRequestToPojoMapperException extends Exception {

    public NCRequestToPojoMapperException(Exception e) {
        super(e);
    }

    public NCRequestToPojoMapperException(String msg) {
        super(msg);
    }
}
