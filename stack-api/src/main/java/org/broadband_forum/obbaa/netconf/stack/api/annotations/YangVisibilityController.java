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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * YangVisibilityController Annotation is used to annotate a field which will be used to determine the visibility of
 * each instance of an entity.
 *
 * By default all instances are treated as visible. However if some entity instances need visibility control, then a
 * new field should be added to that entity to store visibility value and annotate that field with this annotation.
 *
 * Example:-
 * {@code
 * {@literal @}@YangList(name = "device", namespace = PmaNamespaceConstants.DEVICE_HOLDER_NAMESPACE,
 * revision = YangRevisionConstants.ANV_DEVICE_HOLDERS_YANG_REVISION)
 * public class Device  {
 *
 *    @YangVisibilityController
 *    @Column(name = "visibility")
 *    private boolean visibility = true;
 * }}
 */
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface YangVisibilityController {
}
