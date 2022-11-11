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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class HintDetails {

    private SchemaPath m_referringSP;
    private Expression m_targetNodeExpression;
    private Map<SchemaPath, ValidationHint> m_referredSPToHints = new HashMap<>();
    private boolean m_isSkipValidation;
    
    public SchemaPath getReferringSP() {
        return m_referringSP;
    }
    
    public void setReferringSP(SchemaPath referringSP) {
        this.m_referringSP = referringSP;
    }
    
    public Expression getTargetNodeExpression() {
        return m_targetNodeExpression;
    }
    
    public void setTargetNodeExpression(Expression targetNodeExpression) {
        this.m_targetNodeExpression = targetNodeExpression;
    }
    
    public Map<SchemaPath, ValidationHint> getReferredSPToHints() {
        return m_referredSPToHints;
    }
    
    public void setReferredSPToHints(Map<SchemaPath, ValidationHint> referredSPToHints) {
        this.m_referredSPToHints = referredSPToHints;
    }
    
    public boolean isSkipValidation() {
        return m_isSkipValidation;
    }
    
    public void setIsSkipValidation(boolean isSkipValidation) {
        this.m_isSkipValidation = isSkipValidation;
    }
}
