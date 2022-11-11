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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.BinaryDSValidatorTest;
import org.junit.Test;

public class BinaryTypeValidatorTest extends AbstractTypeValidatorTest {
    @Test
    public void testBinary() throws NetconfMessageBuilderException {
        String base64EncodedValue1 = BinaryDSValidatorTest.convertOctettoBase64(new byte[] {116, 101, 115, 116, 49});
        String validCustomReq1 = "<binary-type>" +base64EncodedValue1+ "</binary-type>";
        testCustomPass(formRequestString(validCustomReq1));
        
        String base64EncodedValue2 = BinaryDSValidatorTest.convertOctettoBase64(new byte[] {116, 101, 115, 116});
        String invalidCustomReq1 = "<binary-type>" +base64EncodedValue2+ "</binary-type>";
        testCustomFail(formRequestString(invalidCustomReq1), "The argument is out of bounds <5, 5>, <8, 9>",
                 "/validation:validation/validation:type-validation[validation:id='1']/validation:binary-type", "length-out-of-specified-bounds");
        
        String base64EncodedValue3 = BinaryDSValidatorTest.convertOctettoBase64(new byte[] {116, 101, 115, 116, 34, 23});
        String invalidCustomReq2 = "<binary-type>" +base64EncodedValue3+ "</binary-type>";
        testCustomFail(formRequestString(invalidCustomReq2), "The argument is out of bounds <5, 5>, <8, 9>",
                 "/validation:validation/validation:type-validation[validation:id='1']/validation:binary-type", "length-out-of-specified-bounds");
        
        String base64EncodedValue4 = BinaryDSValidatorTest.convertOctettoBase64(new byte[] {116, 101, 115, 116, 34, 23, 36, 11, 45, 56});
        String invalidCustomReq3 = "<binary-type>" +base64EncodedValue4+ "</binary-type>";
        testCustomFail(formRequestString(invalidCustomReq3), "The argument is out of bounds <5, 5>, <8, 9>",
                 "/validation:validation/validation:type-validation[validation:id='1']/validation:binary-type", "length-out-of-specified-bounds");
    }
    
    @Test
    public void testCustomBinary() throws NetconfMessageBuilderException {
        String base64EncodedValue1 = BinaryDSValidatorTest.convertOctettoBase64(new byte[] {116, 101, 115, 116, 49});
        String validCustomReq1 = "<custom-binary-type>" +base64EncodedValue1+ "</custom-binary-type>";
        testCustomPass(formRequestString(validCustomReq1));
        
        String base64EncodedValue2 = BinaryDSValidatorTest.convertOctettoBase64(new byte[] {116, 101, 115, 116, 49, 78, 56, 35});
        String validCustomReq2 = "<custom-binary-type>" +base64EncodedValue2+ "</custom-binary-type>";
        testCustomPass(formRequestString(validCustomReq2));
        
        String base64EncodedValue3 = BinaryDSValidatorTest.convertOctettoBase64(new byte[] {116, 101, 115});
        String invalidCustomReq1 = "<custom-binary-type>" +base64EncodedValue3+ "</custom-binary-type>";
        testCustomFail(formRequestString(invalidCustomReq1), "length constraint error-app-message",
                 "/validation:validation/validation:type-validation[validation:id='1']/validation:custom-binary-type",
                 "length constraint error-app-tag");
        
        String base64EncodedValue4 = BinaryDSValidatorTest.convertOctettoBase64(new byte[] {116, 101, 115, 23, 12, 45});
        String invalidCustomReq2 = "<custom-binary-type>" +base64EncodedValue4+ "</custom-binary-type>";
        testCustomFail(formRequestString(invalidCustomReq2), "length constraint error-app-message",
                 "/validation:validation/validation:type-validation[validation:id='1']/validation:custom-binary-type",
                 "length constraint error-app-tag");

    }
}
