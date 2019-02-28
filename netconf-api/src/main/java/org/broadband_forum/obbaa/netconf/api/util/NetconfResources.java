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

package org.broadband_forum.obbaa.netconf.api.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.sshd.common.cipher.BuiltinCiphers;
import org.apache.sshd.common.mac.BuiltinMacs;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Various netconf resources
 *
 *
 *
 */
public final class NetconfResources {

    public static final List<BuiltinCiphers> SSH_CIPHERS_PREFERENCE =
            Collections.unmodifiableList(Arrays.asList(
                    BuiltinCiphers.aes128ctr,
                    BuiltinCiphers.aes192ctr,
                    BuiltinCiphers.aes256ctr
            ));
    public static final List<BuiltinMacs> SSH_MAC_PREFERENCE =
            Collections.unmodifiableList(Arrays.asList(
                    BuiltinMacs.hmacsha1,
                    BuiltinMacs.hmacsha256,
                    BuiltinMacs.hmacsha512
            ));
    public static final String CLOSE_SUBSCRIPTION = "close-subscription";
    public static final String CREATE_SUBSCRIPTION = "create-subscription";
    public static final String TYPE = "type";
    public static final String GET_CONFIG = "get-config";
    public static final String GET_CONFIG_CONFIG = "config";
    public static final String EDIT_CONFIG = "edit-config";
    public static final String EDIT_CONFIG_CONFIG = "config";
    public static final String RPC = "rpc";
    public static final String ACTION = "action";
    public static final String MESSAGE_ID = "message-id";
    public static final String NETCONF = "NETCONF";
    public static final String STATE_CHANGE = "STATE_CHANGE";
    public static final String NETCONF_RPC_NS_1_0 = "urn:ietf:params:xml:ns:netconf:base:1.0";
    public static final String NETCONF_BASE_CAP_1_0 = "urn:ietf:params:netconf:base:1.0";
    public static final String NETCONF_BASE_CAP_1_1 = "urn:ietf:params:netconf:base:1.1";
    public static final String CAPABILITY_TYPE = "CAPABILITY_TYPE";
    public static final String DEFAULT_VALUE_1_1 = "1.1";
    public static final String NETCONF_YANG_1 = "urn:ietf:params:xml:ns:yang:1";
    public static final String NETCONF_NOTIFICATION = "urn:ietf:params:netconf:capability:notification:1.0";
    public static final String NETCONF_NOTIFICATION_NS = "urn:ietf:params:xml:ns:netconf:notification:1.0";
    public static final String IETF_NOTIFICATION_NS = "urn:ietf:params:xml:ns:yang:ietf-netconf-notifications";
    public static final String BBF_NOTIFICATION_NS = "urn:broadband-forum-org:yang:bbf-software-image-management";
    public static final String NC_NOTIFICATION_NS = "urn:ietf:params:xml:ns:netmod:notification";
    public static final String NOTIFICATION_BUFFER_NS = "http://www.test-company.com/solutions/anv-notification-buffer";
    public static final String NETCONF_WRITABLE_RUNNNG = "urn:ietf:params:netconf:capability:writable-running:1.0";
    public static final String NETCONF_ROLLBACK_ON_ERROR = "urn:ietf:params:netconf:capability:rollback-on-error:1.0";
    public static final String WITH_DEFAULTS_NS = "urn:ietf:params:xml:ns:yang:ietf-netconf-with-defaults";
    public static final String NOTIFICATION_INTERLEAVE = "urn:ietf:params:netconf:capability:interleave:1.0";
    public static final String NOTIFICATION = "notification";
    public static final String XMLNS = "xmlns";
    public static final String FILTER = "filter";
    public static final String SUBTREE_FILTER = "subtree";
    public static final String DEFAULT_OPERATION = "default-operation";
    public static final String TEST_OPTION = "test-option";
    public static final String ERROR_OPTION = "error-option";
    public static final String DATA_SOURCE = "source";
    public static final String DATA_TARGET = "target";
    public static final String EDIT_CONFIG_OPERATION = "operation";
    public static final String COPY_CONFIG = "copy-config";
    public static final String SRC = "src";
    public static final String DELETE_CONFIG = "delete-config";
    public static final String LOCK = "lock";
    public static final String UNLOCK = "unlock";
    public static final String GET = "get";
    public static final String WITH_DELAY_NS = "http://www.test-company.com/solutions/anv-test-netconf-extensions";
    public static final String WITH_DELAY = "with-delay";
    public static final String EXTENSION_NS = "http://www.test-company.com/solutions/netconf-extensions";
    public static final String NC_STACK_NS = "urn:bbf:yang:obbaa:netconf-stack";
    public static final String SYSTEM_STATE_NS = "urn:ietf:params:xml:ns:yang:ietf-system";
    public static final String SYSTEM_STATE = "system-state";
    public static final String SYSTEM_STATE_NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-system";
    public static final String CLOCK = "clock";
    public static final String SYS_CURRENT_DATE_TIME = "sys:current-datetime";
    public static final String CURRENT_DATE_TIME = "current-datetime";
    public static final String DEPTH = "depth";
    public static final String FIELDS = "fields";
    public static final String DATA_NODE = "data-node";
    public static final String ATTRIBUTE = "attribute";
    public static final String WITH_DEFAULTS = "with-defaults";
    public static final String CLOSE_SESSION = "close-session";
    public static final String KILL_SESSION = "kill-session";
    public static final String SESSION_ID = "session-id";
    public static final String STREAMS = "streams";
    public static final String STREAM = "stream";
    public static final String WRITABLE_RUNNING = ":writable-running";
    public static final String HELLO = "hello";
    public static final String CAPABILITIES = "capabilities";
    public static final String CAPABILITY = "capability";
    public static final String RPC_EOM_DELIMITER = "]]>]]>";
    public static final String RPC_CHUNKED_DELIMITER = "\n##\n";
    public static final String EOM_HANDLER = "EOM_HANDLER";
    public static final String CHUNKED_HANDLER = "CHUNKED_HANDLER";
    public static final String CHUNK_SIZE = "CHUNK_SIZE";
    public static final String MAXIMUM_SIZE_OF_CHUNKED_MESSAGES = "MAXIMUM_SIZE_OF_CHUNKED_MESSAGES";
    public static final String RPC_REPLY = "rpc-reply";
    public static final String OK = "ok";
    public static final String RPC_REPLY_DATA = "data";
    public static final String RPC_ERROR = "rpc-error";
    public static final String RPC_ERROR_TYPE = "error-type";
    public static final String RPC_ERROR_TAG = "error-tag";
    public static final String RPC_ERROR_SEVERITY = "error-severity";
    public static final String RPC_ERROR_PATH = "error-path";
    public static final String RPC_ERROR_MESSAGE = "error-message";
    public static final String RPC_ERROR_APP_TAG = "error-app-tag";
    public static final String RPC_ERROR_INFO = "error-info";
    public static final String NONE = "NONE";
    public static final String URL = "url";
    public static final String NETCONF_SUBSYSTEM_NAME = "netconf";
    public static final int CALL_HOME_IANA_PORT_TLS = 4335;
    public static final Long DEFAULT_CONNECTION_TIMEOUT = 100000L;
    public static final int DEFAULT_SSH_CONNECTION_PORT = 830;
    public static final String COPY_CONFIG_SRC_CONFIG = "config";
    public static final String REQUEST_LOG_STMT = "Got request from %s/%s ( %s ) session-id %s \n %s \n"
            + "---------------------------------";
    public static final String RESPONSE_LOG_STMT = "Sending response to %s/%s ( %s ) session-id %s\n %s \n"
            + "---------------------------------";
    public static final String NOTIFICATION_LOG_STMT = "Got notification for %s stream: \n %s" + "---------------------------------";
    public static final String CREATESUBSCRIPTION_LOG_STMT = "Create subscription request: \n %s" + "---------------------------------";
    public static final String SUFFIX = "_STREAM_LOGGER";
    public static final String UNCLASSIFIED_NOTIFICATIONS = "unclassified notifications";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    public static final String IMPLIED = "implied";

