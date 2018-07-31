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

package org.broadband_forum.obbaa.netconf.api.x509certificates;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by keshava on 4/29/15.
 */
public class DynamicX509KeyManagerImplTest {
    private static final String INVALID_KEY = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n"
            + "MIIE6TAbBgkqhkiG9w0BBQMwDgQIiUVmbHmyy2oCAggABIIEyL7bCrUEOC3fkVx0\n"
            + "idR34r3bsqeYJ5J+FNI7SHXCEqFiKJ3S/54eMPlZ1NpL96lERYiZzA208F+r11ux\n"
            + "GZHgZNyaT5W7MRwlQk6ni7HoxJWuMgqU9Mg+5op50CEBIX7QxhPd3mllyGWZ/QCp\n"
            + "J4X2HSBku7mcX83vJdg2izdRI7gUY9CraHR530jB7duNnBO6Lq4ndwD0owXAdQXz\n"
            + "mLhu/8lb66xuwKVmmkSDur5sL9fK18jZF/BJk44IWE4rHau2Z4os6YGBJ7JU9KDt\n"
            + "ZWKXA7o21DgGPAca5fL338R7UNwEiwS3CE/9VncJYMOGAQroNHKIaTnCRtHTBy4j\n"
            + "dBcT86yVIAOhEoAMWAimUeapQ6hm19vly1pjQjolU9yNbiKljzmN1vttCV/E2znR\n"
            + "tRsIltE254pTVf1NnImkJ7UQq5NW6bsl0bteXQkgnBndjdsfyaO6KYD0OV2Cjqin\n"
            + "bcR8vdrsUHqoBBn5Of+a5VQhxNFoFNZ668iOuiEGPh1gWP6zZRfuclhzXatNj+4O\n"
            + "QjTyAPdKf54PnBhe6+bsNPAq9gUjWzQf734oh7DL1ogqrcw68gpNFtFFbunriVCA\n"
            + "aRa/8AQDJREMkveg347rKONSrth/scOaECM/fht/4TrZQU5sijxO9N4hOGVbb06q\n"
            + "DGaUATbq7HhiwvEM4lPqpd5l4geOTNJAOzQpfdaIhdFSDI9sqnpmmjhAiOO/w13J\n"
            + "hxI5LOlIqaLWuZURpp900d8Fs74OOl4XttdPI8RSs9y+rK18hBjzrCVaQNyEmIyn\n"
            + "x/kxQvNXFAhxSo/jrexT2QNSHcHV5HRuWwufxFLqISRnRKysBjVy6rCDOS+LIn73\n"
            + "t2VvYgyNFoX5cXpUssIRS4xdK8ygLD9XiYSRYVD7Q4dbdML2uEoec6kxzFh9OwaY\n"
            + "Ggy9xugveiC7252taAogS/5T3WURpKzSRRkqDwUnyhz0oEzIqpmveKUe1JlKwxnM\n"
            + "zJqaD3g1hWjsa+fpa6t/sw8K5QkJ8kv0CDtulsbVtJ05IGV7XysNqhpfV7rm3bj0\n"
            + "HApSxvbs7s4+oVBjldQG3kUOyDJrBs9FX7xZu/uyez/dvg75K/hdguEOFc9pQPyb\n"
            + "xjCaDPFFskkYM9IxURuehMlSX211O7WLZAPsv/tT/OBQkHzRxXAYHEZqEmoXgBEx\n"
            + "sJc/BcQtqXrC5t33IrGlHEvQgPbzS/AZyEgtvVp/j7znR/0nkWwnkezLY/dfV6V1\n"
            + "VokfZ9/++T7p5PsNkktSNR2Z2pSMT/4ZWQK+AMbsxUpklEoG5S3O4zOxLJC+NkQC\n" + "c3ew0ZRhYn6XZcg+Hg==\n"
            + "-----END ENCRYPTED PRIVATE KEY-----";
    private static final String RSA_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDFuwmWQOQsWYaw\n"
            + "E7vuVKg1bO1ihvjsS6CiQnuqgLr2x+zCUzqqhmWrb6EfyFcQeHt9LcaspARTona2\n"
            + "O2ZRZa9FMC/q2HKK9PONaEbSU60nCu2PXPC8wVLG4pfu7H9pXVVuX+fzIVaQaGEF\n"
            + "iW8fmrUj5ocyPl7z/PydI01gfBl7aXpYZjdl+jp4bCGoatSOdPx/hO0R06JdGrHR\n"
            + "sFX6BM76rvn3spBPangSCRHR4ZW0mBlH2GvbljkxqTxmiviBOeITfPQTtbBHICqE\n"
            + "trvwjCIcA8w5mtNpLcxGs5LgQtN7+Zjx8ZQz06/rsC03UiM1jkO1/MD87/JHER1t\n"
            + "1AJoZyHbAgMBAAECggEAM9oCf5Zwv3iSq2XhaHRmz+vKrIW8jSqf6y0IBQRRAxoW\n"
            + "ajctMT6hNr3FQPJmcnlF2OPCL8+rGsuF2e/luv6xYbFrP2iXUP0/SRvJRqqOw6hH\n"
            + "98pljvobXPBa61q1ZsoE9zbLOqTJcIEOwEb0DxOg4NM5LOHSQKVdEVAKoHyqqgPe\n"
            + "lU84EoANnsAfm2M8Y0RV8rgo8GbqzuFWe83DjoPXf/AVy0Ajt8vxUjuuhGANchZR\n"
            + "4CvOlSsH0WnG8HfbLtioKWTOc7WSwBtcSDHDe/BjIidLjHiVr+FFKvcFz8+wtZo7\n"
            + "zHyh4tH1jKcWpdNlJ7vxOSUw2gZjsbME73dxv0ABcQKBgQD1C2l69z6dGk1WGLSb\n"
            + "Qga52+ChISw+WtaGp72Aqa0+cUNQNMpmOkvGnWCqe8j9DQQSTUAGock8HAqwdkTa\n"
            + "DMae/Bkb3yqojK/0Evo0vPyhHcnUuFP9kXOR804zqenuUbHTdWAnSuQhH2she4FC\n"
            + "be3wt2WXFRRrL0FrdmDD+xXC5QKBgQDOkhtfAo6v4tiZwcdyXzfulMYsxQLc6BPE\n"
            + "PxEzVQ7fZhBiqJJwc5CKVip+J+kUy2QurxP9asEWSBespWziFiHTjO8A+33a/GU+\n"
            + "WICP709JbGtlSnU/mKTpQlKP0eWnNX9uZQ5WIJ5mVOopbB9dUnBvR8hR7Xg8dqhN\n"
            + "Fa7eZedFvwKBgQDCmuiPJnHMdIn8mSw9rQcIba2MaXronl2GnQprdtVGJM8vkG05\n"
            + "GIz7VBrj6eCpzr9wBXeWjL/zA9YdZB+0B7dppY4PS4FmUCygqr2YSopbLfwO+sCB\n"
            + "PRVKUK7HauaERM7zv2C4c8qO35PGCH6UEheINy3v+Wa45NOQk3evzOTwXQKBgQCF\n"
            + "t1BefTJOKqKZTTXFFFwJZWavHkyPIO+Crmx89QdulwvuQT6h6jzbP5G8Hiuj2VEd\n"
            + "Yxmmhk89FCe3C1JjO35kCavA0AsVESKca/+0rG1/kt4mMD+bjjzZ/aOiE3X8egXm\n"
            + "OSZBnFXM2hTGAYaAC1hawHWsivK0+P5S/8E1l3NQ0wKBgBC2hDtNzS42PtBhHDD+\n"
            + "7nSF+pbJmB53mjXH2bIaM+Nh1OAlOnnMocVVu96ZGK8L/vHcu42oeXfaq34gKPDY\n"
            + "PMJuBplxHJ7a9GNL9e/VPauogTjwZvbH5SEkfpFNhueWk3x6CeupPEsAxoEslsKD\n" + "aa4T099AcE+oNokQUuDxuKzk\n";
    private static final String CERTIFICATE_FOR_RSA_PRIVATE_KEY =
            "MIIDNTCCAh0CCQCvOFMuHFfwGzANBgkqhkiG9w0BAQUFADB3MQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExEjAQBgNVBAcMCUJhbmdhbG9yZTEXMBUGA1UECgwOQWxj\n"
            + "YXRlbCBMdWNlbnQxDTALBgNVBAsMBEZOQkwxHzAdBgNVBAMMFnd3dy5hbGNhdGVs\n"
            + "LWx1Y2VudC5jb20wIBcNMTUwNDMwMTE0NzM3WhgPMzM4NDA0MTIxMTQ3MzdaMEAx\n"
            + "CzAJBgNVBAYTAklOMQswCQYDVQQIDAJLQTEMMAoGA1UEBwwDQkxyMRYwFAYDVQQK\n"
            + "DA1uZXRjb25mIHBlZXIxMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\n"
            + "xbsJlkDkLFmGsBO77lSoNWztYob47EugokJ7qoC69sfswlM6qoZlq2+hH8hXEHh7\n"
            + "fS3GrKQEU6J2tjtmUWWvRTAv6thyivTzjWhG0lOtJwrtj1zwvMFSxuKX7ux/aV1V\n"
            + "bl/n8yFWkGhhBYlvH5q1I+aHMj5e8/z8nSNNYHwZe2l6WGY3Zfo6eGwhqGrUjnT8\n"
            + "f4TtEdOiXRqx0bBV+gTO+q7597KQT2p4EgkR0eGVtJgZR9hr25Y5Mak8Zor4gTni\n"
            + "E3z0E7WwRyAqhLa78IwiHAPMOZrTaS3MRrOS4ELTe/mY8fGUM9Ov67AtN1IjNY5D\n"
            + "tfzA/O/yRxEdbdQCaGch2wIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQAObuBFvZjN\n"
            + "TKms8VY8zNXfeimFA4LF8pQrc3PtVRySGo6KOnNDCTtq5w0kK52eUY2p1d5Ur/om\n"
            + "FRXsyaXlWXsCP2Mvm6AF+LfC0v53kULxBHmPL1VHfpBYBt2m6M7ZDCUS4NLVL1JY\n"
            + "Ea9Hql2NXfx/mdhukPJcwVeXW8InJubj4oCLsQPWEUNo56xKoLjiQfz1HqtUnLN6\n"
            + "plEEnUnVlh8HZH9SvLjErb2vbswr8h1XZmiK6669MmuRKYV7k7QFhEeEMLfK3ZiA\n"
            + "od3sc3HFwK+J6vyGOyb50sx7hy/rOvU4tQ5vLtQGAsVn0YnaY1OtziJZDHxd/5NU\n" + "jpZDN3ZThHu6\n";
    private static final String CA_CERTIFICATE = "MIIDwTCCAqmgAwIBAgIJAObvpCoyTSy0MA0GCSqGSIb3DQEBBQUAMHcxCzAJBgNV\n"
            + "BAYTAklOMQswCQYDVQQIDAJLQTESMBAGA1UEBwwJQmFuZ2Fsb3JlMRcwFQYDVQQK\n"
            + "DA5BbGNhdGVsIEx1Y2VudDENMAsGA1UECwwERk5CTDEfMB0GA1UEAwwWd3d3LmFs\n"
            + "Y2F0ZWwtbHVjZW50LmNvbTAeFw0xNTA0MzAxMTM1MjFaFw0xODAyMTcxMTM1MjFa\n"
            + "MHcxCzAJBgNVBAYTAklOMQswCQYDVQQIDAJLQTESMBAGA1UEBwwJQmFuZ2Fsb3Jl\n"
            + "MRcwFQYDVQQKDA5BbGNhdGVsIEx1Y2VudDENMAsGA1UECwwERk5CTDEfMB0GA1UE\n"
            + "AwwWd3d3LmFsY2F0ZWwtbHVjZW50LmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEP\n"
            + "ADCCAQoCggEBAOEZdc2HTtH2sP4XA3nc9aCy55P+HsOUB0TSMoIZSIhS15N2YuTK\n"
            + "GN0Xa5RYUZp1d+Cv+R2+AIcNZzqM/QHrwsj2GbjG1d1QSkCvo7VkXtBc2icgjdxg\n"
            + "c6k2o9bsJli1Ynq9CtChzYX/LTySsiTUtZu3/ENXbPjyCFqOHouXNcQmLOhgaIMk\n"
            + "9AF9KUXeR2CMdpZ5vZHMsiU9mJqh2Y9Ywmil9hpdfRYq0/9VmnCefkQbPQsHvlRG\n"
            + "3B85WFlgUL0j1HYmsNAl+kz5v8XWYcQcyFqx7G60z82HH7ViCB0WFaY0YIxEwZno\n"
            + "CldGay4pHhmMikFzGi9jo8Uq+Qjs4ziJAbkCAwEAAaNQME4wHQYDVR0OBBYEFDmT\n"
            + "593teFRaqAHOQ9nj09bjbFpWMB8GA1UdIwQYMBaAFDmT593teFRaqAHOQ9nj09bj\n"
            + "bFpWMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADggEBACBRBbFwTkgUOMrz\n"
            + "klzCG4I7CjHid89odfSPu7Ne/PzPI4DUimUAxJ4pskbD9AZETVMB2yN0sfVJpW4Y\n"
            + "nKX3LCBCie7wP0FHbDtUnyS8mMOpsCS0Es/6+/COT/D8jqu5cvgojQEegNXHcvT+\n"
            + "C7Bx04EwgrjItRzj2CVzxavTnLkXRBatKnYMcSRPC+YmMXJutm6AW1xVCXr09YzQ\n"
            + "n2MbVXArL+g6DvV/uFTW6aWRXLlZqAER6SsvM3cuymSzykTFnTD8AvWCGg5iGD1v\n"
            + "KBwa59s47ufvKKS8kZF1lulKE8bZAVW97VypLetNeUi6svW4KXb2T67foF+HoyqC\n" + "9MfR/bw=\n";

