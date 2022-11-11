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

package org.broadband_forum.obbaa.netconf.api.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;

public class NetconfResponseFuture extends CompletableFuture<NetConfResponse> {
    private final long m_messageTimeOut;
    private final TimeUnit m_timeUnit;
    private final long m_startTime;
    private boolean m_evaluated = false;

    public NetconfResponseFuture(long messageTimeOut, TimeUnit timeUnit) {
        m_messageTimeOut = messageTimeOut;
        m_timeUnit = timeUnit;
        m_startTime = System.currentTimeMillis();
    }

    public NetconfResponseFuture() {
        this(0, TimeUnit.SECONDS);
    }

    /**
     * Returns completed future with response as the result(value)
     * @param response the result response
     */
    public static NetconfResponseFuture completedNetconfResponseFuture(NetConfResponse response) {
        NetconfResponseFuture future = new NetconfResponseFuture();
        future.complete(response);
        return future;
    }

    /** Waits if necessary for at most the given time from the time the request was sent to request timeout
     *  for this future to complete, and then returns its result, if available.
     *  @return the result value, else null if TimeoutException encountered
     */
    @Override
    public NetConfResponse get() throws InterruptedException, ExecutionException {
        try {
            return super.get(m_messageTimeOut - elapsedTime(), m_timeUnit);
        } catch (TimeoutException e) {
            return null;
        }
    }

    /** Returns the result value (or throws any encountered exception)
     * if completed, else returns null.
     * @return the result value, if completed, else null is returned
     */
    public NetConfResponse getNow() throws InterruptedException, ExecutionException {
        return super.getNow(null);
    }

    /** Checks if the future has been timed out from the time the request was sent to the request timeout
     *  @return true if timed-out else return false.
     */
    public boolean timedOut() {
        if(m_messageTimeOut > 0){
            return elapsedTime() > m_messageTimeOut;
        }
        return false;
    }

    private long elapsedTime() {
        return System.currentTimeMillis() - m_startTime;
    }

    public long getStartTimeMillis() {
        return m_startTime;
    }

    public boolean isEvaluated() {
        return m_evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        m_evaluated = evaluated;
    }

    public long getMessageTimeOut() {
        return m_messageTimeOut;
    }
}
