package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

public class LockDeniedOtherOwnerException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int m_lockOwner;

	public LockDeniedOtherOwnerException(int lockOwner) {
		super();
		this.m_lockOwner = lockOwner;
	}

	public int getLockOwner() {
		return m_lockOwner;
	}

}
