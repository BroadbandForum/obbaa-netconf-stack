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

package org.broadband_forum.obbaa.netconf.api.utils;

import static junit.framework.TestCase.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import org.junit.Test;

/**
 * Created by kbhatk on 11/8/16.
 */
public class PemReaderTest {

    private static final String PK_WITH_DELIMITERS = "  \n-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQD1DrBjS7UeS9ZD\n" +
            "e42Zcyszc9mM5t50ue/ndT+lbWL8VP6HwRE7Y+za36put8Ry4FVwA1GsEkJBPeLn\n" +
            "/a9Z9/qA1d7+l89YhihQaYxTE4Q/ueMqM0RE7VKGRcqexewPrUGDTibbvZ91kY4c\n" +
            "wZIr07dTtXoeH2rxbF0OM0cW1zLkWARIpxqxT1Hwrq8rAaI4Flc/QIASXkhVSzmY\n" +
            "dw0uv70AHkJKdJ/aBijBD5FUASCOHfk3mgw0xVy8+dCeNadjtezwlo487FFqCtfg\n" +
            "v7ml3Fdtt3IDoph0moQRgNZgVEKt/+YA4pJyqzc+rqb4Yryni3C24iDlztH+GNuv\n" +
            "OX0wutvTAgMBAAECggEAZjzgniyhvagKFlFfvBtudKLqqnxPZweD7V7fVNcUKw4S\n" +
            "uvRzigGgeZhC9Lo6fWrWeksIMe/UMH/vQLZ4B+MLYeDYjgMsAFTIUPQYFTjZPfUB\n" +
            "r0OAQfl5KofHhwIwAEJaSLu8PoUYF+bIEXs4zowfug7Gifa1mU+Kazg9emwB0X2O\n" +
            "Gq21B8CFu9pdZzGQTfAgTNFnnvIm2PNltiAQaVI/2S7Q/NADS3gGpNWqbY/K3vvB\n" +
            "4xSITBaaph0WP/eNEZ0FWpW5bpABO4Q2A3Sx5MCnKt+DHymCyGuzYvRGmGhFmtbd\n" +
            "B771n5vGfWRYSLNcjy5hyUCeET1suYsePXG6h8w8MQKBgQD9CWHdo0A20XnZ1iIf\n" +
            "P+kAgclIxPGb2KRQB2iO50/NLILwvjz5KZBpkYs9NPdAfkE+t3zXvYVQw5ABBUuP\n" +
            "n6A9vV0ays9S0rZhKxEYiZ05ZbBICdkTMYggCTZDXzQsZtbpA8AMPI1BSF1TXTbL\n" +
            "M9+LwQ+LibY0c3Z54GVnQPjybQKBgQD37WJqfRtlFc4cWAKNXe3M0OMXUMdO9clm\n" +
            "J1VAwH5ARx6o1P6+FSefMaYUT6FAFJgNj2PwITJjh3z7kwSGH+8LR0UF+ONNZMaL\n" +
            "pfIkTunSejFQ00zAUt8yQM8QMtC+7jofoDLvFtAsSx5u16P2q9cHjNeW7x3vEIGd\n" +
            "Lpxu1QwfPwKBgQCFotLg7zsWuIMWHRVgU6yG7ASWPg0sNbpx2bfK4TcwMPXml1I0\n" +
            "dVMjrg5PgQ2kLgnfSaDRf/JMuTvwjg9eBvvmH4BwifP81fQkVU5uGx/CFIaJRUoz\n" +
            "7NDrunHCGyG+4YFXBvgCfmhLtiAzyuMJZpgFgyzmkRB9mw1TSMPFSHcx8QKBgQCv\n" +
            "P4Aij75ujKQ9isR7EtsFvN3Y3EOV/8zVxZXQiIB1hRAZ/Tz4NdHlCF5B2yu7NRNp\n" +
            "+mKFGaIZkmr5FSnMeQQqr70NhKl/Sm3BxpJLsfA71B3J6SJGjA2y4va6l4DQhWpW\n" +
            "cpGuSSzrMkoXxZvjwAHmF1tJGErLcpp79bej7Dp+VwKBgQDOFIlfFRS7VwA5KPg2\n" +
            "euezvaRz51eJOIIqRtKMzGLoFsIAT6UoTaBbYz6nk5qPrWXa64BWU5CSNENXv8lQ\n" +
            "lBIiP20uRgEJKeq/ADK9W5XkUN8zvKqJ+YTyq139RyugQ1iKeo2vU8AW8W643vtc\n" +
            "8/Amd1Pkr/6UoVGRvzQFsf16gA==\n" +
            "-----END PRIVATE KEY-----  \n  ";
    private static final String PK_WITHOUT_DELIMITERS =
            "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQD1DrBjS7UeS9ZD\n" +
                    "e42Zcyszc9mM5t50ue/ndT+lbWL8VP6HwRE7Y+za36put8Ry4FVwA1GsEkJBPeLn\n" +
                    "/a9Z9/qA1d7+l89YhihQaYxTE4Q/ueMqM0RE7VKGRcqexewPrUGDTibbvZ91kY4c\n" +
                    "wZIr07dTtXoeH2rxbF0OM0cW1zLkWARIpxqxT1Hwrq8rAaI4Flc/QIASXkhVSzmY\n" +
                    "dw0uv70AHkJKdJ/aBijBD5FUASCOHfk3mgw0xVy8+dCeNadjtezwlo487FFqCtfg\n" +
                    "v7ml3Fdtt3IDoph0moQRgNZgVEKt/+YA4pJyqzc+rqb4Yryni3C24iDlztH+GNuv\n" +
                    "OX0wutvTAgMBAAECggEAZjzgniyhvagKFlFfvBtudKLqqnxPZweD7V7fVNcUKw4S\n" +
                    "uvRzigGgeZhC9Lo6fWrWeksIMe/UMH/vQLZ4B+MLYeDYjgMsAFTIUPQYFTjZPfUB\n" +
                    "r0OAQfl5KofHhwIwAEJaSLu8PoUYF+bIEXs4zowfug7Gifa1mU+Kazg9emwB0X2O\n" +
                    "Gq21B8CFu9pdZzGQTfAgTNFnnvIm2PNltiAQaVI/2S7Q/NADS3gGpNWqbY/K3vvB\n" +
                    "4xSITBaaph0WP/eNEZ0FWpW5bpABO4Q2A3Sx5MCnKt+DHymCyGuzYvRGmGhFmtbd\n" +
                    "B771n5vGfWRYSLNcjy5hyUCeET1suYsePXG6h8w8MQKBgQD9CWHdo0A20XnZ1iIf\n" +
                    "P+kAgclIxPGb2KRQB2iO50/NLILwvjz5KZBpkYs9NPdAfkE+t3zXvYVQw5ABBUuP\n" +
                    "n6A9vV0ays9S0rZhKxEYiZ05ZbBICdkTMYggCTZDXzQsZtbpA8AMPI1BSF1TXTbL\n" +
                    "M9+LwQ+LibY0c3Z54GVnQPjybQKBgQD37WJqfRtlFc4cWAKNXe3M0OMXUMdO9clm\n" +
                    "J1VAwH5ARx6o1P6+FSefMaYUT6FAFJgNj2PwITJjh3z7kwSGH+8LR0UF+ONNZMaL\n" +
                    "pfIkTunSejFQ00zAUt8yQM8QMtC+7jofoDLvFtAsSx5u16P2q9cHjNeW7x3vEIGd\n" +
                    "Lpxu1QwfPwKBgQCFotLg7zsWuIMWHRVgU6yG7ASWPg0sNbpx2bfK4TcwMPXml1I0\n" +
                    "dVMjrg5PgQ2kLgnfSaDRf/JMuTvwjg9eBvvmH4BwifP81fQkVU5uGx/CFIaJRUoz\n" +
                    "7NDrunHCGyG+4YFXBvgCfmhLtiAzyuMJZpgFgyzmkRB9mw1TSMPFSHcx8QKBgQCv\n" +
                    "P4Aij75ujKQ9isR7EtsFvN3Y3EOV/8zVxZXQiIB1hRAZ/Tz4NdHlCF5B2yu7NRNp\n" +
                    "+mKFGaIZkmr5FSnMeQQqr70NhKl/Sm3BxpJLsfA71B3J6SJGjA2y4va6l4DQhWpW\n" +
                    "cpGuSSzrMkoXxZvjwAHmF1tJGErLcpp79bej7Dp+VwKBgQDOFIlfFRS7VwA5KPg2\n" +
                    "euezvaRz51eJOIIqRtKMzGLoFsIAT6UoTaBbYz6nk5qPrWXa64BWU5CSNENXv8lQ\n" +
                    "lBIiP20uRgEJKeq/ADK9W5XkUN8zvKqJ+YTyq139RyugQ1iKeo2vU8AW8W643vtc\n" +
                    "8/Amd1Pkr/6UoVGRvzQFsf16gA==";
    private static final String PUBLIC_KEYS = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyCJcuCh0yXlwp/+E0VEJ\n" +
            "i9arlag2vJdkq5s3DuL6RgKeASOE/dfXugV90Oe49EA/62atpi0g9yZnHWmZ8hsd\n" +
            "ZSnEFG/CHRdkY0jPIqRNmRw0bQGKqYyCrMSZlJt4y/rRvWGjCuX0eEAuMhMXw55X\n" +
            "oPiB0p50mJcEAzl+xupH0I8LAv20zRSi21liPe0cAIBUC8exBplwvPlCFcwTZ5yi\n" +
            "EjGVlr0Oc4EXmzPbI2C0TO2cnylQWbDvBApHLY4CltYAiohP15Opcr0N2UDsezZk\n" +
            "Mn4wIlrWx44SmtdqM8gz6wYnbB4zQ1G2ZFq3HvLlp81dgmZYIkQzj21X3dhG5rtp\n" +
            "DQIDAQAB\n" +
            "-----END PUBLIC KEY-----\n" +
            "-----BEGIN PUBLIC KEY-----\n" +
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1e+LzeNkDUreFVEFNEdtrVIAB\n" +
            "b2M7hSG0utc3ZYZGoUmUXzv9x6ow90cQ8E1CqZt+koZ502qDJzS+s9HcIb9O7Bpa\n" +
            "PQ8hACnQF/guGR27Zp9MM2Bvnh0+fxo2zdvII5UMVG1j4T5jXvMYtdbX8uyFiTjj\n" +
            "2kSLlluTs46FYXVlxwIDAQAB\n" +
            "-----END PUBLIC KEY-----\n" +
            "-----BEGIN PUBLIC KEY-----\n" +
            "MIIDRzCCAjoGByqGSM44BAEwggItAoIBAQDLiC/E6lYos8w9d1J6cMgi5eTicJK5\n" +
            "PqV7yOGZ5phGGScAL+YoZkdoyyMUh0H0w86xUK//+K4qtDMfQ3rRjX5FkLa5oCfJ\n" +
            "xf+T3yfFWwy+7O7sHpB8vwU/85mRqNkyP4V9fFEwMmkrJoIHiOOkMZDegP46UReZ\n" +
            "p8d+RuSfsrzpiXjVTxgFAv6wOzLEIQ1ALxiuQnmtTmlYxAFYQcizwXWfpRuooOeY\n" +
            "bIzqv6iX9bA0LIm3SpBGPtFct3eneZfaLydwTw6ggMYJ71ZBT6EDqzKJCU4ZjQss\n" +
            "yQyCMo3eLCHLEPs2KTbA4TAXz/yYUZDpQbY1054dzji0BDzlApoBJx6RAiEAwBD4\n" +
            "wiJglBogWiwPX7pA3K4UNB8Xjb9mjfiBwNsXyc8CggEBAJFgzYKLf9tRVnXfHbeF\n" +
            "Ql1t5XN101DpzQqHNvke7U9sEAhqDeU/TpMiZr9lWz7nFSjEeS8EY/oqCN9sLqEH\n" +
            "jP9eUI95cm5+qsPP5sAeLBWUpIg8DOMr0MyzxduH7EjMPIFbTB3/UTID+mpxwvio\n" +
            "CCROm8g3uLrPHMpL6JSYmuk0MczgPGd7Ov8MopMqu9tMl6ivTuVnaWQwlPvpjl+4\n" +
            "2Zh54vMOehq1MNBZMNaIYniVIcDncsdjXL9l1fcEad+LwS7dp7dzdCEBfDf0Yj0E\n" +
            "UgVAMJZtbQa4mj4+6i5eVZZGcTQEtwkG3CC5j59Qnirsh9uJw5irYl2CzMoDbWb9\n" +
            "HX8DggEFAAKCAQAIDCISnJS0L43JZmfXpfjpj8Tsp0JlbhROKh5g9UGiviR4xUob\n" +
            "SBewsvuL5QM8cKY5KeghHXya/nEksNYolrh46E/0bjRpbjUA+5DFPQyVfAmou4Gj\n" +
            "2bTe223dgJPRtEj7Vb34kPlpbJAOuUXHB0T0qcPP6p69RGnQYdahmdPmE3CbKXmr\n" +
            "+nIqgCfyWcU6p0nOm0BWnEXzvE0KMf93nxQmGzYXxNHWJX+iyshlLADxk21DoX/N\n" +
            "Qa8MI2y2KOl2xCj/4fw/44dQgAvIn5cHpichzo4KwD50UFL533V9nHrYczTuLnmN\n" +
            "WEZaSNH2SGWrH0t3TpChCXc1qs3gWCMzvWHA\n" +
            "-----END PUBLIC KEY-----\n";
    private static final String DSA_PK_WITH_DELIMITERS = "-----BEGIN PRIVATE KEY-----\n" +
            "MIICZQIBADCCAjoGByqGSM44BAEwggItAoIBAQDLiC/E6lYos8w9d1J6cMgi5eTi\n" +
            "cJK5PqV7yOGZ5phGGScAL+YoZkdoyyMUh0H0w86xUK//+K4qtDMfQ3rRjX5FkLa5\n" +
            "oCfJxf+T3yfFWwy+7O7sHpB8vwU/85mRqNkyP4V9fFEwMmkrJoIHiOOkMZDegP46\n" +
            "UReZp8d+RuSfsrzpiXjVTxgFAv6wOzLEIQ1ALxiuQnmtTmlYxAFYQcizwXWfpRuo\n" +
            "oOeYbIzqv6iX9bA0LIm3SpBGPtFct3eneZfaLydwTw6ggMYJ71ZBT6EDqzKJCU4Z\n" +
            "jQssyQyCMo3eLCHLEPs2KTbA4TAXz/yYUZDpQbY1054dzji0BDzlApoBJx6RAiEA\n" +
            "wBD4wiJglBogWiwPX7pA3K4UNB8Xjb9mjfiBwNsXyc8CggEBAJFgzYKLf9tRVnXf\n" +
            "HbeFQl1t5XN101DpzQqHNvke7U9sEAhqDeU/TpMiZr9lWz7nFSjEeS8EY/oqCN9s\n" +
            "LqEHjP9eUI95cm5+qsPP5sAeLBWUpIg8DOMr0MyzxduH7EjMPIFbTB3/UTID+mpx\n" +
            "wvioCCROm8g3uLrPHMpL6JSYmuk0MczgPGd7Ov8MopMqu9tMl6ivTuVnaWQwlPvp\n" +
            "jl+42Zh54vMOehq1MNBZMNaIYniVIcDncsdjXL9l1fcEad+LwS7dp7dzdCEBfDf0\n" +
            "Yj0EUgVAMJZtbQa4mj4+6i5eVZZGcTQEtwkG3CC5j59Qnirsh9uJw5irYl2CzMoD\n" +
            "bWb9HX8EIgIgVgFv6ZS3yg8hQfa2TTsslrjgS75VHFhSgLu3ieG2Atc=\n" +
            "-----END PRIVATE KEY-----\n";


