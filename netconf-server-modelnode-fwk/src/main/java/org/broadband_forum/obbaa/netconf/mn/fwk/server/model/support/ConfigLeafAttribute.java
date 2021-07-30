package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.w3c.dom.Element;

/**
 * Created by sgs on 2/1/17.
 */
public interface ConfigLeafAttribute {
    /**
     * @return Returns the DOM representation of the attribute.
     * For ex:
     * In case of an attribute "<crypto xmlns:des="http://example.com/des">des:des3</crypto>"
     * the value returned would be an Element whose value is "<crypto xmlns:des="http://example.com/des">des:des3</crypto>".
     *
     */
    Element getDOMValue();

    /**
     * @param namespace - namespace
     * @param prefix - prefix
     * @return Returns the DOM representation of the attribute.
     * The prefix in the returned element, will be decided according to the given namespace
     *
     */
    Element getDOMValue(final String namespace, final String prefix);

    /**
     * @return Returns the text content of a attribute.
     * For ex:
     * In case of an attribute "<crypto xmlns:des="http://example.com/des">des:des3</crypto>"
     * the value returned would be "des:des3".
     */
    String getStringValue();

    /**
     * Returns namespace of Identity ref and Instance Identifier
     * For ex:
     * IdentityRef "<child xmlns="childNs" xmlns:p3="valueNs">p3:value</child>" The value returned is valueNs
     * Instance Identifier "<child xmlns="parentNs" xmlns:ex1="value2Ns" xmlns:ex2="value3Ns">/ex1:system/ex2:services/ex2:ssh</child>"
     * The value returned is value2Ns ex1,value3Ns ex2
     * @return
     */
    String getNamespace();

    Integer getInsertIndex();

    void setInsertIndex(Integer insertIndex);

}
