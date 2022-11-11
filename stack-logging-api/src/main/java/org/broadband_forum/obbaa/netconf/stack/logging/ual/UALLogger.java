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

package org.broadband_forum.obbaa.netconf.stack.logging.ual;

import java.util.Optional;

public interface UALLogger {

    public static final Optional<String> SUCCESS = Optional.of("success");
    public static final Optional<String> FAILURE = Optional.of("failure");

    void log(String invocationTime, String user, String session, String application, String operation,
             Optional<String> arguments, Optional<String> payload, Optional<String> result,
             Optional<String> delegateUser, Optional<String> delegateSession);

    boolean canLog();

    void setCanLog(boolean shouldLog);

    boolean isUalLogEnabled();
    
    void resetCanLog();
}