    private static final String RSA_ENCRYPTED_PRIVATE_KEY =
            "MIIE6TAbBgkqhkiG9w0BBQMwDgQI9EEtQ9mEcJ4CAggABIIEyABhOJ+Hd6QCgd6E\n"
            + "YrBYuCWIfu4eklO3lJhfu7ixk/8pEbiTXp5x/b0uqZRu4MRy9V49W/+MI5GwhARr\n"
            + "lnlk9nq+VzQtF6YRugZG3R/Cy45V5BsUIHz1EfPpyQKG7ntAzE16vzOLpX+SKb6L\n"
            + "lCy8MupcnplC4YQZX66A11E5WR5HOGNzQk3xD3fsdUKRN5pz6tVP7+zF7tGOwHwE\n"
            + "WTPO+67/6BqFE9Tw9HFnCwpdZgO1Mfvs0JG+pvFgNFYNJo2Q7Pgv2dTSviW80tV5\n"
            + "ouRruq/VU8/aAM1jg5av40ULaBXCCNaSYkpMoKCbimDXNRTJE3nyEkvtQHEuYplo\n"
            + "ZWrJYG2MhAHS4dS6ie+dnsltDUJ7gjpV4Pb/ODht4oUqhRk3SGPxhCyTOnqS9kpU\n"
            + "sMxcKxBBJ3H4zHnGhmO7jEMy+1ddkJmPq705wKp4tY32UQt4U1B8kDxSjP+t2psf\n"
            + "JjNPwz5qPcj+Vv+TGjr999swLcFdc5dwLrYkknJi4zd9IXN+4u2kxwQmDHo7Eckp\n"
            + "A0x7RNvOf0l+mq4gPtKpBSIRcLvBPTfu0DXFYiiO2G3yz0asEAlcDk22MV5io3wf\n"
            + "d5Ctjl+0FpNApeRUn8mh+hNCvoPEE7h1GBarjqBiWtMdmPcaRDsVWKa08bmnmrCk\n"
            + "BS/Pudc1VIEl6o4jbM0TP9ggZwqt+d6hkG2pKU/b5kBZDJucVV3WlpeJx9HF9wVt\n"
            + "6SEm9ribVRA+kSdAURvXcSQ7W9fqdYZoso6m2HcW4JoO750QViSUZPssNBFsSbYK\n"
            + "YgkY0ppifXx8PCWW8KZFk8K7PtdUlfCaSdY0WfrqV2/FUyLqc2I0ADhVstP4Ym6r\n"
            + "GBd/srtcyJh5ho5aZocuW84eNPPuNAPP6HNyPVpRdbn/OZPm4r5RY6ejZgn06u+M\n"
            + "A2LWdxq46kV05TESwffxFqnt1qNulTeYYeIRGiMuZCm/Nqt3KWukjsQy1ECgyKa9\n"
            + "xiTUHC8jzY8fE/1YF6sJBbi/xWO0lorV8745yY/VYVtApraLoadLQUZ0YwTAseH7\n"
            + "yX5P55whtZFcS+kDhakhKhe/MCYSpBgyLQLV3BgEuYL+tSCPYN+OyaMi33G/EoOc\n"
            + "hWJLvHqjm5j1mEJebhVry0EXFtTvNySukeYVneLg8ukPs7BK1WtN7GjuR+gvJ2oF\n"
            + "c7WIZl0BkyVLQYgbeNPM49O/Kvpo4E3L3bN4PZR9VnxCP+y+GJXoNWBLuxu2MRGa\n"
            + "gFb9rnl7JY/nJx15WC0/IKMY6OG4+0HDqzC/jow+1s+l2SR3Uj+aBy4Jvg8RCDTt\n"
            + "Nc+5GpL7MPdzDz+Db5SZVHzIZ3D7VBwEtm6rgjRKUR3RRkt6nco6fNnBwRKwHch+\n"
            + "akx/sZp2dizKYM0HR+o+7V7zgpNwaA3edEfRBClZ0bZkfp/ilYQwNIcEteGarM7p\n"
            + "6dDYHOJylaui2ips8OwKTGkgHb220V6usAM+iqF7DFXg7CtJVO0GXJm0dd+WUju0\n"
            + "OJY0InbyNi59YtOVAYsTf0s54pbxBCduX1QB+pkg+C3IYAvhmjfiVswqY3zr0bVg\n"
            + "MvIFM0s65AFjZasDbQicriqaRue+MEy6n4jZfzoXstDMpDmHT39GXn4wJt3Ydh4T\n" + "PbfE3eCd0NPBiz4yCQ==\n";
    private static final String RSA_ENCRYPTED_PRIVATE_KEY_PASS = "rsapass";
    private static final String CERTIFICATE_FOR_RSA_ENCRYPTED_PRIVATE_KEY =
            "MIIDRTCCAi0CCQCvOFMuHFfwHDANBgkqhkiG9w0BAQUFADB3MQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExEjAQBgNVBAcMCUJhbmdhbG9yZTEXMBUGA1UECgwOQWxj\n"
            + "YXRlbCBMdWNlbnQxDTALBgNVBAsMBEZOQkwxHzAdBgNVBAMMFnd3dy5hbGNhdGVs\n"
            + "LWx1Y2VudC5jb20wIBcNMTUwNDMwMTIwNDQ5WhgPMzM4NDA0MTIxMjA0NDlaMFAx\n"
            + "CzAJBgNVBAYTAklOMQswCQYDVQQIDAJLQTEWMBQGA1UEBwwNUlNBIHdpdGggcGFz\n"
            + "czEcMBoGA1UECgwTRGVmYXVsdCBDb21wYW55IEx0ZDCCASIwDQYJKoZIhvcNAQEB\n"
            + "BQADggEPADCCAQoCggEBANCxSpJpWwZ/+sKUUBajkM+Kgpvq5jRLRvAwwH6JlSp/\n"
            + "FSANpaWzPPuvh3zKsiUzFVQBe9ndjD56nCIQjfXWi0ySk8/q77HDMAHSuPKcJCoN\n"
            + "IDjycrs35N8DvRM3H4ilpPu50s+MwSJx0rQC3VaVoLVb4td51XBPme5xE/hEiYxJ\n"
            + "pCMiNpbwSdA60yDLcgaZjOSEo8YyW8xCRwbBv9Ty8rXCxMGpKDfC0yQOFj2L3UMT\n"
            + "GmGF3fqlSnMQoV5/DNgnJsPZPlHk5XNILEyNCZIilFcX2F/3QSDRRcvK/gxkqtSk\n"
            + "LrahB1qOLxlWiQE10OlmsUXNZkKh+5V03rrZEwheikUCAwEAATANBgkqhkiG9w0B\n"
            + "AQUFAAOCAQEAuyNNMEQRN7nMYr/iUX5JNEmPVNO5zMAngU0JIX32yobd7MxrBMhk\n"
            + "tglK71qghEzVg9VfQJ8vjD77qszU7FJpU4e+3W95CH859smCAb36ScWAZ+ve0Yl7\n"
            + "aVJBJAP77iPHoJB81PwnCWnqMv0nsSpjELHCvtskJFajL6/TMvMamWU2fHMTX+8y\n"
            + "KSy8UHsJlyuG0lifWAWAByesIf1RKlT6XsWlwktYUZhlZ4vngUq+d+lxULuYySyQ\n"
            + "6+6p1B6N2d/G0BP//rfe545GXqisvZVWiEX6bUkbR2UWECGMUOQ2kyJK7o38CTNz\n" +
                    "5m8VL/eSzilLP3BWrdU2RXI4KVZwqDK65A==\n";

