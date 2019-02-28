package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.v1;

import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.CertificatePk;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * Created by keshava on 5/12/15.
 */
@Entity(name = "Certificatev1")
@IdClass(CertificatePk.class)
@Table(name = "Certificatev1")
@YangList(name="certificate", namespace = "test:certificates", revision="2015-12-08")
public class Certificate{
    @Id
    @YangListKey(name="id")
    private String id;

    @Column
    @YangAttribute(name="cert-bianry")
    private String cerBinary;

    @Id
    @Column
    @YangParentId
    private String parentId;

    public void setParentId(String parentType) {
        this.parentId = parentType;
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
}
