package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.math.BigInteger;

import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

public class StringToObjectTransformer {
	public static Object transform(String strValue, Class<?> class1) throws TransformerException {
		return transform(null,strValue,class1);
	}
	
	public static Object transform(QName qName, String strValue, Class<?> class1) throws TransformerException {
		if (class1 == null){
			// We do not have a specific type parser. We do not validate. 
			return strValue;
		}
		if(class1.equals(Integer.class) || class1.equals(Integer.TYPE)){
			return transformToInteger(strValue);			
		}
		if(class1.equals(Long.class) || class1.equals(Long.TYPE)){
			return transformToLong(strValue);	
		}
		if(class1.equals(String.class) || class1.equals(Byte[].class) || class1.equals(Double.class)){
			return strValue;
		}
		if(class1.equals(Short.class) || class1.equals(Short.TYPE)){
			return transformToShort(strValue);	
		}
		if(class1.equals(BigInteger.class)) {
			return transformToBigInteger(strValue);
		}
		if(class1.equals(Byte.class) || class1.equals(Byte.TYPE)) {
			return transformToByte(strValue);	
		}
		if(class1.equals(Boolean.class) || class1.equals(Boolean.TYPE)) {
			return Boolean.valueOf(strValue);
		}
		if(class1.equals(QName.class)) {
			//FIXME FNMS-10113 should resolve prefix into namespace
			String identityName = null;
			int index = strValue.indexOf(':');
			if (index >= 0) {
				identityName = strValue.substring(index + 1);
			} else {
				identityName = strValue;
			}
			if (qName != null){
				return QName.create(qName, identityName);
			}

		}
		throw new TransformerException("No transoformer found for : "+class1);
	}

	private static BigInteger transformToBigInteger(String strValue) throws TransformerException{
		try { 
			return new BigInteger(strValue);
		} catch (NumberFormatException e) {
			NetconfRpcError rpcError = buildNetconfRpcError("Invalid value : " + strValue + ", it should be a BigInteger");
			throw new TransformerException(rpcError);
		}
	}

	private static Byte transformToByte(String strValue) throws TransformerException {
		try { 
			return Byte.valueOf(strValue);
		} catch (NumberFormatException e) {
			NetconfRpcError rpcError = buildNetconfRpcError("Invalid value : " + strValue + ", it should be a Byte with range from " + Byte.MIN_VALUE + " to " + Byte.MAX_VALUE);
			throw new TransformerException(rpcError);
		}
	}

	private static Short transformToShort(String strValue) throws TransformerException {
		try { 
			return Short.valueOf(strValue);
		} catch (NumberFormatException e) {
			NetconfRpcError rpcError = buildNetconfRpcError("Invalid value : " + strValue + ", it should be a Short with range from " + Short.MIN_VALUE + " to " + Short.MAX_VALUE);
			throw new TransformerException(rpcError);
		}
	}

	private static Long transformToLong(String strValue) throws TransformerException {
		try { 
			return Long.valueOf(strValue);
		} catch (NumberFormatException e) {
			NetconfRpcError rpcError = buildNetconfRpcError("Invalid value : " + strValue + ", it should be a Long with range from " + Long.MIN_VALUE + " to " + Long.MAX_VALUE);
			throw new TransformerException(rpcError);
		}
	}

	private static Integer transformToInteger(String strValue) throws TransformerException {
		try { 
			return Integer.valueOf(strValue);
		} catch (NumberFormatException e) {
			NetconfRpcError rpcError = buildNetconfRpcError("Invalid value : " + strValue + ", it should be an Integer with range from " + Integer.MIN_VALUE + " to " + Integer.MAX_VALUE);
			throw new TransformerException(rpcError);
		}
	}
	
	private static NetconfRpcError buildNetconfRpcError(String message) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
				 message);
		return rpcError;
	}

}
