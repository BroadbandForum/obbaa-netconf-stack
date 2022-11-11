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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.junit.Test;

/**
 * Created by keshava on 4/28/15.
 */
public class CertificateUtilTest {
    private static final String CERT_1 = "MIIC8zCCAdsCCQDO9DO6yC63WzANBgkqhkiG9w0BAQUFADBGMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEcMBoGA1UECgwTRGVmYXVsdCBD\n"
            + "b21wYW55IEx0ZDAgFw0xNTAzMzExMDE4MDJaGA8yMTUyMDIyMTEwMTgwMlowLzEQ\n"
            + "MA4GA1UECgwHQWxjYXRlbDENMAsGA1UECwwERk5CTDEMMAoGA1UEAwwDQUxVMIIB\n"
            + "IjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtnou32AHsG8iq6rIVJp+ib8V\n"
            + "4PmwsfrSKV7lxYahTmmHr65owdaz0NgNpSTjjbqi5cVlYJpSKHCKz8OHYv3B8Vif\n"
            + "UE+BKzFpRDfbW5zXiiExpQRiOwsI8dRyuCqdSp86Ac2sPm2wT8lL8zJ7pxHchJCy\n"
            + "J7sFPUhb8btr4IZgOUFWFQohqDsAifzYoiY4f/vKGgEY2oRd7Os19F8SKWRnQBYS\n"
            + "JsZBqsoHHdXvGctlp+/ezIhgUNBPCj9/hdEX4YbarZTxFbvN3+a6iX743KLExq5E\n"
            + "hYfCbT3AoQ0iJZE0ypL00JhvA3SIWqwpDhBjPSEYT3/r3mQO6eW0iwLVH7jdLQID\n"
            + "AQABMA0GCSqGSIb3DQEBBQUAA4IBAQBpQaQodFna6ainoLqvlD2phY80LH4t+2JE\n"
            + "X/AUE0+cHoqdfiaNw1siwJejDqs9ihPKl0zB2yBDljrvifIMBfOQML5qu9eccF2q\n"
            + "6UOFJImh8v60BDRfWW1CyOXrDt2zM3K6/Gx+mIy/nRNN+md8ROXrkrV4u0GJwAql\n"
            + "9O+zzzrPYP45LLJPmA+am8LfeH94RTZoNNSOTVBwgv5ib7Mvx9T6LcbUoW+E1jgk\n"
            + "NO6//1JqnHf/C/JwS3NF3GBB99B2BEEsAYB5nwcfAIUuVR6uZPuv7NmDOO4/Nm8Q\n"
            + "8iRLCrK4A1Ch0UFkLnmMcWPeMXd9cFcweMMD/p6WODC7Yxa1bTJj\n";
    private static final String CERT_2 = "MIIDIjCCAgoCCQDO9DO6yC63WjANBgkqhkiG9w0BAQUFADBGMQswCQYDVQQGEwJJ"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEcMBoGA1UECgwTRGVmYXVsdCBD"
            + "b21wYW55IEx0ZDAgFw0xNTAzMzEwOTAyNTJaGA8yMTUyMDIyMTA5MDI1MlowXjEL"
            + "MAkGA1UEBhMCSU4xCzAJBgNVBAgMAktBMQwwCgYDVQQHDANCTFIxDDAKBgNVBAoM"
            + "A0FMVTENMAsGA1UECwwERk5CTDEXMBUGA1UEAwwOMTM1LjI1MC4xNy4xMDAwggEi"
            + "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCo0e6xJZ2jKYE1ewed+VFPKmx6"
            + "YdZPCTKKZqzGA0VgrDNpi4VYJtw45w51niftZ8S810dynEzM4mjVanNUTrL/FyVT"
            + "q/FIbmUA2UGizkt1+8hh1Dd7q3wQDk360o7MhkltWBx+visGxT7EjAsOY/WIVq/A"
            + "C7WpRAuS8yg59QB49pmFe1kXOnBsWzEDienZeUoVQHqoLOpv1BYMJEYh9bdq7R3G"
            + "gX0/L8SNNnVbFePGKKIYgKApNvRGDJxVbE97de038C6VDsoZSAbxXvUayJ49+Ppl"
            + "Do3y3DZWoAPrkKWqzMt6MCm2i0loebQe4+H/AV2iEB5Rs8ypEliYR60Aj4qvAgMB"
            + "AAEwDQYJKoZIhvcNAQEFBQADggEBAATeGl17FWADCqWZ5seA816J24hliA/r1vuf"
            + "Sk/7GSDCm3V8bX6qUTPMEWcuLOw727ySl4nrl4wfhAm4ZdIess4DMwCsJsu4LxpA"
            + "OWK5GYQDuvicmMr2njTjw9cjnweSWHdNCltct5EcazjfuDEdj6TZFi6ZWLbBk3pE"
            + "x2xoe6Cry59PKlgzzkUWgwbXA/KB0KBzSif9BFfwFxmUOfaklBrTwjwfZuAkYXa2"
            + "Ce/wGXz2wMc3AOW26Jt0mrkFUb6e417vZfdgjjdwqxbY2JAWCn+7KCrAmeeXDH8G" + "JHOJLRLRJSs9SzbLbfMT1DwlRovUA0GHjLz2xAiMSD//z+wUpig=";
    private static final String CERT_3 = "MIIDIjCCAgoCCQDO9DO6yC63WjANBgkqhkiG9w0BAQUFADBGMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEcMBoGA1UECgwTRGVmYXVsdCBD\n"
            + "b21wYW55IEx0ZDAgFw0xNTAzMzEwOTAyNTJaGA8yMTUyMDIyMTA5MDI1MlowXjEL\n"
            + "MAkGA1UEBhMCSU4xCzAJBgNVBAgMAktBMQwwCgYDVQQHDANCTFIxDDAKBgNVBAoM\n"
            + "A0FMVTENMAsGA1UECwwERk5CTDEXMBUGA1UEAwwOMTM1LjI1MC4xNy4xMDAwggEi\n"
            + "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCo0e6xJZ2jKYE1ewed+VFPKmx6\n"
            + "YdZPCTKKZqzGA0VgrDNpi4VYJtw45w51niftZ8S810dynEzM4mjVanNUTrL/FyVT\n"
            + "q/FIbmUA2UGizkt1+8hh1Dd7q3wQDk360o7MhkltWBx+visGxT7EjAsOY/WIVq/A\n"
            + "C7WpRAuS8yg59QB49pmFe1kXOnBsWzEDienZeUoVQHqoLOpv1BYMJEYh9bdq7R3G\n"
            + "gX0/L8SNNnVbFePGKKIYgKApNvRGDJxVbE97de038C6VDsoZSAbxXvUayJ49+Ppl\n"
            + "Do3y3DZWoAPrkKWqzMt6MCm2i0loebQe4+H/AV2iEB5Rs8ypEliYR60Aj4qvAgMB\n"
            + "AAEwDQYJKoZIhvcNAQEFBQADggEBAATeGl17FWADCqWZ5seA816J24hliA/r1vuf\n"
            + "Sk/7GSDCm3V8bX6qUTPMEWcuLOw727ySl4nrl4wfhAm4ZdIess4DMwCsJsu4LxpA\n"
            + "OWK5GYQDuvicmMr2njTjw9cjnweSWHdNCltct5EcazjfuDEdj6TZFi6ZWLbBk3pE\n"
            + "x2xoe6Cry59PKlgzzkUWgwbXA/KB0KBzSif9BFfwFxmUOfaklBrTwjwfZuAkYXa2\n"
            + "Ce/wGXz2wMc3AOW26Jt0mrkFUb6e417vZfdgjjdwqxbY2JAWCn+7KCrAmeeXDH8G\n"
            + "JHOJLRLRJSs9SzbLbfMT1DwlRovUA0GHjLz2xAiMSD//z+wUpig=\n";
    private static final String INVALID_CERT_1 = "-----BEGIN CERTIFICATE-----\n"
            + "MIIC8zCCAdsCCQDO9DO6yC63WzANBgkqhkiG9w0BAQUFADBGMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEcMBoGA1UECgwTRGVmYXVsdCBD\n"
            + "b21wYW55IEx0ZDAgFw0xNTAzMzExMDE4MDJaGA8yMTUyMDIyMTEwMTgwMlowLzEQ\n"
            + "MA4GA1UECgwHQWxjYXRlbDENMAsGA1UECwwERk5CTDEMMAoGA1UEAwwDQUxVMIIB\n"
            + "IjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtnou32AHsG8iq6rIVJp+ib8V\n"
            + "4PmwsfrSKV7lxYahTmmHr65owdaz0NgNpSTjjbqi5cVlYJpSKHCKz8OHYv3B8Vif\n"
            + "UE+BKzFpRDfbW5zXiiExpQRiOwsI8dRyuCqdSp86Ac2sPm2wT8lL8zJ7pxHchJCy\n"
            + "J7sFPUhb8btr4IZgOUFWFQohqDsAifzYoiY4f/vKGgEY2oRd7Os19F8SKWRnQBYS\n"
            + "JsZBqsoHHdXvGctlp+/ezIhgUNBPCj9/hdEX4YbarZTxFbvN3+a6iX743KLExq5E\n"
            + "hYfCbT3AoQ0iJZE0ypL00JhvA3SIWqwpDhBjPSEYT3/r3mQO6eW0iwLVH7jdLQID\n"
            + "AQABMA0GCSqGSIb3DQEBBQUAA4IBAQBpQaQodFna6ainoLqvlD2phY80LH4t+2JE\n"
            + "X/AUE0+cHoqdfiaNw1siwJejDqs9ihPKl0zB2yBDljrvifIMBfOQML5qu9eccF2q\n"
            + "6UOFJImh8v60BDRfWW1CyOXrDt2zM3K6/Gx+mIy/nRNN+md8ROXrkrV4u0GJwAql\n"
            + "9O+zzzrPYP45LLJPmA+am8LfeH94RTZoNNSOTVBwgv5ib7Mvx9T6LcbUoW+E1jgk\n"
            + "NO6//1JqnHf/C/JwS3NF3GBB99B2BEEsAYB5nwcfAIUuVR6uZPuv7NmDOO4/Nm8Q\n" + "8iRLCrK4A1Ch0UFkLnmMcWPeMXd9cFcweMMD/p6WODC7Yxa1bTJj";
    private static final String INVALID_CERT_2 = "MIIDIjCCAgoCCQDO9DO6yC63WjANBgkqhkiG9w0BAQUFADBGMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEcMBoGA1UECgwTRGVmYXVsdCBD\n"
            + "b21wYW55IEx0ZDAgFw0xNTAzMzEwOTAyNTJaGA8yMTUyMDIyMTA5MDI1MlowXjEL\n"
            + "MAkGA1UEBhMCSU4xCzAJBgNVBAgMAktBMQwwCgYDVQQHDANCTFIxDDAKBgNVBAoM\n"
            + "A0FMVTENMAsGA1UECwwERk5CTDEXMBUGA1UEAwwOMTM1LjI1MC4xNy4xMDAwggEi\n"
            + "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCo0e6xJZ2jKYE1ewed+VFPKmx6\n"
            + "YdZPCTKKZqzGA0VgrDNpi4VYJtw45w51niftZ8S810dynEzM4mjVanNUTrL/FyVT\n"
            + "q/FIbmUA2UGizkt1+8hh1Dd7q3wQDk360o7MhkltWBx+visGxT7EjAsOY/WIVq/A\n"
            + "C7WpRAuS8yg59QB49pmFe1kXOnBsWzEDienZeUoVQHqoLOpv1BYMJEYh9bdq7R3G\n"
            + "gX0/L8SNNnVbFePGKKIYgKApNvRGDJxVbE97de038C6VDsoZSAbxXvUayJ49+Ppl\n"
            + "Do3y3DZWoAPrkKWqzMt6MCm2i0loebQe4+H/AV2iEB5Rs8ypEliYR60Aj4qvAgMB\n"
            + "AAEwDQYJKoZIhvcNAQEFBQADggEBAATeGl17FWADCqWZ5seA816J24hliA/r1vuf\n"
            + "Sk/7GSDCm3V8bX6qUTPMEWcuLOw727ySl4nrl4wfhAm4ZdIess4DMwCsJsu4LxpA\n"
            + "OWK5GYQDuvicmMr2njTjw9cjnweSWHdNCltct5EcazjfuDEdj6TZFi6ZWLbBk3pE\n"
            + "x2xoe6Cry59PKlgzzkUWgwbXA/KB0KBzSif9BFfwFxmUOfaklBrTwjwfZuAkYXa2\n"
            + "Ce/wGXz2wMc3AOW26Jt0mrkFUb6e417vZfdgjjdwqxbY2JAWCn+7KCrAmeeXDH8G\n"
            + "JHOJLRLRJSs9SzbLbfMT1DwlRovUA0GHjLz2xAiMSD//z+wUpig=\n" + "-----END CERTIFICATE-----";

