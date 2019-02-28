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

package org.broadband_forum.obbaa.netconf.stack.logging;

import java.lang.reflect.Proxy;

public class LoggerFactory {
    private static AdvancedLoggerFactory LOGGER_FACTORY = new DefaultLoggerFactoryImpl();

    public static AdvancedLogger getLogger(String category, String application, String logType, String logScope) {
        return LOGGER_FACTORY.getLogger(category, application, logType, logScope);
    }

    public static void setLoggerFactory(AdvancedLoggerFactory advancedLogger) {
        LoggerFactory.LOGGER_FACTORY = advancedLogger;
    }
    public static AdvancedLogger getLogger(Class klass, String application, String logType, String logScope) {

        return LOGGER_FACTORY.getLogger(klass, application, logType, logScope);
    }

    public static void restLoggerFactory() {
        LOGGER_FACTORY = new DefaultLoggerFactoryImpl();
    }

    static class DefaultLoggerFactoryImpl implements AdvancedLoggerFactory {
        @Override
        public AdvancedLogger getLogger(String category, String application, String logType, String logScope) {
            return (AdvancedLogger) Proxy.newProxyInstance(AdvancedLogger.class.getClassLoader(),
                    new Class[] { AdvancedLogger.class }, new DefaultLogger(category));
        }

        @Override
        public AdvancedLogger getLogger(Class klass, String application, String logType, String logScope) {
            return getLogger(klass.getName(), application, logType, logScope);
        }
    }
}
