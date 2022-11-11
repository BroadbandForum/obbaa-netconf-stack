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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.CROSS_TEST_NS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.CROSS_TEST_REVISION;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;

@YangContainer(name="CrossTest", namespace = CROSS_TEST_NS , revision=CROSS_TEST_REVISION)
@Entity
public class CrossTest implements Serializable{

    private static final long serialVersionUID = 2348850324905673607L;
    
    @Id
    @YangParentId
    @Column(name=YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangXmlSubtree
    @Column(length=100000)
    private String xmlSubTree;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;
    
    
    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getXmlSubTree() {
        return xmlSubTree;
    }

    public void setXmlSubTree(String xmlSubTree) {
        this.xmlSubTree = xmlSubTree;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((xmlSubTree == null) ? 0 : xmlSubTree.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CrossTest other = (CrossTest) obj;
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        if (xmlSubTree == null) {
            if (other.xmlSubTree != null)
                return false;
        } else if (!xmlSubTree.equals(other.xmlSubTree))
            return false;
        return true;
    }

    public CrossTest() {
        super();
    }

    @Override
    public String toString() {
        return "CrossTest [parentId=" + parentId + ", xmlSubTree=" + xmlSubTree + "]";
    }


}