    private static final String DELIMITED_CERT_1 = "-----BEGIN CERTIFICATE-----\n"
            + "MIIC8zCCAdsCCQDO9DO6yC63WzANBgkqhkiG9w0BAQUFADBGMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEcMBoGA1UECgwTRGVmYXVsdCBD\n"
            + "b21wYW55IEx0ZDAgFw0xNTAzMzExMDE4MDJaGA8yMTUyMDIyMTEwMTgwMlowLzEQ\n"
            + "MA4GA1UECgwHQWxjYXRlbDENMAsGA1UECwwERk5CTDEMMAoGA1UEAwwDQUxVMIIB\n"
            + "IjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtnou32AHsG8iq6rIVJp+ib8V\n"
            + "4PmwsfrSKV7lxYahTmmHr65owdaz0NgNpSTjjbqi5cVlYJpSKHCKz8OHYv3B8Vif\n"
            + "UE+BKzFpRDfbW5zXiiExpQRiOwsI8dRyuCqdSp86Ac2sPm2wT8lL8zJ7pxHchJCy\n"
            + "J7sFPUhb8btr4IZgOUFWFQohqDsAifzYoiY4f/vKGgEY2oRd7Os19F8SKWRnQBYS\n"
            + "JsZBqsoHHdXvGctlp+/ezIhgUNBPCj9/hdEX4YbarZTxFbvN3+a6iX743KLExq5E\n"
            + "hYfCbT3AoQ0iJZE0ypL00JhvA3SIWqwpDhBjPSEYT3/r3mQO6eW0iwLVH7jdLQID\n"
            + "AQABMA0GCSqGSIb3DQEBBQUAA4IBAQBpQaQodFna6ainoLqvlD2phY80LH4t+2JE\n"
            + "X/AUE0+cHoqdfiaNw1siwJejDqs9ihPKl0zB2yBDljrvifIMBfOQML5qu9eccF2q\n"
            + "6UOFJImh8v60BDRfWW1CyOXrDt2zM3K6/Gx+mIy/nRNN+md8ROXrkrV4u0GJwAql\n"
            + "9O+zzzrPYP45LLJPmA+am8LfeH94RTZoNNSOTVBwgv5ib7Mvx9T6LcbUoW+E1jgk\n"
            + "NO6//1JqnHf/C/JwS3NF3GBB99B2BEEsAYB5nwcfAIUuVR6uZPuv7NmDOO4/Nm8Q\n"
            + "8iRLCrK4A1Ch0UFkLnmMcWPeMXd9cFcweMMD/p6WODC7Yxa1bTJj\n" + "-----END CERTIFICATE-----";