    @Test
    public void testStripPKDelimiters() {
        assertEquals(PK_WITHOUT_DELIMITERS, PemReader.stripPKDelimiters(PK_WITH_DELIMITERS));
    }

    @Test
    public void testStripPKWithoutDelimiters() {
        assertEquals(PK_WITHOUT_DELIMITERS, PemReader.stripPKDelimiters(PK_WITHOUT_DELIMITERS));
    }

    @Test
    public void testReadPublicKey() throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException,
            KeyException {
        InputStream stream = new ByteArrayInputStream(PUBLIC_KEYS.getBytes(StandardCharsets.UTF_8));

        List<PublicKey> publicKeys = PemReader.readPublicKey(stream);
        assertEquals(3, publicKeys.size());
        assertEquals("RSA", publicKeys.get(0).getAlgorithm());
        assertEquals("RSA", publicKeys.get(1).getAlgorithm());
        assertEquals("DSA", publicKeys.get(2).getAlgorithm());
    }

    @Test
    public void testReadPrivateKey() throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException,
            KeyException {
        InputStream stream = new ByteArrayInputStream(PK_WITH_DELIMITERS.getBytes(StandardCharsets.UTF_8));

        PrivateKey privateKey = PemReader.readPrivateKey(stream);
        assertEquals("RSA", privateKey.getAlgorithm());

        stream = new ByteArrayInputStream(DSA_PK_WITH_DELIMITERS.getBytes(StandardCharsets.UTF_8));

        privateKey = PemReader.readPrivateKey(stream);
        assertEquals("DSA", privateKey.getAlgorithm());
    }
}
