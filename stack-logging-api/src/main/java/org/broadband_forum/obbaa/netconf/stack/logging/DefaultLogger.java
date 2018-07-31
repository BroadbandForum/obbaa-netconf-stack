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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

public class DefaultLogger implements InvocationHandler {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DefaultLogger.class);

    private Logger m_slf4jLogger;//NOSONAR

    public DefaultLogger(String category) {
        m_slf4jLogger = org.slf4j.LoggerFactory.getLogger(category);
    }

    @Override
    public Object invoke(Object object, Method method, Object[] args) throws Throwable {
        List<Object> slf4jParams = new ArrayList<>();
        if (args != null) {
            slf4jParams.addAll(Arrays.asList(args));
        }
        Method targetMethod = getTargetMethod(method, method.getParameterTypes());
        return targetMethod.invoke(m_slf4jLogger, slf4jParams.toArray());
    }

    private Method getTargetMethod(Method method, Class<?>[] paramTypes) {
        try {
            return m_slf4jLogger.getClass().getMethod(method.getName(), paramTypes);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Error while logging", e);
            throw new RuntimeException("Error while logging", e);
        }
    }


    void setSlf4jLogger(Logger slf4jLogger) {
        m_slf4jLogger = slf4jLogger;
    }

}
