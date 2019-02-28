package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

public class ModelNodeDeleteException extends Exception {

	private static final long serialVersionUID = -4567452590514478472L;

	public ModelNodeDeleteException() {
	}

	public ModelNodeDeleteException(String message) {
		super(message);
	}

	public ModelNodeDeleteException(Throwable cause) {
		super(cause);
	}

	public ModelNodeDeleteException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelNodeDeleteException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
