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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

import java.security.KeyException;
import java.security.PrivateKey;

import org.junit.Test;

/**
 * Created by keshava on 4/29/15.
 */
public class KeyUtilTest {
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
    private static final String RSA_PRIVATE_KEY = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDSnMtftTnT3wzG\n"
            + "2Vr8rySgmlVlT03n4jnEObuaU4wnOWJiVFStwl8XhS5Eo8xrYyRFnR4Y+iMeQ7e8\n"
            + "1RjPvzeg99NJKiDlgBVtaf/Yxw4+A4eGaq9QPZALVdXNk1mHYXzusn3+7BWn2Qnu\n"
            + "TXhynuC2iSSUk1N+SXbSQdWnSc2AwPKJeWPM2MjddPnaaqXFh+oRgfNdC1/lvsxW\n"
            + "y2b7ZQTza/5BqkBtc9X4QM5VkNhj1seb+lSVJzq8XUGLycl9FeFVyBlggaR9+xuf\n"
            + "bbLUZU8bxUc1ANdj4wvgGILlC9YWt+H2pbekbGT9Dt9bgrRfIvn1KdEVDZGOyB8I\n"
            + "mb0MOoSVAgMBAAECggEBAKeC1+rGfDj8le/uXoTNVKd8OOF8Iu2ErAdbF9BWFQn5\n"
            + "hTvJ8RPGndVaMwOa9/KNhwLrRU3+XUIsAA5ruvE5GCiqE92EaWe/6Mr+XORw5e4C\n"
            + "5p7NaaS7Yj7xxTxdrv6TVkePpdDEuzCl2lU0PtGDdh0YlQSq7ORrz9rJTAJjG9UD\n"
            + "rqRUNmpLgNHlWgJskIG4BZqIf217s7eAPbLShvcAWOj5MMupaI3AhV7upjeq8P1a\n"
            + "72VQQ1J2cv4w8o9dwq52+OIkO4G2ZJ5cgrEEP7k2Gfe1F/zCdqKGQhJGsChd+94K\n"
            + "59E161smd7Z6rWK5otv0HFu9EM2/CzFBRJSPtPhCb0ECgYEA/xPEs9BM00Ox8KiB\n"
            + "pvP52RzrpWxiZFxcRq50z3uzMImQUTCDcdYx4Jmhiqkk38NI/wqBVsCVOErGIQxV\n"
            + "UY3nrcAQi7FyOEdiLHQDHZATScDqm9SVGiUtPra4rVHK6maHXG4/BQIM570wSnf1\n"
            + "TIDwE6oNkCSExcVjVbkhEL4EX0UCgYEA01/YsXeb3IvY7krIbJbIuZECt057/873\n"
            + "y1afmA5cUe3lqq0KC6gkaWLnl4bp4hU9uJ1/QVPJwpML1Gz3pLWRbBViioQuMLCb\n"
            + "gy2QF3tn9CqYgZymhTKVssuYRcDW3ZK31m+EXS7G/DI/o4hFeO6GJ2chynnKV5gp\n"
            + "LdhRT8C1/RECgYEA6gtUb/zPkQVLLtseY3b3J/x+R4HEYfvQ+1W1fm8tAnsmfSh7\n"
            + "Yyc7Cq9MZvM+D0abItCbzmLUSBtr4gTz/+mpy2YwiEyf1f3BmbI07Zo16HVnjuYm\n"
            + "jR/RfPqhRv6Gpj6/MtNBZbH072lK9vlMjQ69uf1NhpXLcb/knlJIgZkoX9UCgYEA\n"
            + "q/BlVkbTVB22AnB2hE5LTRd2PCHtn/J9grwd6hSUaFi535IOf9jwdYFsncey7KMW\n"
            + "p9wFreA4WLxAfTV+ZE8F0gO7Oq223QhsF9KySUerEeSsVZJVqjexqdUPMn5gybAV\n"
            + "NqtT4nCwkGlXdvDcgjiUi+zKged7/rDZnl8+FT/PSeECgYEAx08ZS4fcYHmpFXqJ\n"
            + "06nMWvm39ien4GD8/YKl2DCNOlgHkeHqVlAtR+d3CwrLIH7tt+W48hEFkP6RVHnB\n"
            + "Ac+vm2DUbyvv/kVS+pnlWU5vAUVAeOE1yDV7mqEdmR0YYFc+wOBk6Iic/9rE6x4i\n" + "MJ99bBNs2Ja7d1AfgStJRTuyHvs=\n";
    private static final String RSA_PRIVATE_KEY_WITHOUT_NEWLINE = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDSnMtftTnT3wzG"
            + "2Vr8rySgmlVlT03n4jnEObuaU4wnOWJiVFStwl8XhS5Eo8xrYyRFnR4Y+iMeQ7e8"
            + "1RjPvzeg99NJKiDlgBVtaf/Yxw4+A4eGaq9QPZALVdXNk1mHYXzusn3+7BWn2Qnu"
            + "TXhynuC2iSSUk1N+SXbSQdWnSc2AwPKJeWPM2MjddPnaaqXFh+oRgfNdC1/lvsxW"
            + "y2b7ZQTza/5BqkBtc9X4QM5VkNhj1seb+lSVJzq8XUGLycl9FeFVyBlggaR9+xuf"
            + "bbLUZU8bxUc1ANdj4wvgGILlC9YWt+H2pbekbGT9Dt9bgrRfIvn1KdEVDZGOyB8I"
            + "mb0MOoSVAgMBAAECggEBAKeC1+rGfDj8le/uXoTNVKd8OOF8Iu2ErAdbF9BWFQn5"
            + "hTvJ8RPGndVaMwOa9/KNhwLrRU3+XUIsAA5ruvE5GCiqE92EaWe/6Mr+XORw5e4C"
            + "5p7NaaS7Yj7xxTxdrv6TVkePpdDEuzCl2lU0PtGDdh0YlQSq7ORrz9rJTAJjG9UD"
            + "rqRUNmpLgNHlWgJskIG4BZqIf217s7eAPbLShvcAWOj5MMupaI3AhV7upjeq8P1a"
            + "72VQQ1J2cv4w8o9dwq52+OIkO4G2ZJ5cgrEEP7k2Gfe1F/zCdqKGQhJGsChd+94K"
            + "59E161smd7Z6rWK5otv0HFu9EM2/CzFBRJSPtPhCb0ECgYEA/xPEs9BM00Ox8KiB"
            + "pvP52RzrpWxiZFxcRq50z3uzMImQUTCDcdYx4Jmhiqkk38NI/wqBVsCVOErGIQxV"
            + "UY3nrcAQi7FyOEdiLHQDHZATScDqm9SVGiUtPra4rVHK6maHXG4/BQIM570wSnf1"
            + "TIDwE6oNkCSExcVjVbkhEL4EX0UCgYEA01/YsXeb3IvY7krIbJbIuZECt057/873"
            + "y1afmA5cUe3lqq0KC6gkaWLnl4bp4hU9uJ1/QVPJwpML1Gz3pLWRbBViioQuMLCb"
            + "gy2QF3tn9CqYgZymhTKVssuYRcDW3ZK31m+EXS7G/DI/o4hFeO6GJ2chynnKV5gp"
            + "LdhRT8C1/RECgYEA6gtUb/zPkQVLLtseY3b3J/x+R4HEYfvQ+1W1fm8tAnsmfSh7"
            + "Yyc7Cq9MZvM+D0abItCbzmLUSBtr4gTz/+mpy2YwiEyf1f3BmbI07Zo16HVnjuYm"
            + "jR/RfPqhRv6Gpj6/MtNBZbH072lK9vlMjQ69uf1NhpXLcb/knlJIgZkoX9UCgYEA"
            + "q/BlVkbTVB22AnB2hE5LTRd2PCHtn/J9grwd6hSUaFi535IOf9jwdYFsncey7KMW"
            + "p9wFreA4WLxAfTV+ZE8F0gO7Oq223QhsF9KySUerEeSsVZJVqjexqdUPMn5gybAV"
            + "NqtT4nCwkGlXdvDcgjiUi+zKged7/rDZnl8+FT/PSeECgYEAx08ZS4fcYHmpFXqJ"
            + "06nMWvm39ien4GD8/YKl2DCNOlgHkeHqVlAtR+d3CwrLIH7tt+W48hEFkP6RVHnB"
            + "Ac+vm2DUbyvv/kVS+pnlWU5vAUVAeOE1yDV7mqEdmR0YYFc+wOBk6Iic/9rE6x4i" + "MJ99bBNs2Ja7d1AfgStJRTuyHvs=";
    private static final String RSA_PRIVATE_KEY_WITH_NEWLINE_AT_END = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDSnMtftTnT3wzG\n"
            + "2Vr8rySgmlVlT03n4jnEObuaU4wnOWJiVFStwl8XhS5Eo8xrYyRFnR4Y+iMeQ7e8\n"
            + "1RjPvzeg99NJKiDlgBVtaf/Yxw4+A4eGaq9QPZALVdXNk1mHYXzusn3+7BWn2Qnu\n"
            + "TXhynuC2iSSUk1N+SXbSQdWnSc2AwPKJeWPM2MjddPnaaqXFh+oRgfNdC1/lvsxW\n"
            + "y2b7ZQTza/5BqkBtc9X4QM5VkNhj1seb+lSVJzq8XUGLycl9FeFVyBlggaR9+xuf\n"
            + "bbLUZU8bxUc1ANdj4wvgGILlC9YWt+H2pbekbGT9Dt9bgrRfIvn1KdEVDZGOyB8I\n"
            + "mb0MOoSVAgMBAAECggEBAKeC1+rGfDj8le/uXoTNVKd8OOF8Iu2ErAdbF9BWFQn5\n"
            + "hTvJ8RPGndVaMwOa9/KNhwLrRU3+XUIsAA5ruvE5GCiqE92EaWe/6Mr+XORw5e4C\n"
            + "5p7NaaS7Yj7xxTxdrv6TVkePpdDEuzCl2lU0PtGDdh0YlQSq7ORrz9rJTAJjG9UD\n"
            + "rqRUNmpLgNHlWgJskIG4BZqIf217s7eAPbLShvcAWOj5MMupaI3AhV7upjeq8P1a\n"
            + "72VQQ1J2cv4w8o9dwq52+OIkO4G2ZJ5cgrEEP7k2Gfe1F/zCdqKGQhJGsChd+94K\n"
            + "59E161smd7Z6rWK5otv0HFu9EM2/CzFBRJSPtPhCb0ECgYEA/xPEs9BM00Ox8KiB\n"
            + "pvP52RzrpWxiZFxcRq50z3uzMImQUTCDcdYx4Jmhiqkk38NI/wqBVsCVOErGIQxV\n"
            + "UY3nrcAQi7FyOEdiLHQDHZATScDqm9SVGiUtPra4rVHK6maHXG4/BQIM570wSnf1\n"
            + "TIDwE6oNkCSExcVjVbkhEL4EX0UCgYEA01/YsXeb3IvY7krIbJbIuZECt057/873\n"
            + "y1afmA5cUe3lqq0KC6gkaWLnl4bp4hU9uJ1/QVPJwpML1Gz3pLWRbBViioQuMLCb\n"
            + "gy2QF3tn9CqYgZymhTKVssuYRcDW3ZK31m+EXS7G/DI/o4hFeO6GJ2chynnKV5gp\n"
            + "LdhRT8C1/RECgYEA6gtUb/zPkQVLLtseY3b3J/x+R4HEYfvQ+1W1fm8tAnsmfSh7\n"
            + "Yyc7Cq9MZvM+D0abItCbzmLUSBtr4gTz/+mpy2YwiEyf1f3BmbI07Zo16HVnjuYm\n"
            + "jR/RfPqhRv6Gpj6/MtNBZbH072lK9vlMjQ69uf1NhpXLcb/knlJIgZkoX9UCgYEA\n"
            + "q/BlVkbTVB22AnB2hE5LTRd2PCHtn/J9grwd6hSUaFi535IOf9jwdYFsncey7KMW\n"
            + "p9wFreA4WLxAfTV+ZE8F0gO7Oq223QhsF9KySUerEeSsVZJVqjexqdUPMn5gybAV\n"
            + "NqtT4nCwkGlXdvDcgjiUi+zKged7/rDZnl8+FT/PSeECgYEAx08ZS4fcYHmpFXqJ\n"
            + "06nMWvm39ien4GD8/YKl2DCNOlgHkeHqVlAtR+d3CwrLIH7tt+W48hEFkP6RVHnB\n"
            + "Ac+vm2DUbyvv/kVS+pnlWU5vAUVAeOE1yDV7mqEdmR0YYFc+wOBk6Iic/9rE6x4i\n" + "MJ99bBNs2Ja7d1AfgStJRTuyHvs=\n";
    private static final String DSA_PRIVATE_KEY = "MIICZQIBADCCAjkGByqGSM44BAEwggIsAoIBAQD6ZjfirdjPsasbMIoWvPqs5lDb\n"
            + "HRdaOyUw3HrG0g3CPgmroEEOwKdMSFFnpFJ53Y07nkjiRTOaejtuBl7rIR7I45P1\n"
            + "wr0p81oIenhFSgiBs6zeUd/DXk5dxq2TtQrvlEZO1GHzxu2w+pxfq44XzdtBbFug\n"
            + "L0e+hqNYKecgUXb5CfAjfXESC2MYRm5dTmUU+PZuPnfvXEV2IIN9AceJ1FuoAcGA\n"
            + "xIxdtwJDxApF6eyiXJFPOMNliONnshqYPkKZVjJmYfTsjCMD9YXjr2GvXH3aTrDp\n"
            + "jBu334mYvmbN2KdaSxszHlNYAAch5Ago6pvEzPu7Liy7WZKld1Gxce4VweEzAiEA\n"
            + "4UYJKu8S8lmAujmHAKKP0tlUJtpqhVRTv0mOjXIFMGkCggEAYzz67h6feBZc4dBO\n"
            + "jxwJlLtXG5cTilt40Qhqcfb48jyRKD5ZEbvBeqpzqIWykjp4oHVz8Yei32A/cdrD\n"
            + "gKQUvJRBdB+qrOTVxt4C/NxG1XxGdf4TXGMilp+5C3SoDYnodWpJnk4KhnHSrudc\n"
            + "HsYbpnnZEKE+NfOCf13mQfFcNQWmsl6q7nIe4hR1q8lqkU80TNIJziHWS+ZQW/kA\n"
            + "TYqpnihkwWOfEBKIwYkfI9WH1Kg6L0CksMoJhjS3WXgJ34Qt6YCvPQdM3pHUtO1m\n"
            + "zlKdJR7+rmBE+7KVFpFdWa4ed4OQ7BrVFx02YYVXll1EraD6Pcy9xy46mcqHyxJE\n"
            + "tFdU4AQjAiEAncfjnOvkORnGSfE0Mn6OLUQcSupOy3PnCT3TVN1dTKE=\n";
    private static final String RSA_ENCRYPTED_PRIVATE_KEY = "MIIE6TAbBgkqhkiG9w0BBQMwDgQIiUVmbHmyy2oCAggABIIEyL7bCrUEOC3fkVx0\n"
            + "idR34r3bsqeYJ5J+FNI7SHXCEqFiKJ3S/54eMPlZ1NpL96lERYiZzA208F+r11ux\n"
            + "GZHgZNyaT5W7MRwlQk6ni7HoxJWuMgqU9Mg+5op50CEBIX7QxhPd3mllyGWZ/QCp\n"
            + "J4X2HSBku7mcX83vJdg2izdRI7gUY9CraHR530jB7duNnBO6Lq4ndwD0owXAdQXz\n"
            + "OOP87dZtyY9n5Gsyy9V17ta61Kf4LcMBRH3SISI+JbHpwCAFWpJzRbIYci11Z5Oo\n"
            + "jJXl8CkG4qyWjRukRwmp01+gnWhruc9xwDso6p/Zhd7XdkUpqBKAwlCfaMRt26nw\n"
            + "jjkrn+FqAcjF1VR/BI+DLr6I1dSHdNbSh4VsVII7YDPtPtwae7Vs0l6e7+WeU5OZ\n"
            + "jF5BYkwPBKbi7VCuXneA5NfCbtFS9cHYKqxRaMuKhAFoYPhaipDmnf4xn2j+P4VF\n"
            + "XBvbBFaX27Q3i9ShQa7MT71cgnoOIQj0C0o3r6Xv+ItpkmcODjSSqBO5G3BmOOp8\n"
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
            + "VokfZ9/++T7p5PsNkktSNR2Z2pSMT/4ZWQK+AMbsxUpklEoG5S3O4zOxLJC+NkQC\n" + "c3ew0ZRhYn6XZcg+Hg==\n";
    private static final String RSA_ENCRYPTED_PRIVATE_KEY_PASS = "rsapass";
    private static final String DSA_ENCRYPTED_PRIVATE_KEY = "MIICkTAbBgkqhkiG9w0BBQMwDgQIf9X4Kbyfqc4CAggABIICcPY0xoeInBPUopv+\n"
            + "DHYslG7tBFWMildMoBykW5gQAhfWuI6Q27JKowa57R5MmexXfykPMouqklBMNR6x\n"
            + "v3Oh+hooQNPpISHcmeXKes+TC0FlTHnrIfGbvlgrxaq0sdxWRE2xjOgyc/uiuGFJ\n"
            + "lzp8hLltAZwihU4HlhPn85gqdgPI8hMH1VBn4BNyt23eiOOIo/SHipPpLt1NE4Q7\n"
            + "aiz0GaZvZD9ku9NysFtuu0QdHJi2jvmrmZaj+ddYnb14bQl+cd4l8MrPB4q9MDp9\n"
            + "jg2m+3Wkzd0CmvqMrzXqEZTsVF5d+QxEbKY7r1aeF4QQj5HpQFWXsDybKk6lAkO4\n"
            + "n4MdBbdIaTBFZSQjOY7m75n45wHdOSVyVUbK66kAbDChA3IqsH+Xjs3uVznWl9kT\n"
            + "7SPDtsjU1UrWUm6EMO0qJQHsucWK8GlXtMqwNsyZZEePPKr+A9vt6JAyfzMXggX7\n"
            + "tlpxjZSZ7V6bnVN/KuA0dh7YwYjQiwAgfEC4Oiqim+wiPeaQCvShm4/AlQk/xcMB\n"
            + "w93dYQdsC5j9Z464SEUF0P0KzopaNoTv0Tklc2iwQHMyTJTaEhf1gt+bMfQtOd+I\n"
            + "dkCxhwNBAcrqjRDtnnWeDd9Uj1E2WpHLUf0cAA/xqW3ecKg2Sp+jR63pKR6bXhww\n"
            + "W6xTjSZHJlV+eznQ6GGdoUbVbuNRntu4tGD5+8Ri1SWAeAd54vKXTpDuyctFCvm+\n"
            + "rHfGlM6Rnr130wi78+OGK7Af1dHKb2gJWoQbmY1EbOwu5SIC1K1aO+lX+nUvpbni\n"
            + "WC7oFbWj72m2qIYc4hlB+9+OssXTLpp3m69dr3Hs0rcKqA/Eyg==\n";
    private static final String DELIMITED_RSA_PRIVATE_KEY = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n"
            + "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDSnMtftTnT3wzG\n"
            + "2Vr8rySgmlVlT03n4jnEObuaU4wnOWJiVFStwl8XhS5Eo8xrYyRFnR4Y+iMeQ7e8\n"
            + "1RjPvzeg99NJKiDlgBVtaf/Yxw4+A4eGaq9QPZALVdXNk1mHYXzusn3+7BWn2Qnu\n"
            + "TXhynuC2iSSUk1N+SXbSQdWnSc2AwPKJeWPM2MjddPnaaqXFh+oRgfNdC1/lvsxW\n"
            + "y2b7ZQTza/5BqkBtc9X4QM5VkNhj1seb+lSVJzq8XUGLycl9FeFVyBlggaR9+xuf\n"
            + "bbLUZU8bxUc1ANdj4wvgGILlC9YWt+H2pbekbGT9Dt9bgrRfIvn1KdEVDZGOyB8I\n"
            + "mb0MOoSVAgMBAAECggEBAKeC1+rGfDj8le/uXoTNVKd8OOF8Iu2ErAdbF9BWFQn5\n"
            + "hTvJ8RPGndVaMwOa9/KNhwLrRU3+XUIsAA5ruvE5GCiqE92EaWe/6Mr+XORw5e4C\n"
            + "5p7NaaS7Yj7xxTxdrv6TVkePpdDEuzCl2lU0PtGDdh0YlQSq7ORrz9rJTAJjG9UD\n"
            + "rqRUNmpLgNHlWgJskIG4BZqIf217s7eAPbLShvcAWOj5MMupaI3AhV7upjeq8P1a\n"
            + "72VQQ1J2cv4w8o9dwq52+OIkO4G2ZJ5cgrEEP7k2Gfe1F/zCdqKGQhJGsChd+94K\n"
            + "59E161smd7Z6rWK5otv0HFu9EM2/CzFBRJSPtPhCb0ECgYEA/xPEs9BM00Ox8KiB\n"
            + "pvP52RzrpWxiZFxcRq50z3uzMImQUTCDcdYx4Jmhiqkk38NI/wqBVsCVOErGIQxV\n"
            + "UY3nrcAQi7FyOEdiLHQDHZATScDqm9SVGiUtPra4rVHK6maHXG4/BQIM570wSnf1\n"
            + "TIDwE6oNkCSExcVjVbkhEL4EX0UCgYEA01/YsXeb3IvY7krIbJbIuZECt057/873\n"
            + "y1afmA5cUe3lqq0KC6gkaWLnl4bp4hU9uJ1/QVPJwpML1Gz3pLWRbBViioQuMLCb\n"
            + "gy2QF3tn9CqYgZymhTKVssuYRcDW3ZK31m+EXS7G/DI/o4hFeO6GJ2chynnKV5gp\n"
            + "LdhRT8C1/RECgYEA6gtUb/zPkQVLLtseY3b3J/x+R4HEYfvQ+1W1fm8tAnsmfSh7\n"
            + "Yyc7Cq9MZvM+D0abItCbzmLUSBtr4gTz/+mpy2YwiEyf1f3BmbI07Zo16HVnjuYm\n"
            + "jR/RfPqhRv6Gpj6/MtNBZbH072lK9vlMjQ69uf1NhpXLcb/knlJIgZkoX9UCgYEA\n"
            + "q/BlVkbTVB22AnB2hE5LTRd2PCHtn/J9grwd6hSUaFi535IOf9jwdYFsncey7KMW\n"
            + "p9wFreA4WLxAfTV+ZE8F0gO7Oq223QhsF9KySUerEeSsVZJVqjexqdUPMn5gybAV\n"
            + "NqtT4nCwkGlXdvDcgjiUi+zKged7/rDZnl8+FT/PSeECgYEAx08ZS4fcYHmpFXqJ\n"
            + "06nMWvm39ien4GD8/YKl2DCNOlgHkeHqVlAtR+d3CwrLIH7tt+W48hEFkP6RVHnB\n"
            + "Ac+vm2DUbyvv/kVS+pnlWU5vAUVAeOE1yDV7mqEdmR0YYFc+wOBk6Iic/9rE6x4i\n" + "MJ99bBNs2Ja7d1AfgStJRTuyHvs=\n"
            + "-----END ENCRYPTED PRIVATE KEY-----";
    private static final String DELIMITED_PRIVATE_KEY_WITH_SPACES = "-----BEGIN ENCRYPTED PRIVATE KEY-----  \n"
            + "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDSnMtftTnT3wzG\n"
            + "2Vr8rySgmlVlT03n4jnEObuaU4wnOWJiVFStwl8XhS5Eo8xrYyRFnR4Y+iMeQ7e8\n"
            + "1RjPvzeg99NJKiDlgBVtaf/Yxw4+\tA4eGaq9QPZALVdXNk1mHYXzusn3+7BWn2Qnu\n"
            + "TXhynuC2iSSUk1N+SXbSQdWnSc2AwPKJeWPM2MjddPnaaqXFh+oRgfNdC1/lvsxW\n"
            + "y2b7ZQTza/5BqkBtc9X4QM5VkNhj1seb+lSVJzq8XUGLycl9FeFVyBlggaR9+xuf\n"
            + "bbLUZU8bxUc1ANdj4wvgGILlC9YWt+H2pbekbGT9Dt9bgrRfIvn1KdEVDZGOyB8I\n"
            + "mb0MOoSVAgMBAAECggEBAKeC1+rGfD\nj8le/uXoTNVKd8OOF8Iu2ErAdbF9BWFQn5\n"
            + "hTvJ8RPGndVaMwOa9/KNhwLrRU3+XUIsAA5ruvE5GCiqE92EaWe/6Mr+XORw5e4C\n"
            + "5p7NaaS7Yj7xxTxdrv6TVkePpdDEuzCl2lU0PtGDdh0YlQSq7ORrz9rJTAJjG9UD\n"
            + "rqRUNmpLgNHlWgJskIG4BZqIf217s7eAPbLShvcAWOj5MMupaI3AhV7upjeq8P1a\n"
            + "72VQQ1J2cv4w8o9dwq52+OIkO4G2ZJ5cgrEEP7k2Gfe1F/zCdqKGQhJGsChd+94K\n"
            + "59E161smd7Z6rWK5o tv0HFu9EM2/CzFBRJSPtPhCb0ECgYEA/xPEs9BM00Ox8KiB\n"
            + "pvP52RzrpWxiZFxcRq50z3uzMImQUTCDcdYx4Jmhiqkk38NI/wqBVsCVOErGIQxV\n"
            + "UY3nrcAQi7FyOEdiLHQDHZATScDqm9SVGiUtPra4rVHK6maHXG4/BQIM570wSnf1\n"
            + "TIDwE6oNkCSExcVjVbkhEL4EX0UCgYEA01/YsXeb3IvY7krIbJbIuZECt057/873\n"
            + "y1afmA5cUe3lqq0KC6gkaWLnl4bp4hU9uJ1/QVPJwpML1Gz3pLWRbBViioQuMLCb\n"
            + "gy2QF3tn9CqYgZymhTKVssuYRcDW3ZK31m+EXS7G/DI/o4hFeO6GJ2chynnKV5gp\n"
            + "LdhRT8C1/RECgYEA6gtUb/zPkQVLLtseY3b3J/x+R4HEYfvQ+1W1fm8tAnsmfSh7\n"
            + "Yyc7Cq9MZvM+D0abItCbzmLUSBtr4gTz/+mpy2YwiEyf1f3BmbI07Zo16HVnjuYm\n"
            + "jR/RfPqhRv6Gpj6/MtNBZbH072lK9vlMjQ69uf1NhpXLcb/knlJIgZkoX9UCgYEA\n"
            + "q/BlVkbTVB22AnB2hE5LTRd2PCHtn/J9grwd6hSUaFi535IOf9jwdYFsncey7KMW\n"
            + "p9wFreA4WLxAfTV+ZE8F0gO7Oq223QhsF9KySUerEeSsVZJVqjexqdUPMn5gybAV\n"
            + "NqtT4nCwkGlXdvDcgjiUi+zKged7/rDZnl8+FT/PSeECgYEAx08ZS4fcYHmpFXqJ\n"
            + "06nMWvm39ien4GD8/YKl2DCNOlgHkeHqVlAtR+d3CwrLIH7tt+W48hEFkP6RVHnB\n"
            + "Ac+vm2DUbyvv/kVS+pnlWU5vAUVAeOE1yDV7mqEdmR0YYFc+wOBk6Iic/9rE6x4i\n" + "MJ99bBNs2Ja7d1AfgStJRTuyHvs=  \n"
            + "-----END ENCRYPTED PRIVATE KEY-----";
    private static final String DSA_ENCRYPTED_PRIVATE_KEY_PASS = "dsapass";