    private static final String DELIMITED_CERT_2 = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDIjCCAgoCCQDO9DO6yC63WjANBgkqhkiG9w0BAQUFADBGMQswCQYDVQQGEwJJ"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEcMBoGA1UECgwTRGVmYXVsdCBD"
            + "b21wYW55IEx0ZDAgFw0xNTAzMzEwOTAyNTJaGA8yMTUyMDIyMTA5MDI1MlowXjEL"
            + "MAkGA1UEBhMCSU4xCzAJBgNVBAgMAktBMQwwCgYDVQQHDANCTFIxDDAKBgNVBAoM"
            + "A0FMVTENMAsGA1UECwwERk5CTDEXMBUGA1UEAwwOMTM1LjI1MC4xNy4xMDAwggEi"
            + "MA0GCSqGSIb3DQEBAQUA  A4IBDwAwggEKAoIBAQCo0e6xJZ2jKYE1ewed+VFPKmx6"
            + "YdZPCTKKZqzGA0VgrDNpi4VYJtw45w51niftZ8S810dynEzM4mjVanNUTrL/FyVT"
            + "q/FIbmUA2UGizkt1+8hh1Dd7q3wQDk360o7MhkltWBx+visGxT7EjAsOY/WIVq/A"
            + "C7WpRAuS8yg59QB49pmFe1kXOnBsWzEDienZeUoVQHqoLOpv1BYMJEYh9bdq7R3G"
            + "gX0/L8SNNnVbFePGKKIYgKApNvRGDJ\txVbE97de038C6VDsoZSAbxXvUayJ49+Ppl"
            + "Do3y3DZWoAPrkKWqzMt6MCm2i0loebQe4+H/AV2iEB5Rs8ypEliYR60Aj4qvAgMB"
            + "AAEwDQYJKoZIhvcNAQEFBQADggEBAATeGl17FWADCqWZ5seA816J24hliA/r1vuf"
            + "Sk/7GSDCm3V8bX6qUTPMEWcuLOw727ySl4nrl4wfhAm4ZdIess4DMwCsJsu4LxpA"
            + "OWK5GYQDuvicmMr2njTjw9cjnweSWHdNCltct5EcazjfuDEdj6TZFi6ZWLbBk3pE"
            + "x2xoe6Cry59PKlgzzkUWgwbXA/KB0KBzSif9BFfwFxmUOfaklBrTwjwfZuAkYXa2"
            + "Ce/wGXz2wMc3AOW26Jt0mrkFUb6e417vZfdgjjdwqxbY2JAWCn+7KCrAmeeXDH8G"
            + "JHOJLRLRJSs9SzbLbfMT1DwlRovUA0GHjLz2xAiMSD//z+wUpig=" + "-----END CERTIFICATE-----";

