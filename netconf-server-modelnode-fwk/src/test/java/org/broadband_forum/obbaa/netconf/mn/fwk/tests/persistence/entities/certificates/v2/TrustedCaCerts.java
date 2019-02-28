package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.v2;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshava on 5/12/15.
 */
@Entity
@Table(name = "CaCertificates")
@YangList(name="trusted-ca-certs", namespace = "test:certificates", revision="2015-12-08")
public class TrustedCaCerts {

    @OneToMany(cascade = CascadeType.ALL)
    @YangChild
    private List<Certificate> certificates = new ArrayList<>();

    @Id
    @YangParentId
    private String parentId;

    @YangSchemaPath
    @Column
    String schemaPath;

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


    public List<Certificate> getCertificates() {
        return certificates;
    }

    public void addCertificates(Certificate certificate) {
        certificates.add(certificate);
    }

    public void setCertificates(List<Certificate> certificates) {
        this.certificates = certificates;
    }
}
