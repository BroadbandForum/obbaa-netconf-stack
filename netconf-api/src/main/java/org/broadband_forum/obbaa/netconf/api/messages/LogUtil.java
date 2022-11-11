/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.api.messages;

import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshava on 8/13/15.
 */
public class LogUtil {

    public static final String SOMETHING_WENT_WRONG_WHILE_PRINTING_THIS = "-something went wrong while printing this-";
    public static final String NETCONF_LOGGER_NAME = "NETCONF_LOGGER";

    public static void logDebug(AdvancedLogger logger, String message, Object... args) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format(message, getStringValues(logger, args)));
            }
        } catch (Exception e) {
            logger.error("Error while logging ", e);
        }
    }
    
    public static void logDebug(Logger logger, String message, Object... args) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format(message, getStringValues(logger, args)));
            }
        } catch (Exception e) {
            logger.error("Error while logging ", e);
        }
    }

    private static Object[] getStringValues(AdvancedLogger logger, Object... args) {
        List<Object> stringValues = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof NetConfResponse) {
                stringValues.add(((NetConfResponse) arg).responseToString());
            } else if (arg instanceof AbstractNetconfRequest) {
                stringValues.add(((AbstractNetconfRequest) arg).requestToString());
            } else if (arg instanceof Document) {
                try {
                    stringValues.add(DocumentUtils.documentToPrettyString((Document) arg));
                } catch (NetconfMessageBuilderException e) {
                    logger.error("Error while logging ", e);
                    stringValues.add(SOMETHING_WENT_WRONG_WHILE_PRINTING_THIS);
                }
            } else {
                if (null == arg) {
                    stringValues.add("<null>");
                } else {
                    stringValues.add(arg.toString());
                }
            }
        }
        return stringValues.toArray(new Object[stringValues.size()]);
    }

    private static Object[] getStringValues(Logger logger, Object... args) {
        /*This method is retained until all the base-platform and simulator impacts
         * due to changing Logger to AdvancedLogger is completed. */
        List<Object> stringValues = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof NetConfResponse) {
                stringValues.add(((NetConfResponse) arg).responseToString());
            } else if (arg instanceof AbstractNetconfRequest) {
                stringValues.add(((AbstractNetconfRequest) arg).requestToString());
            } else if (arg instanceof Document) {
                try {
                    stringValues.add(DocumentUtils.documentToPrettyString((Document) arg));
                } catch (NetconfMessageBuilderException e) {
                    logger.error("Error while logging ", e);
                    stringValues.add(SOMETHING_WENT_WRONG_WHILE_PRINTING_THIS);
                }
            } else {
                if (null == arg) {
                    stringValues.add("<null>");
                } else {
                    stringValues.add(arg.toString());
                }
            }
        }
        return stringValues.toArray(new Object[stringValues.size()]);
    }
    
    public static void logTrace(AdvancedLogger logger, String message, Object... params) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace(String.format(message, getStringValues(logger, params)));
            }
        } catch (Exception e) {
            logger.error("Error while logging ", e);
        }
    }

    public static void logInfo(AdvancedLogger logger, String message, Object... params) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info(String.format(message, getStringValues(logger, params)));
            }
        } catch (Exception e) {
            logger.error("Error while logging ", e);
        }
    }
    
    public static void logInfo(Logger logger, String message, Object... params) {
        /*This method is retained until all the base-platform and simulator impacts
         * due to changing Logger to AdvancedLogger is completed. */
        try {
            if (logger.isInfoEnabled()) {
                logger.info(String.format(message, getStringValues(logger, params)));
            }
        } catch (Exception e) {
            logger.error("Error while logging ", e);
        }
    }
}