    private static final String DSA_PRIVATE_KEY = "MIICZgIBADCCAjoGByqGSM44BAEwggItAoIBAQDQ3bisLVPEXLOyhMLaczI+JHsk\n"
            + "lx05MeeHeSu6yV6i925e1psWy51f9h2Sq7ntFr+rPDHXoH7J/vUFMQ8MsXg4OLD9\n"
            + "FkA4iIfJ6e3+Tu2h7HmK5Fjw+UtusD8m/5Zabzt2X1jfX9I+HRZqVf7X9B47b3b9\n"
            + "K2t3vRblsaOSptDFWJf4tOu1xZPs/GvBQC7p0yeJ7xCyq5dN9ciBPOFKI+nRCeGq\n"
            + "44jdnr52+SAATFlFbeYn8yLRabMEMdv+PL9lxfxOaYk3iyITPOYl+JWDAxjt6FKi\n"
            + "qq8wjtzKG7mOqT+11z7AYX7lAu87nWGH/KYpyBaOF+zycS+4nfEWxhOExnw3AiEA\n"
            + "p7G8G2XlTq/DMiwO0rbobDzl60BY5QO/SKGTaaUPzdMCggEBAMxzcKaZCJ/KHUSf\n"
            + "tSAhclvvyN5ltQ5R4URZXzeHRhChwkgCS17lQh4hdtMd4YeWLY4vvrjX/YTEmwMH\n"
            + "kC77xbynSYEwAaG+hZ0ZJMiT2uJ+TrZh96gjwIzKaTckQT+osR6MQtoJovYCbk7W\n"
            + "2j+uG3QNHUN0jxLDoe/mO05hIMVe31IZORHBHNkLP0o+3mCGYc+5+dPF10NBWJ3N\n"
            + "ulxcpwrWABuXD0mtutJQw+t3c2xkvzSAwCGOX/6gn9p73liyXsi3CIc6Y/sAwpbu\n"
            + "W1qNfSWgCpJq7V9Hxerc4BHIx/BTWmDOzStjL1BbjEym86C/Ty6wXLX9YvMC7Z49\n"
            + "6A6Cu1EEIwIhAKbzpWBwko+MH4SZL7HRb0Jp1Acq8wGyfBQZDfNBb8U0\n";
    private static final String CERTIFICATE_FOR_DSA_PRIVATE_KEY =
            "MIIFdTCCBF0CCQCvOFMuHFfwHTANBgkqhkiG9w0BAQUFADB3MQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExEjAQBgNVBAcMCUJhbmdhbG9yZTEXMBUGA1UECgwOQWxj\n"
            + "YXRlbCBMdWNlbnQxDTALBgNVBAsMBEZOQkwxHzAdBgNVBAMMFnd3dy5hbGNhdGVs\n"
            + "LWx1Y2VudC5jb20wIBcNMTUwNTA1MDQ1MDE2WhgPMzM4NDA0MTcwNDUwMTZaMFsx\n"
            + "CzAJBgNVBAYTAklOMQswCQYDVQQIDAJLQTEMMAoGA1UEBwwDQkxSMRYwFAYDVQQK\n"
            + "DA1HYW5laHMgQmVlZGlzMRkwFwYDVQQDDBBnYW5lc2hiZWVkaXMuY29tMIIDRzCC\n"
            + "AjoGByqGSM44BAEwggItAoIBAQDQ3bisLVPEXLOyhMLaczI+JHsklx05MeeHeSu6\n"
            + "yV6i925e1psWy51f9h2Sq7ntFr+rPDHXoH7J/vUFMQ8MsXg4OLD9FkA4iIfJ6e3+\n"
            + "Tu2h7HmK5Fjw+UtusD8m/5Zabzt2X1jfX9I+HRZqVf7X9B47b3b9K2t3vRblsaOS\n"
            + "ptDFWJf4tOu1xZPs/GvBQC7p0yeJ7xCyq5dN9ciBPOFKI+nRCeGq44jdnr52+SAA\n"
            + "TFlFbeYn8yLRabMEMdv+PL9lxfxOaYk3iyITPOYl+JWDAxjt6FKiqq8wjtzKG7mO\n"
            + "qT+11z7AYX7lAu87nWGH/KYpyBaOF+zycS+4nfEWxhOExnw3AiEAp7G8G2XlTq/D\n"
            + "MiwO0rbobDzl60BY5QO/SKGTaaUPzdMCggEBAMxzcKaZCJ/KHUSftSAhclvvyN5l\n"
            + "tQ5R4URZXzeHRhChwkgCS17lQh4hdtMd4YeWLY4vvrjX/YTEmwMHkC77xbynSYEw\n"
            + "AaG+hZ0ZJMiT2uJ+TrZh96gjwIzKaTckQT+osR6MQtoJovYCbk7W2j+uG3QNHUN0\n"
            + "jxLDoe/mO05hIMVe31IZORHBHNkLP0o+3mCGYc+5+dPF10NBWJ3NulxcpwrWABuX\n"
            + "D0mtutJQw+t3c2xkvzSAwCGOX/6gn9p73liyXsi3CIc6Y/sAwpbuW1qNfSWgCpJq\n"
            + "7V9Hxerc4BHIx/BTWmDOzStjL1BbjEym86C/Ty6wXLX9YvMC7Z496A6Cu1EDggEF\n"
            + "AAKCAQBvMkLaQZv+NTPJaHYzl4KN3FZCi2k6I0RVSK7ysWeI4+v5pu2q1Hxs47Pp\n"
            + "BUOYPNOosjJEr3q2Lt8uqf4TH1hWD3Is0WMPpCwVidwrBbUBiJ+JgbJA28/6jO3x\n"
            + "yQsDIsRpsIIlJiTQiSlszpjLUbi/WDbv/vtzd+FazO6Ka3Gujefo04zeWYzkG0IV\n"
            + "h9FcpP5IGpKNOpT0C2mNVFKNiIyO7vHmZGF9JornbNPqxuoS1SOsOGA6nhGKxAWG\n"
            + "VbGca4ZVG9w8ANE1hDixxTL/44PVn6zNKjSaNe46DLQNEjrJmMQj3DD3cCFzAsGv\n"
            + "Bp3I+X4FG3BZRuuhm1odZEIircydMA0GCSqGSIb3DQEBBQUAA4IBAQBxr8OO0+DL\n"
            + "1RtkfSPl0uXsfzCeAnSdELTsYGJSreR4uR9kSVVyyWiHoVS12f4mU+pIdH788zZb\n"
            + "xRK8LIu2EIYxQPAvc8W6DuqptAbr7cVUMwsvXAE6yqgNqADpmEqDhB20QTjJ+wAa\n"
            + "rPlWQnRsOOqAshYJOhe/g3sGbneKhM+oUuQBA2XDlcuGShebGDf+/vLECX1oCffv\n"
            + "4ofSAH0onibwutceWxljQeAs5jow9ea6kVWDIZw/s+G2A8nX4uHZwAFAH1pS3BJg\n"
            + "DJS+2nBRIQDip3SajKDf7aTT+UQ9/qbNE+TA+nsURA+lPL97Jb6Y8diulx0YABT1\n" + "2yCuwbsEVTC1\n";

