package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.BACKWARD_SLASH_CHAR;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.BACKWARD_SLASH_ENCODER;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.EQUAL_TO;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.EQUAL_TO_CHAR;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.EQUAL_TO_ENCODER;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.FORWARD_SLASH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.FORWARD_SLASH_CHAR;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.FORWARD_SLASH_ENCODER;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.PLUS;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class CharacterCodec {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CharacterCodec.class, LogAppNames.NETCONF_STACK);
    public static String encode(final String text) {
        if (text != null) {
            StringBuilder sb = new StringBuilder();
            for (char ch : text.toCharArray()) {
                if (ch == FORWARD_SLASH_CHAR) {
                    sb = sb.append(FORWARD_SLASH_ENCODER);
                    continue;
                } else if (ch == EQUAL_TO_CHAR) {
                    sb = sb.append(EQUAL_TO_ENCODER);
                    continue;
                } else if (ch == BACKWARD_SLASH_CHAR) {
                    sb = sb.append(BACKWARD_SLASH_ENCODER);
                    continue;
                }
                sb = sb.append(ch);
            }
            return sb.toString();
        }
        LOGGER.warn("The input is null, hence not encoding");
        return null;
    }

    public static String decode(String text) {
        if (text != null) {
            if (text.contains(BACKWARD_SLASH_ENCODER)) {
                text = text.replaceAll(BACKWARD_SLASH_ENCODER + PLUS, BACKWARD_SLASH_ENCODER);
            }
            if (text.contains(EQUAL_TO_ENCODER)) {
                text = text.replaceAll(BACKWARD_SLASH_ENCODER + EQUAL_TO, EQUAL_TO);
            }
            if (text.contains(FORWARD_SLASH_ENCODER)) {
                text = text.replaceAll(BACKWARD_SLASH_ENCODER + FORWARD_SLASH, FORWARD_SLASH);
            }
            return text;
        }
        LOGGER.warn("The input is null, hence not decoding");
        return null;
    }
}