    private static final String PEER_CERT = "MIIDpDCCAowCCQCBGDthXaSq4zANBgkqhkiG9w0BAQsFADCBjDELMAkGA1UEBhMC\n"
            + "SU4xEjAQBgNVBAgMCVRhbWlsbmFkdTEQMA4GA1UEBwwHQ2hlbm5haTEUMBIGA1UE\n"
            + "CgwLQ0EgUHZ0LiBMdGQxFjAUBgNVBAsMDUNBIERlcGFydG1lbnQxDzANBgNVBAMM\n"
            + "BmNhX2NydDEYMBYGCSqGSIb3DQEJARYJY2FAY2EuY29tMB4XDTE2MDYzMDE4MTM0\n"
            + "MloXDTI2MDYyODE4MTM0MlowgZoxCzAJBgNVBAYTAklOMRIwEAYDVQQIDAlUYW1p\n"
            + "bG5hZHUxEDAOBgNVBAcMB0NoZW5uYWkxFjAUBgNVBAoMDVBlZXIgUHZ0LiBMdGQx\n"
            + "GDAWBgNVBAsMD1BlZXIgRGVwYXJ0bWVudDEVMBMGA1UEAwwMd3d3LnBlZXIuY29t\n"
            + "MRwwGgYJKoZIhvcNAQkBFg1wZWVyQHBlZXIuY29tMIIBIjANBgkqhkiG9w0BAQEF\n"
            + "AAOCAQ8AMIIBCgKCAQEAycpdrFEoCWFX2HanijnmvMP3Pb583mhtfVflfSRR2a3t\n"
            + "ici/p+AQgymI81SfxA2SXC3GN+I4px8dN3k+XqFWJYYGlDfYpjP8bjoX6dVF/L+U\n"
            + "kf542xBuQlhwGb7gOE/vP1Dj7WtvsUiwL0bBosC4+vuBPR5tjMr1B6HKityF1m3H\n"
            + "MKuHQtMoSegfLuzgGPDQUmVscPqU93f3TNY1ynB71K1Wp3HVTSkPGO0gjVAu+pFR\n"
            + "vCIgp3p6P/Pfu0W26rZOy+FDSwsHGY7Ax14dXU5MI1x1hNpbblC5C4kNBu9FxMtH\n"
            + "76IIVNNK1yiaQN+MswkxzzmB2QruUsNyAddXEyP6pwIDAQABMA0GCSqGSIb3DQEB\n"
            + "CwUAA4IBAQB+s+4+sRLk0gndnkF94Jo0RFkFjVqz71QtATcaD4GgCYWeV+LE1/9e\n"
            + "a8+/cC97Cdw0mvQBqEjC5ABc0VcTgnokN7UYCuIRXbI+eWdIkZkfGW4HcziTPDv2\n"
            + "ZooKXrqREUpBE4d3v+4HR/Kn57FKCDIP8jyiO5oRZIzLnBFp4RuZPxlSi5f8ImrZ\n"
            + "xVkkUZaJMREB9u6ucP8aFQctCuqnq9DrbcbR+t8Qi9VPVAojU+pc16uqhS2P9/98\n"
            + "OmEz0e5n154YpYsbh/IoMvZ55aOV6SjSbgLaL4bx7NWNllIncakVX+TnR4LrPeAM\n"
            + "Gbb5tJYvB7VFbLJm029Jx4yI8MWnCSbA\n";

