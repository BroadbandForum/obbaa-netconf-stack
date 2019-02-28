package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

public class LockedByOtherSessionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int m_lockOwner;

	public LockedByOtherSessionException(int lockOwner) {
		m_lockOwner = lockOwner;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getLockOwner() {
		return m_lockOwner;
	}

	

}
