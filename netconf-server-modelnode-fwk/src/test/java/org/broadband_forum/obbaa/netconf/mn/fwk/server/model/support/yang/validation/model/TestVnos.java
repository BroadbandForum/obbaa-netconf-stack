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

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants;

@YangContainer(name = "test-vnos", namespace = ValidationConstants.FANS_VALIDATION_TEST_NS, revision = ValidationConstants.FANS_VALIDATION_TEST_REVISION)
@Entity
public class TestVnos implements Serializable {

    private static final long serialVersionUID = -5871948078355078900L;

    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangChild
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TestVno> testVno = new LinkedHashSet<TestVno>();

    @YangXmlSubtree
    @Column(length = 100000)
    private String xmlSubTree;

    public TestVnos() {
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public Set<TestVno> getTestVno() {
        return testVno;
    }

    public void setTestVno(Set<TestVno> testVno) {
        this.testVno = testVno;
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
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        result = prime * result + ((testVno == null) ? 0 : testVno.hashCode());
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
        TestVnos other = (TestVnos) obj;
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        if (schemaPath == null) {
            if (other.schemaPath != null)
                return false;
        } else if (!schemaPath.equals(other.schemaPath))
            return false;
        if (testVno == null) {
            if (other.testVno != null)
                return false;
        } else if (!testVno.equals(other.testVno))
            return false;
        if (xmlSubTree == null) {
            if (other.xmlSubTree != null)
                return false;
        } else if (!xmlSubTree.equals(other.xmlSubTree))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TestVnos [parentId=" + parentId + ", schemaPath=" + schemaPath + ", testVno=" + testVno
                + ", xmlSubTree=" + xmlSubTree + "]";
    }
}
