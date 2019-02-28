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

package org.broadband_forum.obbaa.netconf.stack;

import java.util.Collection;

/**
 * An interface to to record and retrieve request counters in NCY Stack.
 *
 * Created by keshava on 2/23/16.
 */
public interface NcCounterService {
    public static final String NUMBER_OF_GET_REQUESTS = "number-of-get-requests";
    public static final String NUMBER_OF_GET_CONFIG_REQUESTS = "number-of-get-config-requests";
    public static final String NUMBER_OF_EDIT_CONFIG_REQUESTS = "number-of-edit-config-requests";
    public static final String NUMBER_OF_RPC_REQUESTS = "number-of-rpc-requests";

    public static final String NUMBER_OF_RESTCONF_GET_REQUESTS = "number-of-restconf-get-requests";
    public static final String NUMBER_OF_RESTCONF_POST_REQUESTS = "number-of-restconf-post-requests";
    public static final String NUMBER_OF_RESTCONF_PUT_REQUESTS = "number-of-restconf-put-requests";
    public static final String NUMBER_OF_RESTCONF_PATCH_REQUESTS = "number-of-restconf-patch-requests";
    public static final String NUMBER_OF_RESTCONF_DELETE_REQUESTS = "number-of-restconf-delete-requests";
    
    public static final String IN_RPCS = "in-rpcs";
    public static final String OUT_RPC_ERRORS = "out-rpc-errors";
    public static final String IN_BAD_RPCS = "in-bad-rpcs";

    /**
     * Add a username whose requests are not to be included in counters.
     * Typically internal system calls come under this category.
     * @param username
     */
    void addInternalUser(String username);

    void removeInternalUser(String username);

    Collection<String> getInternalUsers();

    long getNumberOfGetRequests();

    void increaseNumberOfGetRequests();

    long getNumberOfGetConfigRequests();

    void increaseNumberOfGetConfigRequests();

    long getNumberOfEditConfigRequests();

    void increaseNumberOfEditConfigRequests();

    long getNumberOfRpcRequests();

    void increaseNumberOfRpcRequests();

    long getNumberOfRestconfGetRequests();

    void increaseNumberOfRestconfGetRequests();

    long getNumberOfRestconfPostRequests();

    void increaseNumberOfRestconfPostRequests();  
   
    long getNumberOfRestconfPutRequests();

    void increaseNumberOfRestconfPutRequests();
    
    long getNumberOfRestconfPatchRequests();

    void increaseNumberOfRestconfPatchRequests();
    
    long getNumberOfRestconfDeleteRequests();

    void increaseNumberOfRestconfDeleteRequests();
    
    long getInRpcs(Integer sessionId);

	void increaseInRpcs();
	
	long getInBadRpcs(Integer sessionId);

    void increaseInBadRpcs();

	long getOutRpcErrors(int sessionId);

	void increaseOutRpcErrors();
	
}
