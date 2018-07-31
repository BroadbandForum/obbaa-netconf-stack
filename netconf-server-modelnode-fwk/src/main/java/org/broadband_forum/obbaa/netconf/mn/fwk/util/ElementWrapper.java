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

package org.broadband_forum.obbaa.netconf.mn.fwk.util;

import org.w3c.dom.Element;

/**
 * This is a util class used mainly to wrap an Element instance for JXPath consumption.
 * <p>
 * During a usage of ExtensionFunctions in JXPath, setting a Element directly triggers
 * internal implementation. To avoid the same and use Element as input, this wrapper util is used.
 */
public class ElementWrapper {
    private Element element;

    public ElementWrapper(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    public static Object current(ElementWrapper element) {
        return element.getElement();
    }

    public static String currentElementText(ElementWrapper element) {
        return element.getElement().getTextContent();
    }

    public static ElementWrapper wrapElement(Element element) {
        return new ElementWrapper(element);
    }

}