    private static final String CA_CERT = "MIIDrzCCApcCCQCVj1/2EkeoqTANBgkqhkiG9w0BAQsFADCBpTELMAkGA1UEBhMC\n"
            + "SU4xEjAQBgNVBAgMCVRhbWlsbmFkdTEQMA4GA1UEBwwHQ2hlbm5haTEZMBcGA1UE\n"
            + "CgwQUm9vdCBDQSBQdnQuIEx0ZDEbMBkGA1UECwwSUm9vdCBDQSBEZXBhcnRtZW50\n"
            + "MRQwEgYDVQQDDAtyb290X2NhX2NydDEiMCAGCSqGSIb3DQEJARYTcm9vdF9jYUBy\n"
            + "b290X2NhLmNvbTAeFw0xNjA2MzAxODA2MDBaFw0yNjA2MjgxODA2MDBaMIGMMQsw\n"
            + "CQYDVQQGEwJJTjESMBAGA1UECAwJVGFtaWxuYWR1MRAwDgYDVQQHDAdDaGVubmFp\n"
            + "MRQwEgYDVQQKDAtDQSBQdnQuIEx0ZDEWMBQGA1UECwwNQ0EgRGVwYXJ0bWVudDEP\n"
            + "MA0GA1UEAwwGY2FfY3J0MRgwFgYJKoZIhvcNAQkBFgljYUBjYS5jb20wggEiMA0G\n"
            + "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCYZkfLmIMN9f1LSHfIXPdUQVltkqHo\n"
            + "+aZbe3vByC+O09UFy2AfzkMwnC606RZM1WTwrdX9/6NzUOoxOI4YRqeBP41MMTIQ\n"
            + "Erf+0ES+nIxalLW2lysGgvawM+l8Yy9lmEkzE4/5sp3hBpjaNfEBdcUUBO03uqiH\n"
            + "w1Jf3vqFGNU4eIRUbw/dUOoQEk3eTATf5fbibg7bjqKSMfufFGbHHBCYOD1G+B7F\n"
            + "lfwM52i0xxFp96zzoC4eD2fWtMSEbwx/8Z8/tm32c8tDp3SYnTuAky5vStfefAQd\n"
            + "M/MAAoPmsjMQwUQeSmm5wB9wujD77D4f3kiQYae74IJ6zw5ciadj6jfRAgMBAAEw\n"
            + "DQYJKoZIhvcNAQELBQADggEBAC0NNVqcTKkOTrGGSCDIHYKL2Ow/svnfTg5CVomt\n"
            + "+Ip1DAk0xhDnuAs549RQFsAQqZaUBiu3+V1neBw47v9UvS5VAgLP58UyMsyS2AEa\n"
            + "xkXha9kZFuULTPWpBcQM/lpjArJ+rxpeQ6AwrG0EpSfoKoRpFcv+RtP39EFysXLn\n"
            + "Ntnc96a5wZPZdW5ENR3GI3vMKvMLda9NcKrVWDtvIjI36vKQO/fpQmqjlusQ8QMC\n"
            + "q3OLxgkdAcyBdjMY7kcmJXHDKM8g6XNUDq3OyLlHcBdsZ/Cr0bKVlxDJLK/wzRjq\n"
            + "LWjLygha9PgeCPi/s9YwuGQTj1SOnY+5hcB6eQ8I/SQQTaU=\n";

