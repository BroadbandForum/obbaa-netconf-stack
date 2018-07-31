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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * YangContainer Annotation is used to mark on the entity that
 * represents container type in the yang model.
 * <pre>
 * Example:-
 * {@code
 *  {@literal @}YangContainer(name="jukebox", namespace = "http://example.com/ns/example-jukebox",
 *  revision="2014-07-03")
 *  public class Jukebox  {
 *  ...
 *  }}
 * </pre>
 * Created by keshava on 4/12/15.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface YangContainer {
    String name() default "";

    String namespace() default "";

    String revision() default "";
}
