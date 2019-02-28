package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.StringToObjectTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.TransformerException;

/**
 * Created by vgotagi on 10/19/16.
 */
public class AnnotationConfigAttributeHelperTest {
    private Method m_method;
    private ModelNode m_node =  mock(ModelNode.class);
    private URI m_uri = URI.create("");
    private QName m_qName = QName.create(m_uri, (Revision)null, "en");

    @Before
    public void setUp() throws Exception{
        m_method = Class.forName(ModelNode.class.getName()).getMethod("getModelNodeId");
    }

    @Test(expected = GetAttributeException.class)
    public void testException() throws Exception{
        when(m_node.getModelNodeId()).thenThrow(new  IllegalArgumentException("test"));
        AnnotationConfigAttributeHelper helper = new AnnotationConfigAttributeHelper(m_method);
        helper.getValue(m_node);
    }


    @Test(expected = SetAttributeException.class)
    public void testSetAttrException() throws Exception{
        AnnotationConfigAttributeHelper helper = new AnnotationConfigAttributeHelper(m_method);
        helper.setValue(m_node,new GenericConfigAttribute(m_qName.getLocalName(), m_qName.getNamespace().toString(), "value"));
    }

    @Test(expected = SetAttributeException.class)
    public void testSetValue() throws Exception{
        Method getterMethod = Class.forName(ModelNode.class.getName()).getMethod("getValue");
        Method setterMethod = Class.forName(ModelNode.class.getName()).getMethod("setValue", Object.class);
        AnnotationConfigAttributeHelper helper = new AnnotationConfigAttributeHelper(getterMethod,setterMethod);
        helper.setValue(m_node,new GenericConfigAttribute(m_qName.getLocalName(), m_qName.getNamespace().toString(), "value"));
    }

    @Test
    public void testDefault() throws Exception{
        AnnotationConfigAttributeHelper helper = new AnnotationConfigAttributeHelper(m_method);
        assertNull(helper.getDefault());
    }

    @Test(expected = TransformerException.class)
    public void testTransform() throws Exception{
        StringToObjectTransformer.transform(m_qName,"value",Object.class);
    }

    @Test(expected = TransformerException.class)
    public void testTransformBigIntegr() throws Exception{
        StringToObjectTransformer.transform(m_qName,"value",BigInteger.class);
    }


}