    private static final String DSA_ENCRYPTED_PRIVATE_KEY =
            "MIICkTAbBgkqhkiG9w0BBQMwDgQIrtZQGyieY1kCAggABIICcCWjC93ibQV+0uKg\n"
            + "m8ouRQwu0vlOpKRpdYbYHQNrk+W4zu1g48lPUPx6AR6Ixnt1owwkQvWUE/0434vA\n"
            + "WmABmO0h/yMSBiS4F4numrVL83UMUwUURhN+qGLwT5E5Iv5Ibt9ok7lct+nwiFrS\n"
            + "UFWHw2UijvrXo/VKiYVKsXUuM5/SdiWru7vL7roaDhvxKsWxcCLvTIL4WH+Zf3iw\n"
            + "1dofZWLFUO8J/XjR7TIAdEBBQWYUpMoSvCSHq+x5i6h9rG/E78+tM4qiEEZ4+EYU\n"
            + "p9E0s6gLf/ZqZ+mA4IRieCqFOj9wqgJekheTbqkFE4EBILvEaMmIg1ougv+AMa5W\n"
            + "cX2PghjqJPvVFU4ffsBRKUCcM9P5mR1RANXELiaEVf3PZj2z3ZZeKq5V5kYSowaK\n"
            + "0OW239MpaQ/kK3xr+0/Ay7Uq60jItVm5sSseuixzEI3HYS75BJz8mzfYnQHSbGwg\n"
            + "KieaU1Htk+9jWsvVwGhq9/J44RrMLb0QJlyRVl5G1VqwsQi3TSYVC0BZWUR1q/OY\n"
            + "6MnkR1uYP/Y04Zpl1Ii6Klmn1ALizj+jJ8OEE7nDpR9SeFyL0hbEkNMY5+ixbGDM\n"
            + "7mp/XZGywJhhAVc3Tw/zy7srN0EodpQnXG4gSBghSpOAtNlnBHgqaEJvy9NenjV1\n"
            + "qMpIlZe3IdLVFhMaHgr6d7dsPO1ZZ85D3afGSh6P0JQmvTgZJ7oSiyqKRmRia/SA\n"
            + "kxVEcRBIsD+GUMTZ+zUsHm8Gh6zpHIkJ/GixyH8cu+ZXjH59ledYNTZjKQclZW+Y\n"
            + "jdJH6eViga8DqZtjDNVIZCqbpDaoZ5O+RLzIYdlC6hI4YBQc1w==\n";
    private static final String DSA_ENCRYPTED_PRIVATE_KEY_PASS = "dsapass";
    private static final String CERTIFICATE_FOR_DSA_ENCRYPTED_PRIVATE_KEY =
            "MIIFcjCCBFoCCQCvOFMuHFfwHDANBgkqhkiG9w0BAQUFADB3MQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExEjAQBgNVBAcMCUJhbmdhbG9yZTEXMBUGA1UECgwOQWxj\n"
            + "YXRlbCBMdWNlbnQxDTALBgNVBAsMBEZOQkwxHzAdBgNVBAMMFnd3dy5hbGNhdGVs\n"
            + "LWx1Y2VudC5jb20wIBcNMTUwNTA1MDUwMDI1WhgPMzM4NDA0MTcwNTAwMjVaMFgx\n"
            + "CzAJBgNVBAYTAklOMQswCQYDVQQIDAJLQTEMMAoGA1UEBwwDQkxSMRYwFAYDVQQK\n"
            + "DA1HZW5lc2ggQmVlZGlzMRYwFAYDVQQLDA1XaXRoIHBhc3N3b3JkMIIDRzCCAjoG\n"
            + "ByqGSM44BAEwggItAoIBAQD7IoB6JGBPCqK7QcI8ikuJOJ+iJiyCUno342laHnnn\n"
            + "i5Ap0TufYYQbtSemwu0HNXc3xB9lnVkHWQ7T4s0aGGib8ex8wCJnK3aMtxup/gaT\n"
            + "rjr/GAu8X2zzCOf0ctrJeezkzTEPldduawp93ELq5iHmZOglSl1aZlQbYu8WUx6y\n"
            + "5WZPwlB0v7zNONNchmD3Dut7S1x32b1WaEVPInM8XDb7WxyO+KEQu9EGHLSm/Wc9\n"
            + "mY029qLvGyxqAR21sVAxPRUb/ebjdAIejzOil+FnwvjxNzQADePwrklN2CJ8vsvX\n"
            + "jPNEpX6N94fJmWC11fflmJijzMgPndzITf2tYWAplzC1AiEAp/yPhokPW3HhJWin\n"
            + "/EI0RHlAz3BDEtHBeGnah4InVSsCggEBAMMFKUDTRzKe0JV/0idiGkzZlj7suGtO\n"
            + "lCIB/j6kLoRjd2oFNxIbbAXP7hoRA/wl/Jocjn+akuibNDIWMR+SZGC+eXO2DaFP\n"
            + "Ib12vsERCJpZOvlaGZGz1ebi+h7N4qCEIFEwAuBO49KXFq30dUeF/PYcFemTkoJs\n"
            + "DazRphyNGUOAgmv4T97YGUxnv4nL7fl1jy36mHhUA64fQasE0iAPivT+SV83zHJb\n"
            + "ugVeqbd5Cy5xhqAADXenmRR1llLeE9Mo9MQS4fLwfSHXbv1J25jKWZ549GJiZ0RA\n"
            + "jxI+UnhYH3fKtnSwHTvyXhAZOselSE/bckN3PnImSwcMywfqO1eNjA4DggEFAAKC\n"
            + "AQAirWLI4BiU70zRSJF7mAe0bpgQRj++NJ507S+htE59T0ys71WdPS82HYJ+f5rr\n"
            + "wGZ/XFOy+G4jjNP09vnk1lJxHfa5dhxlpc7xiz1YT91/afl/B+fQp/5mzx/qfbTm\n"
            + "cIei1LSKogk4oW6U7lXVxuWCQG7YsOT9zHiCDe+mzocIgo9zDhpam1bacvEhZKAV\n"
            + "scI46QkptedsHN5Mw5iH/R5SfxYwpTF9SqprsV2dBBe5UkyDk94WSKbHFkCfZTi8\n"
            + "dDlzvr/CJU+Z8kd+kENZQcdCXDMRvTYV0jmJXqtvzCTom7Z4Em51A7ouvDryEjyJ\n"
            + "e6iPIcYxfX3NUxhTe9cvYq7OMA0GCSqGSIb3DQEBBQUAA4IBAQCcwU36bZ8layag\n"
            + "GRG977I7qCAkXgYq1N2/74QlSd5wParPI9cWUtO4NMMRqhIXuEMW1KBAIQMXab+y\n"
            + "E36+HmK+FEQe+64Mfp1FNi0AmGhbXLeJ9P2pH6E/YDQIPJeAz4dXIUubNBMwd4lJ\n"
            + "vBWDGSsuaTOwrsmkkOKL3RkCSYtO+yfqIT8uf6rAvDUtX/ufztq1h1CAdtk6CkSp\n"
            + "6HkyxFteFSdhC7LiThLWViiVdPeIEMaccXr4PBb63BMcryAa8TCExW3CBd8BJ7oS\n"
            + "9f9iVuKRkAG/xJEtyaQgIFOWT3jz2oihLEnXg98oZFch1ClOyH3kbEu0KCiqzWAa\n" + "NzwmblLA\n";
    private static final String RSA_PRIVATE_KEY2 = "-----BEGIN PRIVATE KEY-----\n" +
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
            "-----END PRIVATE KEY-----";
    private static final String CERTIFICATE_FOR_RSA_PRIVATE_KEY2 = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDIDCCAggCCQCUl58dC4o32TANBgkqhkiG9w0BAQsFADBdMQswCQYDVQQGEwJJ\n" +
            "TjESMBAGA1UECAwJS2FybmF0YWthMRIwEAYDVQQHDAlCYW5nYWxvcmUxJjAkBgNV\n" +
            "BAoMHUR1bW15IENlcnRpZmljYXRlIEF1dGhvcml0eSAxMCAXDTE2MDgxMTExMTYy\n" +
            "MloYDzMzODUwNzI1MTExNjIyWjBFMQswCQYDVQQGEwJJTjETMBEGA1UECAwKU29t\n" +
            "ZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIIBIjAN\n" +
            "BgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA9Q6wY0u1HkvWQ3uNmXMrM3PZjObe\n" +
            "dLnv53U/pW1i/FT+h8ERO2Ps2t+qbrfEcuBVcANRrBJCQT3i5/2vWff6gNXe/pfP\n" +
            "WIYoUGmMUxOEP7njKjNERO1ShkXKnsXsD61Bg04m272fdZGOHMGSK9O3U7V6Hh9q\n" +
            "8WxdDjNHFtcy5FgESKcasU9R8K6vKwGiOBZXP0CAEl5IVUs5mHcNLr+9AB5CSnSf\n" +
            "2gYowQ+RVAEgjh35N5oMNMVcvPnQnjWnY7Xs8JaOPOxRagrX4L+5pdxXbbdyA6KY\n" +
            "dJqEEYDWYFRCrf/mAOKScqs3Pq6m+GK8p4twtuIg5c7R/hjbrzl9MLrb0wIDAQAB\n" +
            "MA0GCSqGSIb3DQEBCwUAA4IBAQCJaAO2di/2Z8YCL2hauF1N+LnSibO/uumWm6ei\n" +
            "NC0Tn42uX0762mxnprSqA+Y88uBuZ0NZ5wAYvccm55zL6gNroKQSovAztVW6N0ix\n" +
            "RpMhsT8ni0BfTRw1w3QL93p1+po4TBaVf0s/ucek03N82O5svdLf5XyZXZP+PzUo\n" +
            "eUu6EasQzLThLuEYlZ4R1g+T64lg5PTUaIBiX1VvaaMldwpylWmkGeBDWxfr1v31\n" +
            "XGqwQslROrUP0NbahPlRNs4o7J+7FVkxh46hmUHN9zbuUoBZXDR5TvukkmEuuPCt\n" +
            "KBS+6pBcjZZPfXm7AEL/KNtCDwnDH1YjbY0nqr5Wmdxn7c4y\n" +
            "-----END CERTIFICATE-----\n";

