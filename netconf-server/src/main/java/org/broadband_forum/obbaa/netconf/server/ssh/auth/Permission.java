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

package org.broadband_forum.obbaa.netconf.server.ssh.auth;

public class Permission {
	
	private Logical m_logical = Logical.OR;
	private String[] m_permissions;
	
	public Permission(Logical logical, String... permissions) {
		this.m_logical = logical;
		this.m_permissions = permissions;
	}
	
	public Permission(String... permissions) {
		this.m_permissions = permissions;
	}

	public Logical getLogical() {
		if (m_logical == null) {
			return Logical.OR;
		}
		return m_logical;
	}

	public void setLogical(Logical logical) {
		this.m_logical = logical;
	}

	public String[] getPermissions() {
		return m_permissions;
	}

	public void setPermissions(String[] permissions) {
		this.m_permissions = permissions;
	}
	
	
	

}