    // This parameter needs to be moved to configuration
    public static int RETRY_LIMIT_REVERSE_SSH = 3;

    public static final String HEARTBEAT_INTERVAL = "hearbeat-interval";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String REPLAY_SUPPORT = "replaySupport";
    public static final String REPLAY_LOG_CREATION_TIME = "replayLogCreationTime";
    public static final String START_TIME = "startTime";
    public static final String STOP_TIME = "stopTime";
    public static final String EVENT_TIME = "eventTime";
    public static final String DATA_STORE = "datastore";
    public static final String CHANGED_BY = "changed-by";
    public static final String USER_NAME = "username";
    public static final String SOURCE_HOST = "source-host";
    public static final String EDIT = "edit";
    public static final String TARGET = "target";
    public static final String OPERATION = "operation";
    public static final String CHANGED_LEAF = "changed-leaf";
    public static final String INSERT = "insert";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String REPLAY_COMPLETE = "replayComplete";
    public static final String NOTIFICATION_COMPLETE = "notificationComplete";
    public static final String CONFIG_CHANGE_NOTIFICATION = "netconf-config-change";
    public static final String STATE_CHANGE_NOTIFICATION = "state-change-notification";
    public static final String NC_STATE_CHANGE_NOTIFICATION = "netconf-state-change";
    public static final String STATE_CHANGE_VALUE = "value";
    public static final String CHANGES = "changes";
    public static final String INTERLEAVE = "interleave";
    public static final String CONFIG_CHANGE_STREAM = "CONFIG_CHANGE";
    public static final String SYSTEM_STREAM = "SYSTEM";
    public static final String OPER_STATE_CHANGE = "oper-state-change";
    public static final String OLD_OPER_STATUS = "old-oper-status";
    public static final String NEW_OPER_STATUS = "new-oper-status";
    public static final String YANG_NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-yang-types";
    /**
     * ietf-yang-types.yang: typedef date-and-time { type string { pattern
     * '\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[\+\-]\d{2}:\d{2})'; }
     */
    public static final DateTimeFormatter DATE_TIME_WITH_TZ = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    public static final DateTimeFormatter DATE_TIME_WITH_TZ_WITHOUT_MS = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
    public static final Pattern DATE_TIME_WITH_TZ_WITH_MS_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)(Z|[\\+\\-]\\d{2}:\\d{2})");
    public static final Pattern DATE_TIME_WITH_TZ_WITHOUT_MS_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(Z|[\\+\\-]\\d{2}:\\d{2})");
    public static final String IMPLICATION_CHANGE = "--automatic--";

    public static final String NC_NBI_CLIENT_IDLE_CONNECTION_TIMEOUT_MS = "NC_NBI_CLIENT_IDLE_CONNECTION_TIMEOUT_MS";

    public static DateTime parseDateTime(String dateTimeStr){
        return ISODateTimeFormat.dateTimeParser().parseDateTime(dateTimeStr);
    }

    public static String printWithoutMillis(DateTime dateTime){
        return DATE_TIME_WITH_TZ_WITHOUT_MS.print(dateTime);
    }

    public static String printWithMillis(DateTime dateTime){
        return DATE_TIME_WITH_TZ.print(dateTime);
    }

}
