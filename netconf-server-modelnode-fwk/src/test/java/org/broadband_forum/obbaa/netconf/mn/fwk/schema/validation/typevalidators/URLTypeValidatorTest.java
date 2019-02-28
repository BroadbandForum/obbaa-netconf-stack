package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import org.junit.Ignore;
import org.junit.Test;

public class URLTypeValidatorTest extends AbstractTypeValidatorTest {
	
    @Ignore
	@Test
	public void testUrl() throws NetconfMessageBuilderException {
		testPass("urlvalidator/valid-url.xml");
		
		testFail("urlvalidator/invalid-url-1.xml",
                "Supplied value does not match the regular expression ^((tftp://)|(https|http)://((\\S+:\\S+)@)?|((sftp|ftp)://((\\S+:\\S+)@)))([\\S&&[^/]]+)/([\\S&&[^:]]+)$.", "/validation:validation/validation:type-validation[validation:id='1']/validation:url-type", "invalid-regular-expression");
		
		testFail("urlvalidator/invalid-url-2.xml",
                "Supplied value does not match the regular expression ^((tftp://)|(https|http)://((\\S+:\\S+)@)?|((sftp|ftp)://((\\S+:\\S+)@)))([\\S&&[^/]]+)/([\\S&&[^:]]+)$.", "/validation:validation/validation:type-validation[validation:id='1']/validation:url-type", "invalid-regular-expression");
		
		testFail("urlvalidator/invalid-url-3.xml",
				"sftp://abc:123@192.168.95.256/abc.txt is not a valid URL. Host 192.168.95.256 is not correct.",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:url-type", null);
		
		testFail("urlvalidator/invalid-url-4.xml",
                "Supplied value does not match the regular expression ^((tftp://)|(https|http)://((\\S+:\\S+)@)?|((sftp|ftp)://((\\S+:\\S+)@)))([\\S&&[^/]]+)/([\\S&&[^:]]+)$.", "/validation:validation/validation:type-validation[validation:id='1']/validation:url-type", "invalid-regular-expression");

        testFail("urlvalidator/invalid-url-5.xml",
                "tftp://abc:pass@example.com/dir/file.txt is not a valid URL. Host abc:pass@example.com is not correct.",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:url-type", null);

    }

    @Ignore
	@Test
	public void testCustomUrl() throws NetconfMessageBuilderException {
		String validCustomReq1 = "<custom-url-type>ftp://abc:123456@192.168.95.174/test/abc.txt</custom-url-type>";
		testCustomPass(formRequestString(validCustomReq1));

		String validCustomReq2 = "<custom-url-type>sftp://abc:123456@www.google.com/test/abc.txt</custom-url-type>";
		testCustomPass(formRequestString(validCustomReq2));

		String invalidCustomReq1 = "<custom-url-type>12</custom-url-type>";
		testCustomFail(formRequestString(invalidCustomReq1),
				"pattern constraint error-app-message",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-url-type", "pattern constraint error-app-tag");
	}

}
