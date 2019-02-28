package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

public class ModelNodeInitException extends Exception {
	private static final long serialVersionUID = 1L;

	public ModelNodeInitException() {
		super();
	}

	public ModelNodeInitException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ModelNodeInitException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelNodeInitException(String message) {
		super(message);
	}

	public ModelNodeInitException(Throwable cause) {
		super(cause);
	}

}
