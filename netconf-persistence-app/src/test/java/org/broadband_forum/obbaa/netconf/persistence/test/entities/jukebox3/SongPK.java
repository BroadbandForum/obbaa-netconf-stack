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

import java.io.Serializable;

/**
 * Created by keshava on 5/1/16.
 */
public class SongPK implements Serializable {
   
	private static final long serialVersionUID = -5778512365055298893L;
	String name;
    String parentId;

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        SongPK songPK = (SongPK) o;

        if (name != null ? !name.equals(songPK.name) : songPK.name != null) return false;
        return parentId != null ? parentId.equals(songPK.parentId) : songPK.parentId == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        return result;
    }
}