    private static final String ROOT_CA_CERT = "MIIEHzCCAwegAwIBAgIJAPqIoyFtil02MA0GCSqGSIb3DQEBCwUAMIGlMQswCQYD\n"
            + "VQQGEwJJTjESMBAGA1UECAwJVGFtaWxuYWR1MRAwDgYDVQQHDAdDaGVubmFpMRkw\n"
            + "FwYDVQQKDBBSb290IENBIFB2dC4gTHRkMRswGQYDVQQLDBJSb290IENBIERlcGFy\n"
            + "dG1lbnQxFDASBgNVBAMMC3Jvb3RfY2FfY3J0MSIwIAYJKoZIhvcNAQkBFhNyb290\n"
            + "X2NhQHJvb3RfY2EuY29tMB4XDTE2MDYzMDE4MDQ1MVoXDTI2MDYyODE4MDQ1MVow\n"
            + "gaUxCzAJBgNVBAYTAklOMRIwEAYDVQQIDAlUYW1pbG5hZHUxEDAOBgNVBAcMB0No\n"
            + "ZW5uYWkxGTAXBgNVBAoMEFJvb3QgQ0EgUHZ0LiBMdGQxGzAZBgNVBAsMElJvb3Qg\n"
            + "Q0EgRGVwYXJ0bWVudDEUMBIGA1UEAwwLcm9vdF9jYV9jcnQxIjAgBgkqhkiG9w0B\n"
            + "CQEWE3Jvb3RfY2FAcm9vdF9jYS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw\n"
            + "ggEKAoIBAQDTkNw5sVNqlfde48StTSeOEq1uVAxMlQAXz3AM/Yr3F1HE2WxkJsQT\n"
            + "AVI3vocV0ZL5U7skPvltmf6xLODWXuCGBH/IMq5aTNHm5C4OP6BXIfmqbLDeIEMH\n"
            + "MUT8VoP4805YcFQExcRMfR58u2Gjr9PWGZ4Y+TbmM5SCCg2LjAvZMDrsBI2bPVdj\n"
            + "LKdo/XuZ8DxrDX7rg/7e6mNG4oyILX/Bt0/Eca6+48PCmtCnJb7oJVBielZUKMKh\n"
            + "brpqMH7T37g9No8hQNjx15zVY+pBnbtprudkLB60tM/8SapFKwl7HfhkQSAqmHe9\n"
            + "RbjsCwJ6zt5A40J7QFbEo+qdq/sFSpefAgMBAAGjUDBOMB0GA1UdDgQWBBRaZJXR\n"
            + "1EbISQmH2IkZYUmq/3SKDzAfBgNVHSMEGDAWgBRaZJXR1EbISQmH2IkZYUmq/3SK\n"
            + "DzAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQA/33dVKNHzzuEtSDoM\n"
            + "kEezSJQumewcf+P9iI+UkT8feHsJW4iJ8syVMoTTMN+C/4OX9uaOo38MNtaGCy1y\n"
            + "oe/KAmMbghC0htKuS+UiGoRDLgp+iJzA4inoca4Zv9nI2/fyp5Gcg2oxYBR87NZi\n"
            + "16o2RQTIXVU2SA+FVFEt8BmiVkJiECF78f95MsbqTRiHFg/siNrLmojAw9K20Fby\n"
            + "SZs7S/tUg4AGbdtj+jo2vDZjy+5u83edgqpXLtEkx9Hm/CzzPyljQoj7yap6E3vg\n"
            + "juMOo5L6L4haKLNgl5qGbk5B4kpb7dYw+PaArODkYKhPIu+0FxNXVkdNwfLbMrhW\n"
            + "mRfL\n";


    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CertificateUtilTest.class, LogAppNames.NETCONF_LIB);

    @Test
    public void testCertificatesAreRead() throws CertificateException {

        List<String> caCerts = new ArrayList<>();
        caCerts.add(CERT_1);
        caCerts.add(CERT_2);
        caCerts.add(CERT_3);

        List<X509Certificate> x509Certificates = CertificateUtil.getX509Certificates(caCerts);
        assertEquals(3, x509Certificates.size());
        X509Certificate certificate1 = x509Certificates.get(0);
        X509Certificate certificate2 = x509Certificates.get(1);
        X509Certificate certificate3 = x509Certificates.get(2);
        assertEquals("CN=ALU, OU=FNBL, O=Alcatel", certificate1.getSubjectDN().getName());
        assertEquals("CN=135.250.17.100, OU=FNBL, O=ALU, L=BLR, ST=KA, C=IN", certificate2.getSubjectDN().getName());
        assertEquals("CN=135.250.17.100, OU=FNBL, O=ALU, L=BLR, ST=KA, C=IN", certificate3.getSubjectDN().getName());
    }

    @Test
    public void testInvalidCertificatesAreNotRead() throws IOException {

        List<String> caCerts = new ArrayList<>();
        caCerts.add(INVALID_CERT_1);
        caCerts.add(INVALID_CERT_2);
        try {
            CertificateUtil.getX509Certificates(caCerts);
            fail("invalid certificates were parsed !");
        } catch (CertificateException e) {
            // we are ok, but lets log the exceptions
            LOGGER.error("Expected exception", e);
        }
    }

    @Test
    public void testGetPeerX509Certifcate() throws SSLPeerUnverifiedException, CertificateException, IOException {
        //prepare sslSession with peer certificate chain
        SSLSession sSLSession = mock(SSLSession.class);
        List<String> certificateChain = Arrays.asList(PEER_CERT, CA_CERT, ROOT_CA_CERT);
        List<X509Certificate> peerCertificateChain = CertificateUtil.getX509Certificates(certificateChain);

        //assert certificate chain is loaded in order as peer certificate first to root ca last
        assertEquals("EMAILADDRESS=peer@peer.com, CN=www.peer.com, OU=Peer Department, O=Peer Pvt. Ltd, L=Chennai, ST=Tamilnadu, C=IN",
                peerCertificateChain.get(0).getSubjectDN().getName());
        assertEquals("EMAILADDRESS=ca@ca.com, CN=ca_crt, OU=CA Department, O=CA Pvt. Ltd, L=Chennai, ST=Tamilnadu, C=IN",
                peerCertificateChain.get(1).getSubjectDN().getName());
        assertEquals("EMAILADDRESS=root_ca@root_ca.com, CN=root_ca_crt, OU=Root CA Department, O=Root CA Pvt. Ltd, L=Chennai, ST=Tamilnadu, C=IN",
                peerCertificateChain.get(2).getSubjectDN().getName());

        when(sSLSession.getPeerCertificates()).thenReturn(peerCertificateChain.toArray(new Certificate[0]));

        //test getPeerCertificate
        X509Certificate peerCertificate = CertificateUtil.getPeerX509Certifcate(sSLSession);

        //verify peer certificate retrieved
        assertEquals("EMAILADDRESS=peer@peer.com, CN=www.peer.com, OU=Peer Department, O=Peer Pvt. Ltd, L=Chennai, ST=Tamilnadu, C=IN", peerCertificate.getSubjectDN().getName());

    }

    @Test
    public void testGetX509Certificates() throws CertificateException, IOException {
        File trustChainFile = new File(getClass().getResource("/trustchain.crt").getPath());
        List<X509Certificate> x509Certificates = CertificateUtil.getX509Certificates(new FileInputStream(trustChainFile));
        assertEquals(3, x509Certificates.size());
        X509Certificate certificate1 = x509Certificates.get(0);
        X509Certificate certificate2 = x509Certificates.get(1);
        X509Certificate certificate3 = x509Certificates.get(2);

        assertEquals("CN=ALU, OU=FNBL, O=Alcatel", certificate1.getSubjectDN().getName());
        assertEquals("CN=135.250.17.100, OU=FNBL, O=ALU, L=BLR, ST=KA, C=IN", certificate2.getSubjectDN().getName());
        assertEquals("CN=135.250.17.100, OU=FNBL, O=ALU, L=BLR, ST=KA, C=IN", certificate3.getSubjectDN().getName());
    }

    @Test
    public void testGetx509CertificateFromDelimitedString() throws CertificateException {
        X509Certificate x509Certificate = CertificateUtil.getX509CertificateFromDelimitedString(DELIMITED_CERT_1);
        assertEquals("CN=ALU, OU=FNBL, O=Alcatel", x509Certificate.getSubjectDN().getName());
    }

    @Test
    public void testGetx509CertificatesFromDelimitedStrings() throws CertificateException {
        List<X509Certificate> x509Certificates = CertificateUtil.getX509CertificatesFromDelimitedStrings(Arrays.asList(DELIMITED_CERT_1, DELIMITED_CERT_2));
        assertEquals("CN=ALU, OU=FNBL, O=Alcatel", x509Certificates.get(0).getSubjectDN().getName());
        assertEquals("CN=135.250.17.100, OU=FNBL, O=ALU, L=BLR, ST=KA, C=IN", x509Certificates.get(1).getSubjectDN().getName());
    }

    @Test
    public void testGetx509CertificatesForInvalidCertString() throws IOException {
        File trustChainFile = new File(getClass().getResource("/keyMgrTest/multipleCertificates.crt").getPath());
        try {
            CertificateUtil.getX509Certificates(new FileInputStream(trustChainFile));
            fail("Certificate exception should have been thrown");
        } catch (CertificateException ex) {
            assertEquals("Unable to initialize, java.io.IOException: extra data given to DerValue constructor", ex.getMessage());
        }
    }

    @Test
    public void testGetx509CertificateFromEmptyString() {
        try {
            CertificateUtil.getX509CertificateFromDelimitedString("");
            fail("Certificate exception should have been thrown");
        } catch (CertificateException ex) {
            assertEquals("Could not parse certificate: java.io.IOException: Empty input",ex.getMessage());
        }
    }

    @Test
    public void testGetx509CertificateFromEmptyStream() throws CertificateException {
        List<X509Certificate> certificates = CertificateUtil.getX509Certificates(new ByteArrayInputStream(new String().getBytes()));
        assertTrue(certificates.isEmpty());
    }

}
