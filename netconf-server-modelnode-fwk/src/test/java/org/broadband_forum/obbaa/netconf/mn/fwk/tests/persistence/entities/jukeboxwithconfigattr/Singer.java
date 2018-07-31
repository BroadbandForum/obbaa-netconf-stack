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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxwithconfigattr;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.SingerPK;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangLeafList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity(name = "jukeboxwithconfigattr_singer")
@Table(name = "jukeboxwithconfigattr_singer")
@IdClass(SingerPK.class)
@YangLeafList(name = "singer", namespace = JukeboxConstants.JB_NS, revision = JukeboxConstants.JB_REVISION)
public class Singer {

    @Id
    @Column
    @YangAttribute(name = "singer", namespace = JukeboxConstants.JB_NS, revision = JukeboxConstants.JB_REVISION)
    private String singer;

    @Id
    @YangParentId
    String parentId;

    @YangSchemaPath
    @Column(length = 1000)
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

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Singer singer1 = (Singer) o;

        if (!singer.equals(singer1.singer))
            return false;
        if (!parentId.equals(singer1.parentId))
            return false;
        return schemaPath.equals(singer1.schemaPath);

    }

    @Override
    public int hashCode() {
        int result = singer.hashCode();
        result = 31 * result + parentId.hashCode();
        result = 31 * result + schemaPath.hashCode();
        return result;
    }
}
