package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

public class UnlockFailedOtherOwnerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int m_lockOwner;

	public UnlockFailedOtherOwnerException(int lockOwner) {
		m_lockOwner = lockOwner;
	}

	public int getLockOwner() {
		return m_lockOwner;
	}

}
