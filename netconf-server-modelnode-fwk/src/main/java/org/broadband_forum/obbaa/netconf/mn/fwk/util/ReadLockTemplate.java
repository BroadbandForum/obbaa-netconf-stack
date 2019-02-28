package org.broadband_forum.obbaa.netconf.mn.fwk.util;


public interface ReadLockTemplate<T> {
    T execute();
}