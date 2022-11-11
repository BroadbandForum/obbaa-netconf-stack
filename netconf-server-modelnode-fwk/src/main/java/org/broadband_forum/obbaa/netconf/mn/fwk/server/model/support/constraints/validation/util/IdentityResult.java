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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IdentityResult {
    Map<ExpectedIdentity, Map<ActualIdentity, Map<DerFromOrSelf, Boolean>>> m_map = new HashMap<>();

    public Map<DerFromOrSelf, Boolean> getResult(String expectedBaseIdentity, Object targetValue) {
        Map<ActualIdentity, Map<DerFromOrSelf, Boolean>> identityResult = getExpectedIdentityReusult(expectedBaseIdentity);
        ActualIdentity actualIdentity = new ActualIdentity(targetValue);
        Map<DerFromOrSelf, Boolean> result = getResult(identityResult, actualIdentity);
        return result;
    }

    public Map<DerFromOrSelf, Boolean> getResult(Map<ActualIdentity, Map<DerFromOrSelf, Boolean>> identityResult, ActualIdentity actualIdentity) {
        Map<DerFromOrSelf, Boolean> selfBooleanMap = identityResult.get(actualIdentity);
        if(selfBooleanMap == null){
            selfBooleanMap = new HashMap<>();
            identityResult.put(actualIdentity, selfBooleanMap);
        }
        return selfBooleanMap;
    }

    private Map<ActualIdentity, Map<DerFromOrSelf, Boolean>> getExpectedIdentityReusult(String expectedBaseIdentity) {
        ExpectedIdentity expectedIdentity = new ExpectedIdentity(expectedBaseIdentity);
        Map<ActualIdentity, Map<DerFromOrSelf, Boolean>> result = m_map.get(expectedIdentity);
        if(result == null){
            result = new HashMap<>();
            m_map.put(expectedIdentity, result);
        }
        return result;
    }

    public class ExpectedIdentity {
        final String m_expectedIdentity;

        private ExpectedIdentity(String expectedIdentity) {
            m_expectedIdentity = expectedIdentity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExpectedIdentity that = (ExpectedIdentity) o;
            return Objects.equals(m_expectedIdentity, that.m_expectedIdentity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(m_expectedIdentity);
        }

        @Override
        public String toString() {
            return "ExpectedIdentity{" + m_expectedIdentity +'}';
        }
    }

    public class ActualIdentity {
        final Object m_actualIdentity;

        private ActualIdentity(Object actualIdentity) {
            m_actualIdentity = actualIdentity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ActualIdentity that = (ActualIdentity) o;
            return Objects.equals(m_actualIdentity, that.m_actualIdentity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(m_actualIdentity);
        }

        @Override
        public String toString() {
            return "ActualIdentity{" + m_actualIdentity  + '}';
        }
    }

    public enum DerFromOrSelf {
        derivedFromOrSelf, derivedFrom;

        public static DerFromOrSelf from(boolean canBeBaseIdentity) {
            if (canBeBaseIdentity) {
                return derivedFromOrSelf;
            }
            return derivedFrom;
        }
    }
}