    DynamicX509KeyManager m_x509KeyManager = null;
    private static final Logger LOGGER = Logger.getLogger(DynamicX509TrustManagerImplTest.class);

    @Test
    public void testInitializationFromFile() throws KeyManagerInitException, CertificateException {
        File certificateChainFile = new File(getClass().getResource("/keyMgrTest/certificateChain.crt").getPath());
        File privateKeyFile = new File(getClass().getResource("/keyMgrTest/privateKey.pem").getPath());
        String privateKeyPassword = null;

        m_x509KeyManager = new DynamicX509KeyManagerImpl(certificateChainFile, privateKeyFile, privateKeyPassword);
        PrivateKey key = m_x509KeyManager.getPrivateKey("key");
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());
    }

    @Test
    public void testRsaUnEncryptedPrivateKeyFromFile() throws KeyManagerInitException {
        String rsaPrivateKeyPath = getClass().getResource("/keyMgrTest/rsaPrivateKey.pem").getPath();
        String certificateForRsaPrivateKey = getClass().getResource("/keyMgrTest/certificateForRsaPrivateKey.crt")
                .getPath();
        DynamicX509KeyManager x509KeyManager = new DynamicX509KeyManagerImpl(certificateForRsaPrivateKey,
                rsaPrivateKeyPath, "");

        PrivateKey key = x509KeyManager.getPrivateKey("key");
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        String[] serverAlias = x509KeyManager.getServerAliases("RSA", null);
        assertEquals("key", serverAlias[0]);

        String[] clientAlias = x509KeyManager.getClientAliases("RSA", null);
        assertEquals("key", clientAlias[0]);

        assertEquals("key", x509KeyManager.chooseServerAlias("RSA", (Principal[]) null, (Socket) null));
        assertEquals("key", x509KeyManager.chooseClientAlias(new String[]{"RSA"}, (Principal[]) null, (Socket) null));

        X509Certificate[] certChain = x509KeyManager.getCertificateChain("key");
        assertEquals("SHA1withRSA", certChain[0].getSigAlgName());
        assertEquals("O=netconf peer1, L=BLr, ST=KA, C=IN", certChain[0].getSubjectDN().getName());
    }

    @Test
    public void testDsaUnEncryptedPrivateKeyFromFile() throws KeyManagerInitException {
        String dsaPrivateKeyPath = getClass().getResource("/keyMgrTest/dsaPrivateKey.pem").getPath();
        String certificateForDsaPrivateKey = getClass().getResource("/keyMgrTest/certificateForDsaPrivateKey.crt")
                .getPath();
        DynamicX509KeyManager x509KeyManager = new DynamicX509KeyManagerImpl(certificateForDsaPrivateKey,
                dsaPrivateKeyPath, "");

        PrivateKey key = x509KeyManager.getPrivateKey("key");
        assertEquals("DSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        String[] serverAlias = x509KeyManager.getServerAliases("DSA", null);
        assertEquals("key", serverAlias[0]);

        String[] clientAlias = x509KeyManager.getClientAliases("DSA", null);
        assertEquals("key", clientAlias[0]);

        assertEquals("key", x509KeyManager.chooseServerAlias("DSA", (Principal[]) null, (Socket) null));
        assertEquals("key", x509KeyManager.chooseClientAlias(new String[]{"DSA"}, (Principal[]) null, (Socket) null));

        X509Certificate[] certChain = x509KeyManager.getCertificateChain("key");
        assertEquals("SHA1withRSA", certChain[0].getSigAlgName());
        assertEquals("CN=ganeshbeedis.com, O=Ganehs Beedis, L=BLR, ST=KA, C=IN", certChain[0].getSubjectDN().getName());
    }

    @Test
    public void testRsaUnEncryptedPrivateKey() throws KeyManagerInitException {
        PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(RSA_PRIVATE_KEY, null);
        List<String> keyCertificates = new ArrayList<>();
        keyCertificates.add(CERTIFICATE_FOR_RSA_PRIVATE_KEY);
        m_x509KeyManager = new DynamicX509KeyManagerImpl(keyCertificates, privateKeyInfo);

        PrivateKey key = m_x509KeyManager.getPrivateKey("key");
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        String[] serverAlias = m_x509KeyManager.getServerAliases("RSA", null);
        assertEquals("key", serverAlias[0]);

        String[] clientAlias = m_x509KeyManager.getClientAliases("RSA", null);
        assertEquals("key", clientAlias[0]);

        assertEquals("key", m_x509KeyManager.chooseServerAlias("RSA", (Principal[]) null, (Socket) null));
        assertEquals("key", m_x509KeyManager.chooseClientAlias(new String[]{"RSA"}, (Principal[]) null, (Socket) null));

        X509Certificate[] certChain = m_x509KeyManager.getCertificateChain("key");
        assertEquals("SHA1withRSA", certChain[0].getSigAlgName());
        assertEquals("O=netconf peer1, L=BLr, ST=KA, C=IN", certChain[0].getSubjectDN().getName());
    }

    @Test
    public void testRsaEncryptedPrivateKey() throws KeyManagerInitException {
        PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(RSA_ENCRYPTED_PRIVATE_KEY, RSA_ENCRYPTED_PRIVATE_KEY_PASS);
        List<String> keyCertificate = new ArrayList<>();
        keyCertificate.add(CERTIFICATE_FOR_RSA_ENCRYPTED_PRIVATE_KEY);
        m_x509KeyManager = new DynamicX509KeyManagerImpl(keyCertificate, privateKeyInfo);

        PrivateKey key = m_x509KeyManager.getPrivateKey("key");
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        String[] serverAlias = m_x509KeyManager.getServerAliases("RSA", null);
        assertEquals("key", serverAlias[0]);

        String[] clientAlias = m_x509KeyManager.getClientAliases("RSA", null);
        assertEquals("key", clientAlias[0]);

        assertEquals("key", m_x509KeyManager.chooseServerAlias("RSA", (Principal[]) null, (Socket) null));
        assertEquals("key", m_x509KeyManager.chooseClientAlias(new String[]{"RSA"}, (Principal[]) null, (Socket) null));

        X509Certificate[] certChain = m_x509KeyManager.getCertificateChain("key");
        assertEquals("SHA1withRSA", certChain[0].getSigAlgName());
        assertEquals("O=Default Company Ltd, L=RSA with pass, ST=KA, C=IN", certChain[0].getSubjectDN().getName());
    }

    @Test
    public void testRsaPrivateKeyWithDelimiters() throws KeyManagerInitException {
        PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(RSA_PRIVATE_KEY2, "");
        List<String> keyCertificate = new ArrayList<>();
        keyCertificate.add(CERTIFICATE_FOR_RSA_PRIVATE_KEY2);
        m_x509KeyManager = new DynamicX509KeyManagerImpl(keyCertificate, privateKeyInfo);

        PrivateKey key = m_x509KeyManager.getPrivateKey("key");
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        String[] serverAlias = m_x509KeyManager.getServerAliases("RSA", null);
        assertEquals("key", serverAlias[0]);

        String[] clientAlias = m_x509KeyManager.getClientAliases("RSA", null);
        assertEquals("key", clientAlias[0]);

        assertEquals("key", m_x509KeyManager.chooseServerAlias("RSA", (Principal[]) null, (Socket) null));
        assertEquals("key", m_x509KeyManager.chooseClientAlias(new String[]{"RSA"}, (Principal[]) null, (Socket) null));

        X509Certificate[] certChain = m_x509KeyManager.getCertificateChain("key");
        assertEquals("SHA256withRSA", certChain[0].getSigAlgName());
        assertEquals("O=Internet Widgits Pty Ltd, ST=Some-State, C=IN", certChain[0].getSubjectDN().getName());
    }


    @Test
    public void testDsaUnEncryptedPrivateKey() throws KeyManagerInitException {
        PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(DSA_PRIVATE_KEY, null);
        List<String> keyCertificates = new ArrayList<>();
        keyCertificates.add(CERTIFICATE_FOR_DSA_PRIVATE_KEY);
        m_x509KeyManager = new DynamicX509KeyManagerImpl(keyCertificates, privateKeyInfo);

        PrivateKey key = m_x509KeyManager.getPrivateKey("key");
        assertEquals("DSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        String[] serverAlias = m_x509KeyManager.getServerAliases("DSA", null);
        assertEquals("key", serverAlias[0]);

        String[] clientAlias = m_x509KeyManager.getClientAliases("DSA", null);
        assertEquals("key", clientAlias[0]);

        assertEquals("key", m_x509KeyManager.chooseServerAlias("DSA", (Principal[]) null, (Socket) null));
        assertEquals("key", m_x509KeyManager.chooseClientAlias(new String[]{"DSA"}, (Principal[]) null, (Socket) null));

        X509Certificate[] certChain = m_x509KeyManager.getCertificateChain("key");
        assertEquals("SHA1withRSA", certChain[0].getSigAlgName());
        assertEquals("CN=ganeshbeedis.com, O=Ganehs Beedis, L=BLR, ST=KA, C=IN", certChain[0].getSubjectDN().getName());
    }

    @Test
    public void testDsaEncryptedPrivateKey() throws KeyManagerInitException {
        PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(DSA_ENCRYPTED_PRIVATE_KEY, DSA_ENCRYPTED_PRIVATE_KEY_PASS);
        List<String> keyCertificate = new ArrayList<>();
        keyCertificate.add(CERTIFICATE_FOR_DSA_ENCRYPTED_PRIVATE_KEY);
        m_x509KeyManager = new DynamicX509KeyManagerImpl(keyCertificate, privateKeyInfo);

        PrivateKey key = m_x509KeyManager.getPrivateKey("key");
        assertEquals("DSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        String[] serverAlias = m_x509KeyManager.getServerAliases("DSA", null);
        assertEquals("key", serverAlias[0]);

        String[] clientAlias = m_x509KeyManager.getClientAliases("DSA", null);
        assertEquals("key", clientAlias[0]);

        assertEquals("key", m_x509KeyManager.chooseServerAlias("DSA", (Principal[]) null, (Socket) null));
        assertEquals("key", m_x509KeyManager.chooseClientAlias(new String[]{"DSA"}, (Principal[]) null, (Socket) null));

        X509Certificate[] certChain = m_x509KeyManager.getCertificateChain("key");
        assertEquals("SHA1withRSA", certChain[0].getSigAlgName());
        assertEquals("OU=With password, O=Genesh Beedis, L=BLR, ST=KA, C=IN", certChain[0].getSubjectDN().getName());
    }

    @Test
    public void testKeyManagerGetsReInitialised() throws KeyManagerInitException {
        PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(RSA_PRIVATE_KEY, null);
        List<String> keyCertificates = new ArrayList<>();
        keyCertificates.add(CERTIFICATE_FOR_RSA_PRIVATE_KEY);
        m_x509KeyManager = new DynamicX509KeyManagerImpl(keyCertificates, privateKeyInfo);

        PrivateKey key = m_x509KeyManager.getPrivateKey("key");
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        String[] serverAlias = m_x509KeyManager.getServerAliases("RSA", null);
        assertEquals("key", serverAlias[0]);

        String[] clientAlias = m_x509KeyManager.getClientAliases("RSA", null);
        assertEquals("key", clientAlias[0]);

        assertEquals("key", m_x509KeyManager.chooseServerAlias("RSA", (Principal[]) null, (Socket) null));
        assertEquals("key", m_x509KeyManager.chooseClientAlias(new String[]{"RSA"}, (Principal[]) null, (Socket) null));

        X509Certificate[] certChain = m_x509KeyManager.getCertificateChain("key");
        assertEquals("SHA1withRSA", certChain[0].getSigAlgName());
        assertEquals("O=netconf peer1, L=BLr, ST=KA, C=IN", certChain[0].getSubjectDN().getName());

        // re-initialise with DSA
        privateKeyInfo = new PrivateKeyInfo(DSA_ENCRYPTED_PRIVATE_KEY, DSA_ENCRYPTED_PRIVATE_KEY_PASS);
        List<String> keyCertificate = new ArrayList<>();
        keyCertificate.add(CERTIFICATE_FOR_DSA_ENCRYPTED_PRIVATE_KEY);
        m_x509KeyManager.initKeyManager(keyCertificate, privateKeyInfo);

        key = m_x509KeyManager.getPrivateKey("key");
        assertEquals("DSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        serverAlias = m_x509KeyManager.getServerAliases("DSA", null);
        assertEquals("key", serverAlias[0]);

        clientAlias = m_x509KeyManager.getClientAliases("DSA", null);
        assertEquals("key", clientAlias[0]);

        assertEquals("key", m_x509KeyManager.chooseServerAlias("DSA", (Principal[]) null, (Socket) null));
        assertEquals("key", m_x509KeyManager.chooseClientAlias(new String[]{"DSA"}, (Principal[]) null, (Socket) null));

        certChain = m_x509KeyManager.getCertificateChain("key");
        assertEquals("SHA1withRSA", certChain[0].getSigAlgName());
        assertEquals("OU=With password, O=Genesh Beedis, L=BLR, ST=KA, C=IN", certChain[0].getSubjectDN().getName());
    }
}