    private static final String DELIMITED_RSA_PRIVATE_KEY2 = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpQIBAAKCAQEA9Q6wY0u1HkvWQ3uNmXMrM3PZjObedLnv53U/pW1i/FT+h8ER\n" +
            "O2Ps2t+qbrfEcuBVcANRrBJCQT3i5/2vWff6gNXe/pfPWIYoUGmMUxOEP7njKjNE\n" +
            "RO1ShkXKnsXsD61Bg04m272fdZGOHMGSK9O3U7V6Hh9q8WxdDjNHFtcy5FgESKca\n" +
            "sU9R8K6vKwGiOBZXP0CAEl5IVUs5mHcNLr+9AB5CSnSf2gYowQ+RVAEgjh35N5oM\n" +
            "NMVcvPnQnjWnY7Xs8JaOPOxRagrX4L+5pdxXbbdyA6KYdJqEEYDWYFRCrf/mAOKS\n" +
            "cqs3Pq6m+GK8p4twtuIg5c7R/hjbrzl9MLrb0wIDAQABAoIBAGY84J4sob2oChZR\n" +
            "X7wbbnSi6qp8T2cHg+1e31TXFCsOErr0c4oBoHmYQvS6On1q1npLCDHv1DB/70C2\n" +
            "eAfjC2Hg2I4DLABUyFD0GBU42T31Aa9DgEH5eSqHx4cCMABCWki7vD6FGBfmyBF7\n" +
            "OM6MH7oOxon2tZlPims4PXpsAdF9jhqttQfAhbvaXWcxkE3wIEzRZ57yJtjzZbYg\n" +
            "EGlSP9ku0PzQA0t4BqTVqm2Pyt77weMUiEwWmqYdFj/3jRGdBVqVuW6QATuENgN0\n" +
            "seTApyrfgx8pgshrs2L0RphoRZrW3Qe+9Z+bxn1kWEizXI8uYclAnhE9bLmLHj1x\n" +
            "uofMPDECgYEA/Qlh3aNANtF52dYiHz/pAIHJSMTxm9ikUAdojudPzSyC8L48+SmQ\n" +
            "aZGLPTT3QH5BPrd8172FUMOQAQVLj5+gPb1dGsrPUtK2YSsRGImdOWWwSAnZEzGI\n" +
            "IAk2Q180LGbW6QPADDyNQUhdU102yzPfi8EPi4m2NHN2eeBlZ0D48m0CgYEA9+1i\n" +
            "an0bZRXOHFgCjV3tzNDjF1DHTvXJZidVQMB+QEceqNT+vhUnnzGmFE+hQBSYDY9j\n" +
            "8CEyY4d8+5MEhh/vC0dFBfjjTWTGi6XyJE7p0noxUNNMwFLfMkDPEDLQvu46H6Ay\n" +
            "7xbQLEsebtej9qvXB4zXlu8d7xCBnS6cbtUMHz8CgYEAhaLS4O87FriDFh0VYFOs\n" +
            "huwElj4NLDW6cdm3yuE3MDD15pdSNHVTI64OT4ENpC4J30mg0X/yTLk78I4PXgb7\n" +
            "5h+AcInz/NX0JFVObhsfwhSGiUVKM+zQ67pxwhshvuGBVwb4An5oS7YgM8rjCWaY\n" +
            "BYMs5pEQfZsNU0jDxUh3MfECgYEArz+AIo++boykPYrEexLbBbzd2NxDlf/M1cWV\n" +
            "0IiAdYUQGf08+DXR5QheQdsruzUTafpihRmiGZJq+RUpzHkEKq+9DYSpf0ptwcaS\n" +
            "S7HwO9QdyekiRowNsuL2upeA0IVqVnKRrkks6zJKF8Wb48AB5hdbSRhKy3Kae/W3\n" +
            "o+w6flcCgYEAzhSJXxUUu1cAOSj4Nnrns72kc+dXiTiCKkbSjMxi6BbCAE+lKE2g\n" +
            "W2M+p5Oaj61l2uuAVlOQkjRDV7/JUJQSIj9tLkYBCSnqvwAyvVuV5FDfM7yqifmE\n" +
            "8qtd/UcroENYinqNr1PAFvFuuN77XPPwJndT5K/+lKFRkb80BbH9eoA=\n" +
            "-----END RSA PRIVATE KEY-----";

