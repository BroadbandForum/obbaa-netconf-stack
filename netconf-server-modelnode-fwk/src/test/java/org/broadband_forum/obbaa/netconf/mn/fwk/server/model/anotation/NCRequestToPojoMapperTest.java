package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.RpcArgsInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.RpcArgumentInfo;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.Rpc;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.RpcArg;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by vishal on 18/8/16.
 */
public class NCRequestToPojoMapperTest {
    private static final String RPC_STRING = "<eat-out xmlns=\"http://example.com/ns/eat-out\">\n" +
            "    <restaurant-type>chinese</restaurant-type>\n" +
            "    <restaurant-address>\n" +
            "        <name>3 quarters chinese</name>\n" +
            "        <street>MG Road</street>\n" +
            "    </restaurant-address>\n" +
            "</eat-out>";
    public static final String RESTAURANT_TYPE = "restaurant-type";
    public static final String RESTAURANT_ADDRESS = "restaurant-address";
    public static final String EAT_OUT_NS = "http://example.com/ns/eat-out";
    NCRequestToPojoMapper m_mapper = new JaxbNCRequestToPojoMapper();
    private RpcArgsInfo m_eatOutRpcArgsInfo;
    private RestaurantAddress m_expectedRestAddress;

    @Before
    public void setUp(){
        m_eatOutRpcArgsInfo = new RpcArgsInfo();
        List<RpcArgumentInfo> rpcArgs = new ArrayList<>();
        m_eatOutRpcArgsInfo.setRpcArgsInfo(rpcArgs);

        rpcArgs.add(new RpcArgumentInfo(String.class, RESTAURANT_TYPE, EAT_OUT_NS));
        rpcArgs.add(new RpcArgumentInfo(RestaurantAddress.class, RESTAURANT_ADDRESS, EAT_OUT_NS));
        m_expectedRestAddress = new RestaurantAddress();
        m_expectedRestAddress.name = "3 quarters chinese";
        m_expectedRestAddress.street = "MG Road";
    }


    @Test
    @Ignore("ignoring the ut until we solve JAXB problem in open-jdk 1.8.0_111")
    public void testMapping() throws NetconfMessageBuilderException, NCRequestToPojoMapperException {
        NetconfRpcRequest rpc = new NetconfRpcRequest();
        rpc.setRpcInput(DocumentUtils.stringToDocument(RPC_STRING).getDocumentElement());
        List<Object> args = m_mapper.getRpcArguments(rpc, m_eatOutRpcArgsInfo);
        assertEquals(2, args.size());
        assertEquals("chinese", args.get(0));
        assertEquals(m_expectedRestAddress, args.get(1));
    }


    static class EatOutRpcHandler {

        @Rpc(value = "eat-out", namespace = EAT_OUT_NS)
        public void eatOut(@RpcArg(RESTAURANT_TYPE) String restaurantType, @RpcArg(RESTAURANT_ADDRESS) RestaurantAddress address){
        }
    }

    @XmlRootElement(name = RESTAURANT_ADDRESS, namespace = EAT_OUT_NS)
    static class RestaurantAddress {
        @XmlElement(name = "name", required = true)
        String name;
        @XmlElement(name = "street", required = true)
        String street;

        @Override
        public String toString() {
            return "RestaurantAddress{" +
                    "name='" + name + '\'' +
                    ", street='" + street + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RestaurantAddress that = (RestaurantAddress) o;

            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            return street != null ? street.equals(that.street) : that.street == null;

        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (street != null ? street.hashCode() : 0);
            return result;
        }
    }
}
