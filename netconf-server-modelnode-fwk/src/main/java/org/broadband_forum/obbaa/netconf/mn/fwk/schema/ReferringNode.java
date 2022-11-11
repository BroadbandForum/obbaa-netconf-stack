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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils.getExpression;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class ReferringNode {
    private boolean m_keyLeaf;
    private final SchemaPath m_referredSP;
    private final SchemaPath m_referringSP;
    private final Expression m_referringNodeAP;
    private ReferenceType m_referenceType;
    private ValidationHint m_validationHint = null;
    private String m_simpleReferredSP;
    private boolean m_isReferredNodeIsUnderChangedNode = false;
    private String m_constraintXPathString;
    //for the ut
    public static Set<ReferringNode> c_nodesUnderAugmentedNode = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static boolean c_capture = false;
    private boolean m_autoHint = false;
    private DataSchemaNode m_referredSchemaNode;
    private DataSchemaNode m_referringSchemaNode;

    public ReferringNode(boolean keyLeaf, SchemaPath referredSP, SchemaPath referringSP, String referringNodeAP) {
        m_keyLeaf = keyLeaf;
        m_referredSP = referredSP;
        m_referringSP = referringSP;
        m_referringNodeAP = getExpression(referringNodeAP);
        prepareSimpleReferredSP();
    }

    public ReferringNode(SchemaPath referredSP, SchemaPath referringSP, String referringNodeAP) {
        this(false, referredSP, referringSP, referringNodeAP);
    }

    public SchemaPath getReferredSP() {
        return m_referredSP;
    }

    public SchemaPath getReferringSP() {
        return m_referringSP;
    }

    public Expression getReferringNodeAP() {
        return m_referringNodeAP;
    }

    public boolean isKeyLeaf() {
        return m_keyLeaf;
    }

    public void setKeyLeaf(boolean keyLeaf) {
        m_keyLeaf = keyLeaf;
    }

    @Override
    public String toString() {
        return "ReferringNode{" +
                "m_keyLeaf=" + m_keyLeaf +
                ", m_referredSP=" + m_referredSP +
                ", m_referringSP=" + m_referringSP +
                ", m_referringNodeAP=" + m_referringNodeAP +
                ", m_referenceType=" + m_referenceType +
                ", m_validationHint=" + m_validationHint +
                ", m_simpleReferredSP='" + m_simpleReferredSP + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferringNode that = (ReferringNode) o;
        return m_keyLeaf == that.m_keyLeaf &&
                Objects.equals(m_referredSP, that.m_referredSP) &&
                Objects.equals(m_referringSP, that.m_referringSP) &&
                Objects.equals(m_referringNodeAP, that.m_referringNodeAP) &&
                Objects.equals(m_referenceType,that.m_referenceType) &&
                Objects.equals(m_validationHint,that.m_validationHint) &&
                Objects.equals(m_simpleReferredSP, that.m_simpleReferredSP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_keyLeaf, m_referredSP, m_referringSP, m_referringNodeAP, m_referenceType, m_validationHint, m_simpleReferredSP);
    }

    public void setReferenceType(ReferenceType referenceType) {
        m_referenceType = referenceType;
    }

    public ReferenceType getReferenceType() {
        return m_referenceType;
    }

    public ValidationHint getValidationHint() {
        return m_validationHint;
    }

    public void setValidationHint(ValidationHint validationHint) {
        m_validationHint = validationHint;
    }

    public void parseValidationHint(String hint) {
        if(hint != null){
            setValidationHint(ValidationHint.valueOf(hint));
        }
    }

    public void prepareSimpleReferredSP() {
        StringBuilder sb = new StringBuilder();
        for (QName pathPart : m_referredSP.getPathFromRoot()) {
            sb.append("/").append(pathPart.getLocalName());
        }
        m_simpleReferredSP = sb.toString();
    }

    public String getSimpleReferredSP() {
        return m_simpleReferredSP;
    }

    public void setReferredNodeIsUnderChangedNode(boolean referredNodeChangedNode) {
        m_isReferredNodeIsUnderChangedNode = referredNodeChangedNode;
        if(c_capture && referredNodeChangedNode) {
            c_nodesUnderAugmentedNode.add(this);
        }
    }

    public boolean isReferredNodeIsUnderChangedNode() {
        return m_isReferredNodeIsUnderChangedNode;
    }

    public void parseReferringNodeIsUnderChangedNodeHints(String expressionStr) {
        if(expressionStr != null) {
            if(getExpression(expressionStr).toString().equals(getExpression(m_constraintXPathString).toString())){
                setReferredNodeIsUnderChangedNode(true);
            }
        }
    }

    public void setConstraintXPath(String constraintXPathString) {
        m_constraintXPathString = constraintXPathString;
    }

    public String getConstraintXPath() {
        return m_constraintXPathString;
    }

    public void setReferredSchemaNode(DataSchemaNode referredSchemaNode) {
        m_referredSchemaNode = referredSchemaNode;
    }

    public DataSchemaNode getReferredSchemaNode() {
        return m_referredSchemaNode;
    }

    public void setReferringSchemaNode(DataSchemaNode referringSchemaNode) {
        m_referringSchemaNode = referringSchemaNode;
    }

    public DataSchemaNode getReferringSchemaNode() {
        return m_referringSchemaNode;
    }

    public enum ReferenceType {
        LEAFREF, MUST, WHEN, WHEN_ON_AUGMENT;
    }

}
