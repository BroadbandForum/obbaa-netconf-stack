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

package org.broadband_forum.obbaa.netconf.stack.api.annotations;

/**
 * GENERIC_CONFIG_ATTRIBUTE → this is to be used for all types except IdentityRef OR Instance identifier
 *
 * IDENTITY_REF_CONFIG_ATTRIBUTE → this is to be used for IdentityRef
 *
 * INSTANCE_IDENTIFIER_CONFIG_ATTRIBUTE -> This is to be used for Instance identifier
 */
public enum AttributeType {
    GENERIC_CONFIG_ATTRIBUTE, IDENTITY_REF_CONFIG_ATTRIBUTE, INSTANCE_IDENTIFIER_CONFIG_ATTRIBUTE
}
