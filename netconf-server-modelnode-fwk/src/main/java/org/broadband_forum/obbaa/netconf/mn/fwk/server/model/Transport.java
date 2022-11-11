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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

public enum Transport {
    
    NETCONF_SSH ("netconf-ssh", 1),
    NETCONF_SOAP_OVER_BEEP("netconf-soap-over-beep", 2),
    NETCONF_SOAP_OVER_HTTPS("netconf-soap-over-https", 3),
    NETCONF_BEEP("netconf-beep", 4),
    NETCONF_TLS("netconf-tls", 5);
    
    private final String name;
    private final int value;
    
    private Transport(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    public String toString() {
        return name;
    }
    
    public int value() {
        return value;
    }
    
    public static Transport getTransport(String sev){
        if(Transport.NETCONF_SSH.toString().equals(sev)){
            return Transport.NETCONF_SSH;
        } else if(Transport.NETCONF_SOAP_OVER_BEEP.toString().equals(sev)){
            return Transport.NETCONF_SOAP_OVER_BEEP;
        } else if(Transport.NETCONF_SOAP_OVER_HTTPS.toString().equals(sev)){
            return Transport.NETCONF_SOAP_OVER_HTTPS;
        } else if(Transport.NETCONF_BEEP.toString().equals(sev)){
            return Transport.NETCONF_BEEP;
        } else if(Transport.NETCONF_TLS.toString().equals(sev)){
            return Transport.NETCONF_TLS;
        } else {
            return null;
        }
    }
}
