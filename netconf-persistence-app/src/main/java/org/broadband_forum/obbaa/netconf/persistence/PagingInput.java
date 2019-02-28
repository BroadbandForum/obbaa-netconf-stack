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

package org.broadband_forum.obbaa.netconf.persistence;

public class PagingInput {
	private int m_firstResult;
	private int m_maxResult;
	
	public PagingInput(int firstResult, int maxResult) {
		m_firstResult = firstResult;
		m_maxResult = maxResult;
	}

	public int getFirstResult() {
		return m_firstResult;
	}

	public void setFirstResult(int firstResult) {
		m_firstResult = firstResult;
	}

	public int getMaxResult() {
		return m_maxResult;
	}

	public void setMaxResult(int maxResult) {
		m_maxResult = maxResult;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_firstResult;
		result = prime * result + m_maxResult;
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
		PagingInput other = (PagingInput) obj;
		if (m_firstResult != other.m_firstResult)
			return false;
		if (m_maxResult != other.m_maxResult)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PagingInput [m_firstResult=" + m_firstResult + ", m_maxResult="
				+ m_maxResult + "]";
	}


	
}
