package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils;

/**
 * Created by keshava on 4/12/15.
 */
public class AnnotationAnalysisException extends Exception {
    public AnnotationAnalysisException() {
    }

    public AnnotationAnalysisException(String message) {
        super(message);
    }

    public AnnotationAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationAnalysisException(Throwable cause) {
        super(cause);
    }

    public AnnotationAnalysisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
