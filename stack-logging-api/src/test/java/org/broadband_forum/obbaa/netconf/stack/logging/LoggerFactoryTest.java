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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 10/30/17.
 */
public class LoggerFactoryTest {
    @Mock
    private AdvancedLoggerFactory m_advancedLogger;
    @Mock
    private AdvancedLogger m_logger;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(m_advancedLogger.getLogger(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers
                .anyString())).thenReturn(m_logger);
    }

    @Test
    public void testLoggersAreSuppliedWhenFactoryIdNotSet() {
        AdvancedLogger logger = LoggerFactory.getLogger(LoggerFactoryTest.class.getName(), "unit-test", "debug",
                "global");
        assertNotNull(logger);
        Assert.assertNotEquals(m_logger, logger);
    }

    @Test
    public void testAdvancedLoggerFactoryIsCalled() {
        LoggerFactory.setLoggerFactory(m_advancedLogger);
        AdvancedLogger logger = LoggerFactory.getLogger(LoggerFactoryTest.class.getName(), "unit-test", "debug",
                "global");
        Assert.assertEquals(m_logger, logger);
        verify(m_advancedLogger).getLogger(LoggerFactoryTest.class.getName(), "unit-test", "debug", "global");
    }

    @Test
    public void testResetFactoryWorks() {
        LoggerFactory.setLoggerFactory(m_advancedLogger);
        LoggerFactory.restLoggerFactory();
        AdvancedLogger logger = LoggerFactory.getLogger(LoggerFactoryTest.class.getName(), "unit-test", "debug",
                "global");
        Assert.assertNotNull(logger);
        Assert.assertNotEquals(m_logger, logger);
    }
}
