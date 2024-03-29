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

package org.broadband_forum.obbaa.netconf.api.messages;

public interface EditConfigOperations {
    public static final String MERGE = "merge";
    public static final String REPLACE = "replace";
    public static final String CREATE = "create";
    public static final String DELETE = "delete";
    public static final String REMOVE = "remove";
    public static final String NONE = "none";
    
    public static boolean isOperationDeleteOrRemove(String operation) {
        return REMOVE.equals(operation) || DELETE.equals(operation);
    }
}