    @Test
    public void testIsDelimited() {
        assertTrue(KeyUtil.isDelimited(DELIMITED_RSA_PRIVATE_KEY));
        assertTrue(KeyUtil.isDelimited(DELIMITED_RSA_PRIVATE_KEY2));
        assertFalse(KeyUtil.isDelimited(RSA_ENCRYPTED_PRIVATE_KEY));
    }

    @Test
    public void testUnEncryptedPrivateKeysAreRead() throws KeyException {
        PrivateKey privateKey = KeyUtil.getPrivateKey(RSA_PRIVATE_KEY, "");
        assertEquals("PKCS#8", privateKey.getFormat());
        assertEquals("RSA", privateKey.getAlgorithm());

        privateKey = KeyUtil.getPrivateKey(RSA_PRIVATE_KEY_WITH_NEWLINE_AT_END, "");
        assertEquals("PKCS#8", privateKey.getFormat());
        assertEquals("RSA", privateKey.getAlgorithm());

        privateKey = KeyUtil.getPrivateKey(RSA_PRIVATE_KEY_WITHOUT_NEWLINE, "");
        assertEquals("PKCS#8", privateKey.getFormat());
        assertEquals("RSA", privateKey.getAlgorithm());

        privateKey = KeyUtil.getPrivateKey(DSA_PRIVATE_KEY, "");
        assertEquals("PKCS#8", privateKey.getFormat());
        assertEquals("DSA", privateKey.getAlgorithm());
    }

