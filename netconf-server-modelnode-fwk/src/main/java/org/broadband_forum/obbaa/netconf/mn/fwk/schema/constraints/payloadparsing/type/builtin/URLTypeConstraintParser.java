package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.w3c.dom.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLTypeConstraintParser extends StringTypeConstraintParser {
	
	public static final String LOCAL_TYPE_NAME = "url";
	private static final Pattern URL_PATTERN = Pattern.compile("((tftp://)|(https|http)://((\\S+:\\S+)@)?|((sftp|ftp)://((\\S+:\\S+)@)))([\\S&&[^/]]+)/([\\S&&[^:]]+)");
	private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])");
	private static final Pattern DOMAIN_PATTERN = Pattern.compile("(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6})");

	public URLTypeConstraintParser(TypeDefinition<?> type) {
		super(type);
	}
	
	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		super.validate(value, validateInsertAttribute, insertValue);
		String url = value.getTextContent();
		if (url == null || url.isEmpty()) {
			throw getUrlException(url);
		}
		
		Matcher urlMatcher = URL_PATTERN.matcher(url);
		if (urlMatcher.matches()) {
			String host = urlMatcher.group(10);
			
			Matcher ipAddressMatcher = IP_ADDRESS_PATTERN.matcher(host);
			if (!ipAddressMatcher.matches()) {
				Matcher domainMatcher = DOMAIN_PATTERN.matcher(host);
				if (!domainMatcher.matches()) {
					NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
													 url + " is not a valid URL. Host " + host + " is not correct.");
					throw new ValidationException(rpcError);
				}
			}
			
		} else {
			throw getUrlException(url);
		}
	}
	
	private ValidationException getUrlException(String url) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
										url + " is not a valid URL.");
		return new ValidationException(rpcError);
	}
	
}
