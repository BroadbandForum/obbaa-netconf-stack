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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.v3;

import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.CertificatePk;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * Created by keshava on 5/12/15.
 */
@Entity(name = "V3Certificate")
@IdClass(CertificatePk.class)
@Table(name = "Certificatev3")
@YangList(name = "certificate", namespace = "test:v3-pma-certificates", revision = "2015-12-08")
public class V3Certificate {
    @Id
    @YangListKey(name = "id")
    private String id;

    @Column
    @YangAttribute(name = "cert-binary")
    private String cerBinary;

    @Id
    @Column
    @YangParentId
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    String schemaPath;

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCerBinary() {
        return cerBinary;
    }

    public void setCerBinary(String cerBinary) {
        this.cerBinary = cerBinary;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