    @Test
    public void testUnEncryptedPrivateKeysAreNotReadWithPassword() throws KeyException {
        try {
            KeyUtil.getPrivateKey(RSA_PRIVATE_KEY, "blahblah");
            fail("The KeyUtil should have failed");
        } catch (KeyException e) {
            // we are ok
        }

        try {
            KeyUtil.getPrivateKey(DSA_PRIVATE_KEY, "blahblah");
            fail("The KeyUtil should have failed");
        } catch (KeyException e) {
            // we are ok
        }
    }

    @Test
    public void testEncryptedPrivateKeysAreRead() throws KeyException {
        PrivateKey privateKey = KeyUtil.getPrivateKey(RSA_ENCRYPTED_PRIVATE_KEY, RSA_ENCRYPTED_PRIVATE_KEY_PASS);
        assertEquals("PKCS#8", privateKey.getFormat());
        assertEquals("RSA", privateKey.getAlgorithm());

        privateKey = KeyUtil.getPrivateKey(DSA_ENCRYPTED_PRIVATE_KEY, DSA_ENCRYPTED_PRIVATE_KEY_PASS);
        assertEquals("PKCS#8", privateKey.getFormat());
        assertEquals("DSA", privateKey.getAlgorithm());
    }

    @Test
    public void testEncryptedPrivateKeysAreNotReadWithWrongPassword() {
        try {
            KeyUtil.getPrivateKey(RSA_ENCRYPTED_PRIVATE_KEY, "blahblah");
            fail("The KeyUtil should have failed");
        } catch (KeyException e) {
            // we are ok
        }

        try {
            KeyUtil.getPrivateKey(DSA_ENCRYPTED_PRIVATE_KEY, "blahblah");
            fail("The KeyUtil should have failed");
        } catch (KeyException e) {
            // we are ok
        }
    }

    @Test
    public void testInvalidPrivateKeysAreNotRead() {
        try {
            KeyUtil.getPrivateKey(INVALID_KEY, "blahblah");
            fail("The KeyUtil should have failed");
        } catch (KeyException e) {
            // we are ok
        }
    }

    @Test
    public void testDelimiterBasedCertificateString() throws KeyException {

        PrivateKey privateKey = KeyUtil.getPrivateKeyWithDelimiter(DELIMITED_RSA_PRIVATE_KEY, "");
        assertEquals("PKCS#8", privateKey.getFormat());
        assertEquals("RSA", privateKey.getAlgorithm());
    }

    @Test
    public void testPrivateKeyWithSpaces() throws KeyException {
        PrivateKey privateKey = KeyUtil.getPrivateKeyWithDelimiter(DELIMITED_PRIVATE_KEY_WITH_SPACES, "");
        assertEquals("PKCS#8", privateKey.getFormat());
        assertEquals("RSA", privateKey.getAlgorithm());
    }
}
