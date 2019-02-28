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

package org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangLeafList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity(name = "singer")
@Table(name = "singer")
@IdClass(SingerPK.class)
@YangLeafList(name = "singer", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
public class Singer {
	
	@Id
	@Column
	@YangAttribute(name="singer", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String singer;
	
	@Id
	@YangParentId
	String parentId;
	
	@YangSchemaPath
	@Column(length = 1000)
	String schemaPath;
	
	@Column
	@YangOrderByUser
	private Integer insertOrder;
	
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
	
	public int getInsertOrder() {
		return insertOrder;
	}
	
	public void setInsertOrder(Integer insertOrder) {
		this.insertOrder = insertOrder;
	}
}
