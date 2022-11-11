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

public class DefaultUALLoggerImpl implements UALLogger {
    @Override
    public void log(String invocationTime, String user, String session, String application, String operation, Optional<String> arguments,
             Optional<String> payload, Optional<String> result, Optional<String> delegateUser, Optional<String> delegateSession) {

    }

    @Override
    public boolean canLog() {
        return false;
    }

    @Override
    public void setCanLog(boolean shouldLog) {

    }

    @Override
    public boolean isUalLogEnabled() {
        return false;
    }

	@Override
	public void resetCanLog() {
		
	}
}
