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

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class DynamicX509TrustManagerImplTest {
    private static final Logger LOGGER = org.apache.log4j.Logger.getLogger(DynamicX509TrustManagerImplTest.class);
    // A CA whose Subject is as follows "CN=127.0.0.1, OU=FNBL, O=ALU, L=BLR, ST=KA, C=IN"
    private static final String CA1_CERT = "MIIDhTCCAm2gAwIBAgIJAOR28efktQquMA0GCSqGSIb3DQEBBQUAMFkxCzAJBgNV\n"
            + "BAYTAklOMQswCQYDVQQIDAJLQTEMMAoGA1UEBwwDQkxSMQwwCgYDVQQKDANBTFUx\n"
            + "DTALBgNVBAsMBEZOQkwxEjAQBgNVBAMMCTEyNy4wLjAuMTAeFw0xNTA0MjgxMDAy\n"
            + "MDRaFw0xODAyMTUxMDAyMDRaMFkxCzAJBgNVBAYTAklOMQswCQYDVQQIDAJLQTEM\n"
            + "MAoGA1UEBwwDQkxSMQwwCgYDVQQKDANBTFUxDTALBgNVBAsMBEZOQkwxEjAQBgNV\n"
            + "BAMMCTEyNy4wLjAuMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMVC\n"
            + "sjTIhvW/TLGrdT4aQ65mUW2+SzY+GOxaK0xrmmzmdcVyrY4baCuKVE+9YbiiopFI\n"
            + "WArz45Um5QyGY+282qxW7SmJyPIHKawVMlDE3QC7DfY0XWMd4iCWgM4LT+leMT6B\n"
            + "0iAiQt+1r1QVUKEgZ7Z/lL/ZYCh2aAO8uHTz6qP+Vj4HCuv8DOk3G6HJb4dyfiJ5\n"
            + "RLdKgAFjwtUPygYZ6NPgQP5H3VgJRbfTcyuKLh0X4fkB4PlFNjkgm8FqhFxq4/c/\n"
            + "3Rn1FLMKRSFscpRkIFEV1buOUkhqqdOdP/8MEkt5vmXTGe2+6hCy3ZmZnFJRDymg\n"
            + "DFO70GV0h6hOtbeUZXUCAwEAAaNQME4wHQYDVR0OBBYEFF+oSSOgY18wM8duQKJO\n"
            + "op7qN/NyMB8GA1UdIwQYMBaAFF+oSSOgY18wM8duQKJOop7qN/NyMAwGA1UdEwQF\n"
            + "MAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAGQhtE+aOrPfVDYp3JYYGxgeFOql5FWY\n"
            + "pleAbB6D+P6pP6bVKMDluZSFG+0hnKOEGybNkEQrUcVvFky/RBFWEF+WADfpWWbw\n"
            + "E1gRW47aDP9DCCCSOJH6GsHH1GQCxGrn5KUPiSQs6FpbXIyUN3Umn5ZAAzXV9yYm\n"
            + "JfnAcyoRiCfZkJKOlFAc2Yx64NaH3VqlA0aiZJas6Dbwj9c7/wgHIKeFv98TTaDz\n"
            + "U0/kZb2wkpB/Srixrp1iYTTzUS85Igtgrhn9JwY5YGNSTXnZE7wnFlj1U0OneLe+\n"
            + "Nvd/HlGkn2xCTuYCCGdhB9valEY/aqG70rUmjSPC3B4Sy4b6qJXZnTk=\n";
    // A CA whose Subject is as follows "CN=CA2, OU=CA2, O=CA2, L=BLR, ST=KA, C=IN"
    private static final String CA2_CERT = "MIIDdzCCAl+gAwIBAgIJAPNlJ52QRxDLMA0GCSqGSIb3DQEBBQUAMFIxCzAJBgNV\n"
            + "BAYTAklOMQswCQYDVQQIDAJLQTEMMAoGA1UEBwwDQkxSMQwwCgYDVQQKDANDQTIx\n"
            + "DDAKBgNVBAsMA0NBMjEMMAoGA1UEAwwDQ0EyMB4XDTE1MDQyOTA2MDc0M1oXDTE4\n"
            + "MDIxNjA2MDc0M1owUjELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAktBMQwwCgYDVQQH\n"
            + "DANCTFIxDDAKBgNVBAoMA0NBMjEMMAoGA1UECwwDQ0EyMQwwCgYDVQQDDANDQTIw\n"
            + "ggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC8lukI9R4cdex3T7lBCi4D\n"
            + "3WBghsHZbdV6ikzonqFU1lsaBttA88+LHJWczD6iIFzHWaQrlj3cJDnwTd1e7ZTZ\n"
            + "GiytTmYjq3balPOrTLkR6rZcLLrIAU9ij2rztM9OLBDEXJMBtR3Fh1ZSRsnOBpFw\n"
            + "bknJhv0Ip+HAs4hP7SHU30IJ/6xUgn/0dOrReWdFvzUo+RSIhhu5NlO9zpXacAGd\n"
            + "DFswhqLYA25i6MlYrwYar400FGh2p55JKVSCVNK2A3PZuLy3M7NJj3h/4jRrlUYW\n"
            + "EriwiTvCSpL6orc/IjXzRJ1G/ol+p5X10k/Hjqce4MEQT3XELFfH0zYlPt0Bglw9\n"
            + "AgMBAAGjUDBOMB0GA1UdDgQWBBSPWqRmqzsdfYHqXjeJjKT6UpdyyDAfBgNVHSME\n"
            + "GDAWgBSPWqRmqzsdfYHqXjeJjKT6UpdyyDAMBgNVHRMEBTADAQH/MA0GCSqGSIb3\n"
            + "DQEBBQUAA4IBAQBLraWYfKfKTLgr3F7oY6oUwW8dtBe8zFN0AhBUShwQyJlPIPKK\n"
            + "Hc/vUGXCpewjHnwBnYaeCD0Z2T48RuoIoAzFXVvuLuTbCSZ5jLEqbuMQ2/+nNKxK\n"
            + "P871gY70dM58EOASVjHDLxaV0pXTMfuBns/1AqcbVo7enu1Yi3yph7GFa9UKRvx/\n"
            + "bS8PLo3LfHPrbVFDtpagwWCTqdgsPWFl3gJEGGdgi09KZXuFvofZwTVX3hjzfWDL\n"
            + "n03mx9Sc0/sxCZRkor21A1S4TVTC8wWZ6xW9zeUaXThzBMJOWYSM6MJO2WQcsURy\n" + "12KuCNeU3QQMVGU8gjH7/1NaPEBh4+SrLHx9\n";

    private static final String CA1_SIGNED_CERT1 = "MIIDSDCCAjACCQCjaKR7Ng7jbDANBgkqhkiG9w0BAQUFADBZMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEMMAoGA1UECgwDQUxVMQ0wCwYD\n"
            + "VQQLDARGTkJMMRIwEAYDVQQDDAkxMjcuMC4wLjEwIBcNMTUwNDI5MDYwMDQ2WhgP\n"
            + "MzM4NDA0MTEwNjAwNDZaMHExCzAJBgNVBAYTAklOMQswCQYDVQQIDAJLQTEOMAwG\n"
            + "A1UEBwwFS1VETEExFjAUBgNVBAoMDUdhbmVzaCBCZWVkaXMxEjAQBgNVBAsMCXBh\n"
            + "Y2thZ2luZzEZMBcGA1UEAwwQZ2FuZXNoYmVlZGlzLmNvbTCCASIwDQYJKoZIhvcN\n"
            + "AQEBBQADggEPADCCAQoCggEBANKcy1+1OdPfDMbZWvyvJKCaVWVPTefiOcQ5u5pT\n"
            + "jCc5YmJUVK3CXxeFLkSjzGtjJEWdHhj6Ix5Dt7zVGM+/N6D300kqIOWAFW1p/9jH\n"
            + "Dj4Dh4Zqr1A9kAtV1c2TWYdhfO6yff7sFafZCe5NeHKe4LaJJJSTU35JdtJB1adJ\n"
            + "zYDA8ol5Y8zYyN10+dpqpcWH6hGB810LX+W+zFbLZvtlBPNr/kGqQG1z1fhAzlWQ\n"
            + "2GPWx5v6VJUnOrxdQYvJyX0V4VXIGWCBpH37G59tstRlTxvFRzUA12PjC+AYguUL\n"
            + "1ha34falt6RsZP0O31uCtF8i+fUp0RUNkY7IHwiZvQw6hJUCAwEAATANBgkqhkiG\n"
            + "9w0BAQUFAAOCAQEAvFFZHgJFA3pVXgMdijswAODyGVz1N5pkGnSziqGHHZ3TfR7x\n"
            + "80dEQdkNyyphDGAIe0JUW4TepDZTK5k93w2GSrWzDaoQPIzBPZR/UsoiMYrEHNji\n"
            + "k9HKKiLWKLAUNK5UgnoV/eqiKnWY869UGtkBaNgUOu7Y3MrUYhmv6u3aWtkPOVve\n"
            + "KgnIgq74hq8jP0cfoz+sUg3DE5I5JRPGGRWPTt5yLP8jEMoDmtuRmzTdxfa3JGtJ\n"
            + "ae4lrN6OZIue7gCj/rsuA6S2DZKqNVVCKcYPP8IXbQIr3hKLi+jb85grAKfRytck\n" + "OeSbLcxoFVlWa07EcvFrs2PgVVk29cjwrbrIqQ==\n";
    private static final String CA1_SIGNED_CERT2 = "MIIDQTCCAikCCQCRCpujd22pwTANBgkqhkiG9w0BAQUFADBZMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEMMAoGA1UECgwDQUxVMQ0wCwYD\n"
            + "VQQLDARGTkJMMRIwEAYDVQQDDAkxMjcuMC4wLjEwIBcNMTUwNDI5MDYwMjI5WhgP\n"
            + "MzM4NDA0MTEwNjAyMjlaMGoxCzAJBgNVBAYTAklOMQswCQYDVQQIDAJLQTEPMA0G\n"
            + "A1UEBwwGUFVUVFVSMRQwEgYDVQQKDAtHdWRyYSBGYXJtczEOMAwGA1UECwwFRmFy\n"
            + "bXMxFzAVBgNVBAMMDmd1ZHJhZmFybXMuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOC\n"
            + "AQ8AMIIBCgKCAQEA45IkA7BQlCh5mYHxh/YLi2+uQsk0ps9kh/DQ0pSGc4+C4gZf\n"
            + "PQkgutGaY5B9Dx2/0uuta2ABaY19kwRIQF0RU0nCCaSADL3wjnawa9LeEnMpEEh8\n"
            + "Fsle6DZD6W97VIBzVRmaiOg2TqUlCgdhsoerPAztF/oIFZbt8zjxiADaHyra4rNh\n"
            + "dNvmN8T8EStCY07PXih63ZU5lYdOvLnc4IeDc5cdUytINvVQhUAQ7YsZHvObJg8j\n"
            + "nZ+ruoHJLmDX0bm/SUzrzpP7AcOX7Uec1yHuPijFM3EU8lxMwVTEYfNfN5t7l2kE\n"
            + "FRz9BDVKh0f+2Rs6qUCPpp+Jgu+b66HuSXYCIQIDAQABMA0GCSqGSIb3DQEBBQUA\n"
            + "A4IBAQBDYPVKTVxSg1uli3zm1gJpuVU6cxRE9+MdLa4CcNKfcS9l1gY/UjtUmmeY\n"
            + "+ThH38+opaitsjo0G8cjjBuKKzHsYtLa7Rls3rvC1JfR44r/vfcG+TVQi2kjXi+/\n"
            + "YbGpzxBJImGWWnt2bDp05yiSyK9FmRClTgXlndJARPQwbC0It9Ema1wDoOiPW4ox\n"
            + "ib9nA16xviiTZS06iuzqL64yL8BxUVjepHj2Atoo78qgFQ/5EhV3yTjKas3+hk85\n"
            + "Y8Yr/oHuFFCxufLP6iWhxmRKf7u0hA8pejNtcWZgPmdgBgy9LEvfib3qQtcHxt5G\n" + "NUSwmRw3CPgFksw4vmSt0AYyDfYH\n";
    private static final String CA2_SIGNED_CERT1 = "MIIDQTCCAikCCQCjaKR7Ng7jbTANBgkqhkiG9w0BAQUFADBSMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEMMAoGA1UECgwDQ0EyMQwwCgYD\n"
            + "VQQLDANDQTIxDDAKBgNVBAMMA0NBMjAgFw0xNTA0MjkwNjA4MTZaGA8zMzg0MDQx\n"
            + "MTA2MDgxNlowcTELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAktBMQ4wDAYDVQQHDAVL\n"
            + "VURMQTEWMBQGA1UECgwNR2FuZXNoIEJlZWRpczESMBAGA1UECwwJcGFja2FnaW5n\n"
            + "MRkwFwYDVQQDDBBnYW5lc2hiZWVkaXMuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOC\n"
            + "AQ8AMIIBCgKCAQEA0pzLX7U5098Mxtla/K8koJpVZU9N5+I5xDm7mlOMJzliYlRU\n"
            + "rcJfF4UuRKPMa2MkRZ0eGPojHkO3vNUYz783oPfTSSog5YAVbWn/2McOPgOHhmqv\n"
            + "UD2QC1XVzZNZh2F87rJ9/uwVp9kJ7k14cp7gtokklJNTfkl20kHVp0nNgMDyiXlj\n"
            + "zNjI3XT52mqlxYfqEYHzXQtf5b7MVstm+2UE82v+QapAbXPV+EDOVZDYY9bHm/pU\n"
            + "lSc6vF1Bi8nJfRXhVcgZYIGkffsbn22y1GVPG8VHNQDXY+ML4BiC5QvWFrfh9qW3\n"
            + "pGxk/Q7fW4K0XyL59SnRFQ2RjsgfCJm9DDqElQIDAQABMA0GCSqGSIb3DQEBBQUA\n"
            + "A4IBAQB6Jd4QdbjWjniAH51eOkiLm3y/DOxqm3GXAUtbWQXSfwiXBar3EAazFUTN\n"
            + "kNj9bE3fRYan6Y+4+SuugILs6eUMxkQLJM+xktMm4xtiqEnj+cjdjMvia6ubBliA\n"
            + "v1oYKxlcnI85roOhzoYtQslKdjmvrVQe9ycTIsKviq3/252B+7jEyrQvYW+Dvo6/\n"
            + "76bj50RTiakjhsfKh4OytfVcJST+8jW41zXQMTFwyN5h5t0spBLf3ijdwFQg+BBq\n"
            + "MA495i31fretzwfux9X5LCNpYArVF9VOZZGa+TRAzEkJ5O3zN4+ElrJT7EhO2CUQ\n" + "of6J/49cEP/aChpmuhugBv91Nitw\n";
    private static final String CA2_SIGNED_CERT2 = "MIIDOjCCAiICCQCRCpujd22pwjANBgkqhkiG9w0BAQUFADBSMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEMMAoGA1UECgwDQ0EyMQwwCgYD\n"
            + "VQQLDANDQTIxDDAKBgNVBAMMA0NBMjAgFw0xNTA0MjkwNjA4NTJaGA8zMzg0MDQx\n"
            + "MTA2MDg1MlowajELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAktBMQ8wDQYDVQQHDAZQ\n"
            + "VVRUVVIxFDASBgNVBAoMC0d1ZHJhIEZhcm1zMQ4wDAYDVQQLDAVGYXJtczEXMBUG\n"
            + "A1UEAwwOZ3VkcmFmYXJtcy5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK\n"
            + "AoIBAQDjkiQDsFCUKHmZgfGH9guLb65CyTSmz2SH8NDSlIZzj4LiBl89CSC60Zpj\n"
            + "kH0PHb/S661rYAFpjX2TBEhAXRFTScIJpIAMvfCOdrBr0t4ScykQSHwWyV7oNkPp\n"
            + "b3tUgHNVGZqI6DZOpSUKB2Gyh6s8DO0X+ggVlu3zOPGIANofKtris2F02+Y3xPwR\n"
            + "K0JjTs9eKHrdlTmVh068udzgh4Nzlx1TK0g29VCFQBDtixke85smDyOdn6u6gcku\n"
            + "YNfRub9JTOvOk/sBw5ftR5zXIe4+KMUzcRTyXEzBVMRh8183m3uXaQQVHP0ENUqH\n"
            + "R/7ZGzqpQI+mn4mC75vroe5JdgIhAgMBAAEwDQYJKoZIhvcNAQEFBQADggEBAIfP\n"
            + "8PjCxrSych2wqWiNtemReJcKbUX3mhArvd1UdsxED03m0QSGE3hwnKaVrzlUPun0\n"
            + "E+PrWUnc9d2SiJPqeko0ahYUB+C67bC+jsrCzJCcgO4AgXXBR8jbcbPidlWyAyQm\n"
            + "Yrc/n3wu3r3U3N17lo5C/teuZ03o1LW4H74OHJtVPP3knNbhEYqAYxLVFJN6svuH\n"
            + "zqpszsJJL8FqVsTEf/okWBDuUj74+eHip0Wp//1zyoi5YNFWGP/y7qrHw3/8jpgU\n"
            + "Lw9k/bSiILM4tZaEyFJL3Kh6FJGJiJCzIpnf7twYlMteZAFMlK/P5j99pfOBwBT+\n" + "QZlR1eIQowyhITnX5T4=\n";

    // A CA whose Subject is as follows "EMAILADDRESS=CA3@ca.org, CN=CA3, OU=CA3, O=ALU, L=CA3, ST=KA, C=IN
    private static final String CA3_CERT = "MIIDrTCCApWgAwIBAgIJALygjMBH0D4TMA0GCSqGSIb3DQEBBQUAMG0xCzAJBgNV\n"
            + "BAYTAklOMQswCQYDVQQIDAJLQTEMMAoGA1UEBwwDQ0EzMQwwCgYDVQQKDANBTFUx\n"
            + "DDAKBgNVBAsMA0NBMzEMMAoGA1UEAwwDQ0EzMRkwFwYJKoZIhvcNAQkBFgpDQTNA\n"
            + "Y2Eub3JnMB4XDTE1MDQyOTA4MzkyOVoXDTE4MDIxNjA4MzkyOVowbTELMAkGA1UE\n"
            + "BhMCSU4xCzAJBgNVBAgMAktBMQwwCgYDVQQHDANDQTMxDDAKBgNVBAoMA0FMVTEM\n"
            + "MAoGA1UECwwDQ0EzMQwwCgYDVQQDDANDQTMxGTAXBgkqhkiG9w0BCQEWCkNBM0Bj\n"
            + "YS5vcmcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC23qOMW+3To4h1\n"
            + "4LvbD9oPu0iltVU4Ti/CHF4lxWu/JyDdUAQQUwBBg64ILoRcV3apmHL64seTBUaN\n"
            + "+ZtwA+Ll0SXn641QDELsnalKIu3Q0142WMFmfxD8N4cXf7PGixEBdfeJBSPOelh2\n"
            + "xNS7vyCg0sr5DR2lzjQgs3j+26M4igVYZtDZPCBqPFWDzaFdOLkczZ48KCxvMyRB\n"
            + "R3w9SCCTqcbpcZPLbhyq/8rFzIhMhYDdyLWMP+Q90I+jdJTD0XBqkIaqD7CLzHWP\n"
            + "lsm/4htLTERdRFNbGTwyWrjNiaxz3887zJwOmnVV4sjYRafT2OzRtLAlhp9NljDq\n"
            + "+um1RyJxAgMBAAGjUDBOMB0GA1UdDgQWBBRsD/HKiQgtrOixGKemPywF2zD8SjAf\n"
            + "BgNVHSMEGDAWgBRsD/HKiQgtrOixGKemPywF2zD8SjAMBgNVHRMEBTADAQH/MA0G\n"
            + "CSqGSIb3DQEBBQUAA4IBAQAfdIWu0FzMfuk3nVTMMZ/FQm52QVphXUYJAiVNB5Q3\n"
            + "L7As3nK8axT+brTTLsr2voIZnGpCIVdb9ak/sORsjyObY1rGJJ7ygXXbT+6giTbx\n"
            + "Cm7wzZBaeU3Yxoz9ictfuOYHdC00gOk4LxiiH3lPqV8MdcPyIK3hHGLxU/y6X8IM\n"
            + "uXENR2p4b6xW7d1TO5Iw29jjhhkUL71/ptzTmESFIuNB78DOs4cFBykVzB3Pdnbw\n"
            + "D6D6NPqoqWO/PtyK7gJY/y6pN9ymUJRmlf2rlOlGg4vhFZxBblePHkGtfHNZfrKp\n" + "9Nexb2+WZQWL8q1e7+CiMP3yWNEXLCdhd6YQMWZur8Tn\n";
    private static final String CA3_SIGNED_CERT2 = "MIIDXDCCAkQCCQCjaKR7Ng7jbjANBgkqhkiG9w0BAQUFADBtMQswCQYDVQQGEwJJ\n"
            + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0NBMzEMMAoGA1UECgwDQUxVMQwwCgYD\n"
            + "VQQLDANDQTMxDDAKBgNVBAMMA0NBMzEZMBcGCSqGSIb3DQEJARYKQ0EzQGNhLm9y\n"
            + "ZzAgFw0xNTA0MjkwODM5NTdaGA8zMzg0MDQxMTA4Mzk1N1owcTELMAkGA1UEBhMC\n"
            + "SU4xCzAJBgNVBAgMAktBMQ4wDAYDVQQHDAVLVURMQTEWMBQGA1UECgwNR2FuZXNo\n"
            + "IEJlZWRpczESMBAGA1UECwwJcGFja2FnaW5nMRkwFwYDVQQDDBBnYW5lc2hiZWVk\n"
            + "aXMuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0pzLX7U5098M\n"
            + "xtla/K8koJpVZU9N5+I5xDm7mlOMJzliYlRUrcJfF4UuRKPMa2MkRZ0eGPojHkO3\n"
            + "vNUYz783oPfTSSog5YAVbWn/2McOPgOHhmqvUD2QC1XVzZNZh2F87rJ9/uwVp9kJ\n"
            + "7k14cp7gtokklJNTfkl20kHVp0nNgMDyiXljzNjI3XT52mqlxYfqEYHzXQtf5b7M\n"
            + "Vstm+2UE82v+QapAbXPV+EDOVZDYY9bHm/pUlSc6vF1Bi8nJfRXhVcgZYIGkffsb\n"
            + "n22y1GVPG8VHNQDXY+ML4BiC5QvWFrfh9qW3pGxk/Q7fW4K0XyL59SnRFQ2Rjsgf\n"
            + "CJm9DDqElQIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQCGiJfq784hOFSknyJ1KUgG\n"
            + "A6Xu9hGhwY8JyhX6jth1qplyMKDHlMxGNHCrxW5YaqJfJuLp1IaDaFxto4BjS9N8\n"
            + "XRwgeIFfdmxaKodiF4OQN4D4/3yGs8Jc104x4H9O2xDwSohEPPKClFnCh9692fZf\n"
            + "6LdDs/M5jfs8VjGg5erZXjUT3628LOsxxN72YaOijNzEeidQqHAkX6cV1kNgmuCW\n"
            + "oB3CtuLma6TLKpnJIpv0A0PuzgvtEbvBFLO/3ApFpCM3YW83qCzW/k19wsIzxEKv\n"
            + "OlqWfNKYqOvmH6ND2gpldircTdtOOylgJMw1qyQ2TgK/mHRKam1xUyoButfSxkuU\n";
    private static final String LEVEL2_CA1_SIGNED_CERT1 = "";

    private static final String CA_CERTIFICATE_WITH_EXTENSION = "MIIFvDCCA6SgAwIBAgICB/IwDQYJKoZIhvcNAQELBQAwPTEOMAwGA1UECgwFTm9r\n" +
            "aWExKzApBgNVBAMMIlFBIEZhY3RvcnkgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkw\n" +
            "IhgPMjAxNDA1MTQwOTE0MDBaGA8yMDY0MDUxMzA5MTQwMFowPTEOMAwGA1UECgwF\n" +
            "Tm9raWExKzApBgNVBAMMIlFBIEZhY3RvcnkgQ2VydGlmaWNhdGlvbiBBdXRob3Jp\n" +
            "dHkwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQC8Y4kFpjdL1Zu5Pf4b\n" +
            "4sB+h3mJ/oCOSoIxhFIzQpOW+CQgri8pIV1rx7N/hu6cf4xYPk+l1v1f69/E2Tcz\n" +
            "etaXP/7dhyMaKhthjR0CZXAf33ub6inN2fMYscSNER43yjTV1/TFJEEqcyb0C2KN\n" +
            "dH/LvB6vZ5K0jjTgvgsIryd3t+qgrBcscrYmWqdQUho8Zx2PmJuzdR70/vdnJuHf\n" +
            "SpLCyZiRaZzQRrZG2DpRouCcrSC1aWFn83inenvwanRQNtC6lBRTa9O86VKGKn6J\n" +
            "NdQZkvqPhQRvna+vW5raefNH26E/4VN8VkF0fX9MSOCO1HvpcS02RPcr0ehTToQw\n" +
            "baIlIGRzc4JuOvbaL5b+DULLskBiRFxBR4b4D6soYzG1w//aavbSMv2cmmbfrN5V\n" +
            "bBfiW+FFblKglJqyHDPWhXNeqN9ViFXMOSFL/b3XluSZN0Jfsa/S70rzHqkG4iI1\n" +
            "GUAyeHWaStLZisZbvCB2l0ST3+zkGYmiFKNziFNFnYcfpmNbiM139e+xZPzh5K/4\n" +
            "I6pGMoenGVQpMyxP1RPBxYGv+5jylNmFkx+lLOMUZKW5rYyNcSCD37UkvdRvT/Yx\n" +
            "+372kJ+unpWPfnBUUjKNTz61jse8B1a76LXaJLR1zzEPU+ef1ZEGMNLoxOqNPtDn\n" +
            "uc/o7N5K+bt6386SPKQeqDB/kQIDAQABo4HBMIG+MGYGA1UdIwRfMF2AFPH0ja5y\n" +
            "lBuUOidmsRbmKB99nBOpoUGkPzA9MQ4wDAYDVQQKDAVOb2tpYTErMCkGA1UEAwwi\n" +
            "UUEgRmFjdG9yeSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eYICB/IwHQYDVR0OBBYE\n" +
            "FPH0ja5ylBuUOidmsRbmKB99nBOpMA4GA1UdDwEB/wQEAwIBxjARBgNVHSAECjAI\n" +
            "MAYGBFUdIAAwEgYDVR0TAQH/BAgwBgEB/wIBBDANBgkqhkiG9w0BAQsFAAOCAgEA\n" +
            "iZ4SEA3PdykUiYO15JwWAjJPW5suUkcJ5j+VS7Owe5OQ36kKv0ot4X5XpE71LVrV\n" +
            "/y+76IExpuDXNTnHKidZYKY0ttEBQeQm2zIjSRUIjQ4OYwvXw8Z9FzSUzwT23xf/\n" +
            "UycmM2ABrxYXekVcq4SRxlpnkjWqtCqOi0DdrmhvHxKUrnP4Jk3bUcGVKPVixJyR\n" +
            "8rPHV6ZiUCC7O9j9yrrMolzBinemZXtBAHdBKj42tWZnVo0nSSVad4j36dADdDa1\n" +
            "zTrr5ZSOEtqVrmw0Pg/Qno+LnhzqIT+sCg/9jWr2XGOSoNOmLjEMw+DAJJ9Fv3FX\n" +
            "OQYj4Gu/dJ+ca5CQGzh6Mc3IEeJ72Qc62U4ran9gzAKUpv/JsRe0hsbziJ9lTIr3\n" +
            "5sx7Mh6powTRO5ith0Ql8y+DyHOTiib2ckRyNxl6YJmoMMq3cypEu6126ap9RqGK\n" +
            "8AqrvERdS5/pfXVmP9d8E2dIjfh2fqS8jPTA189iQHOUe4aPLwG/3wnDv4nKyTdK\n" +
            "mYhm5udJMJWTCbFuxKCwiJHL/yUYNlxFucK0p7gtMxxWV1NjNS3KAV+SiHoiKy9P\n" +
            "i2y1fcEPfttO0+dSveak1TaPJWrMCIDfj9DW/XkDwOmNzEfCBLTYWfiR1akuahyt\n" +
            "8aacB5o8weLCDOs18ryM0CwMpixgWiF/6FKIl+Uerqk=\n";

    private static final String CERT_SIGNED_WITH_EXTENSION = "MIIFeTCCA2GgAwIBAgIDAjphMA0GCSqGSIb3DQEBCwUAMD0xDjAMBgNVBAoMBU5v\n" +
            "a2lhMSswKQYDVQQDDCJRQSBGYWN0b3J5IENlcnRpZmljYXRpb24gQXV0aG9yaXR5\n" +
            "MB4XDTE4MDIwMTEwMjQ0MVoXDTM4MDEyNzEwNTQ0MVowTTEOMAwGA1UECgwFTm9r\n" +
            "aWExDDAKBgNVBAsMA0JCQTEYMBYGA1UEAwwPMzcyOS0zNjU3NDMzMTAwMRMwEQYD\n" +
            "VQQFEwozNjU3NDMzMTAwMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA\n" +
            "t5WMIR4aVZ9Kpcq3QJmmcC2FjfTi1KXnZvpnJg52dkoqdB4bk5wO6xvcGBmGOcFq\n" +
            "Jlq4KGG+TDa5XARFlSuqG58PLvJUlezIX72pI0sHaiOqUhwECbcjlcIklDsA5Ts5\n" +
            "ZZLvt2a56b53SwIQ0yiX21U2VnXM9mr55K/ZQoRSkbam9U9Om8AcNPN5K2Py5DWd\n" +
            "chN+yS4NkEXRQ4T7x74tKDFdz+jh56j7hEk3vyHFA1PJnMH9MsIdNlrlNrnKG3gS\n" +
            "74b10B0Vf5IKRaIi1Oko/wK17wPW9PvAh28GA/EWEjVO97LCwpC70GiK2sxgNQh5\n" +
            "/zNszmBT+c0J5VaVHFs4zDwSSJMl3uYESN92Q7LK72gsM05IYkZNp2t/5pSqcdNo\n" +
            "TT0TxBTAnjXEmkrQvw/D8BtPDOM1IOCVOercL6+BQdPxM3nTyZ4stKQ6we0SzCB1\n" +
            "iQf+KWlTI7cg/sqEC8ZW9zMVMsOeHmG4/ljkP+3aAlRmNbNJamEvnLIPyqZmTC7S\n" +
            "LyFabCmuSMZucp2M8AbJWAdU5vCHAKUDiM0ZM2nJiIDz9VOKiyWgGbDqXssOnv9M\n" +
            "c/7FDwt4wk3vx9VoSA591ur53O8iMBDY/YK5VhVJQY388/+GPvHcW+VxlXC9MoxG\n" +
            "SMp+GDWRIeUiset7SojqZNiypzN2AyEKypcYfd40hOkCAwEAAaNyMHAwHwYDVR0j\n" +
            "BBgwFoAU8fSNrnKUG5Q6J2axFuYoH32cE6kwHQYDVR0OBBYEFPJ+A27Zgfu8YhhT\n" +
            "OhPg7a6QK5kGMA4GA1UdDwEB/wQEAwIFoDAeBgNVHSABAf8EFDASMBAGDisGAQQB\n" +
            "gd4qAQ8BAgcBMA0GCSqGSIb3DQEBCwUAA4ICAQCZaZ+jDEG9Tei7aTA3r3bLAnQI\n" +
            "rZAIZRJZlzJ+PZLaDS7cCMjRXhlZy/haLjGDYZjAAcSUQk102xqhAoEq0Vfnv+BW\n" +
            "n712+XlrsYwLmzFGS+/hEApF1LMs9OF9YWz0XB7EVt4ladj0rtTHbxEzndBj5sBT\n" +
            "zPS4Li5JwPrhZZ1nLTWOBymvlcKQIveGReh3zpPsn3zAFO/3ZPERd/L//7zAFSRA\n" +
            "A4EGDIouD/9NI0f60jDecrHYw+7vgTm5d2KNtvdDigWTYXEiLRq8R1iGO0hhqaWB\n" +
            "cJxU0iNyyMR/e44vIuDDJC42JC4idQKKVsfnsTkhfKcudbr/aBE0xafDLPoghKlz\n" +
            "+JFnx1OSIjvXC8FRbgFOXrAcFR5Li4LQEhWotA50KF9JpakZKNYrhflL+fsQ5hZs\n" +
            "i+lO3Xa0v2weq/WhGK2qs2bqcoGFXVJp1uYnWUpQ6MNOt5xtEb/smFfRRYH+2Jc0\n" +
            "KGLBOjlahrpDXOI3pLVP68Yve0jICXxypfBBwD8+OSPZKXjFY1c9wkHDZb1goedN\n" +
            "CzmpI8L5N1z13+tTnUQ0eTUydhgHJU7WH9SzaI5W0Xyd6epL11mUcH2+Kcf4LnXD\n" +
            "IOANzB5aO0jLXnuieBkha6jh7QHF9+xo7/WwF269ZUX1d5CYfAJy7rVSb2eLY4gO\n" +
            "2qfYx1oegQW81KmfJQ==\n";

    DynamicX509TrustManagerImpl m_dynamicX509TrustManager;

    @Before
    public void setUp() throws Exception {
        List<String> caCerts = new ArrayList<>();
        caCerts.add(CA1_CERT);
        caCerts.add(CA2_CERT);
        m_dynamicX509TrustManager = new DynamicX509TrustManagerImpl(caCerts);
    }

    @Test
    public void testInitializationFromFile() throws TrustManagerInitException {
        // A CA whose Subject is as follows "CN=127.0.0.1, OU=FNBL, O=ALU, L=BLR, ST=KA, C=IN"
        // A CA whose Subject is as follows "CN=CA2, OU=CA2, O=CA2, L=BLR, ST=KA, C=IN"
        String caCertificatePath = getClass().getResource("/keyMgrTest/caCertificate.crt").getPath();
        DynamicX509TrustManagerImpl dynamicX509TrustManager = new DynamicX509TrustManagerImpl(caCertificatePath);

        try {
            dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT1) },
                    "RSA");
            dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT2) },
                    "RSA");
            dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT1) },
                    "RSA");
            dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT2) },
                    "RSA");
        } catch (CertificateException e) {
            fail("checkServerTrusted should not have failed: " + e.getMessage());
        }
        try {
            dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA3_SIGNED_CERT2) },
                    "RSA");
            fail("authentication should not have passed");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
    }

    @Test
    public void testTrustManagerReturnsAcceptedIssuersFromFile() throws TrustManagerInitException {
        String caCertificatePath = getClass().getResource("/keyMgrTest/caCertificate.crt").getPath();
        DynamicX509TrustManagerImpl dynamicX509TrustManager = new DynamicX509TrustManagerImpl(caCertificatePath);
        dynamicX509TrustManager.getAcceptedIssuers();

        X509Certificate[] acceptedIssuers = dynamicX509TrustManager.getAcceptedIssuers();
        assertEquals(2, acceptedIssuers.length);
        
        assertContainsCertificate("CN=CA2, OU=CA2, O=CA2, L=BLR, ST=KA, C=IN", acceptedIssuers);
        assertContainsCertificate("CN=127.0.0.1, OU=FNBL, O=ALU, L=BLR, ST=KA, C=IN", acceptedIssuers);
    }
    
    /**
     * assert that actual certificate array contains expected certificate DN.
     * 
     * Note:- javax.net.ssl.X509TrustManager implementation does not need to guarantee ordering.
     * @param expectedCertDN
     * @param actualCertificates
     */
    private void assertContainsCertificate(String expectedCertDN, X509Certificate[] actualCertificates) {
        for(X509Certificate certificate : actualCertificates) {
            if(expectedCertDN.equals(certificate.getSubjectDN().getName())) {
                //if expected certificate found, then return
                return;
            }
        }
        //expected certificate not found
        fail("expected Certificate with CN:" + expectedCertDN + ", but not found");
    }

    @Test
    public void testTrustManagerValidatesServerCertificates() throws CertificateException {

        try {
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT1) },
                    "RSA");
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT2) },
                    "RSA");
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT1) },
                    "RSA");
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT2) },
                    "RSA");
        } catch (CertificateException e) {
            fail("checkServerTrusted should not have failed: " + e.getMessage());
        }
        try {
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA3_SIGNED_CERT2) },
                    "RSA");
            fail("authentication should not have passed");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }

    }

    @Test
    public void testTrustManagerValidatesClientCertificates() {
        try {
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT1) },
                    "RSA");
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT2) },
                    "RSA");
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT1) },
                    "RSA");
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT2) },
                    "RSA");
        } catch (CertificateException e) {
            fail("checkServerTrusted should not have failed: " + e.getMessage());
        }
        try {
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA3_SIGNED_CERT2) },
                    "RSA");
            fail("authentication should not have passed");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
    }

    @Test
    public void testCertificateChain() throws TrustManagerInitException {
        String rootCACertificate = "MIIDwTCCAqmgAwIBAgIJAObvpCoyTSy0MA0GCSqGSIb3DQEBBQUAMHcxCzAJBgNV\n"
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
                + "KBwa59s47ufvKKS8kZF1lulKE8bZAVW97VypLetNeUi6svW4KXb2T67foF+HoyqC\n"
                + "9MfR/bw=\n";
        List<String> caCerts = new ArrayList<>();
        caCerts.add(rootCACertificate);
        m_dynamicX509TrustManager.initTrustManager(caCerts);
        String level2CASignedCert = "MIIDHjCCAgYCCQCcQh88fcvXFzANBgkqhkiG9w0BAQUFADA9MQswCQYDVQQGEwJJ\n"
                + "TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjETMBEGA1UECgwKbGV2ZWwgMiBD\n"
                + "QTAgFw0xNTA1MDUxMTMxNDlaGA8zMzg0MDQxNzExMzE0OVowYzELMAkGA1UEBhMC\n"
                + "SU4xCzAJBgNVBAgMAktBMQwwCgYDVQQHDANCTFIxOTA3BgNVBAoMMG5ldGNvbmYg\n"
                + "cGVlciBnb2luZyB0byBiZSBjZXJ0aWZpZWQgYnkgbGV2ZWwgMiBDQTCCASIwDQYJ\n"
                + "KoZIhvcNAQEBBQADggEPADCCAQoCggEBANLkUTl9Orn2AHno802U3H+wK+uSWa6D\n"
                + "RHPCXeg0d+OMZJ38BMAAS2Q0XF71B4EIq5v4VpZv+i2bN29zy4chYp4F0VgQDyJU\n"
                + "MaY4SBlLRidJPag1BscaQw9+J1y5wIs1ndjj/hqTg44RYK8kOPdY3KODHhtRYqE+\n"
                + "LsIB1ftyOzEE5XtRzjjW4NRamsN2RaaofTFBoczh3orRkivbMTOTskWpm7epSCxl\n"
                + "6Pjo5kWatv7EtjrC9uQ9zduDyKK3NDozbdu/ibgxOGBtSSEnqS9zYJAnatv+ZaTd\n"
                + "nzCEWgAQ+DBkavIDQX/bwg/QJj6P+wLjXUmfFS4TlSTumcAuNoKnVB8CAwEAATAN\n"
                + "BgkqhkiG9w0BAQUFAAOCAQEAHiq07hYEvDFE8Kjs91b040qRJnrJPE400+44xBll\n"
                + "3/+iBqGdACtbzz5j0UDt3V51bw8QgglW5jwCfKSo2TA1GyGGE49b/3Ebfpm0qkYa\n"
                + "XTig0wJQV6q3N5NWbhoRS8u4xLtMmkela+jJ4Syazcgt7/L4I27Mveb3F8zQ2a8V\n"
                + "VJB0g4pWorZW7GKJs4ulZXaKP79+ARB+aF9jHi4C//aHLabG3jaAiufFAZ8emINe\n"
                + "pNAmikBEltwWqVfp64+2BTdyve5DvTHTBe+0F1Z9uPmIQ3oXrZSrOxn8aI3ScPRQ\n"
                + "PWs9P07JonD9dIswCUmFY8vYAFnys/hSFXLh5sTLQMuI2Q==\n";
        try {

            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(level2CASignedCert) },
                    "RSA");

            fail("this certificate should not be trusted");
        } catch (CertificateException e) {
            // we are ok
        }

        // now add level2CA certificate as well
        String level2CACertSignedByrootCA = "MIIDMjCCAhoCCQC/koquIw52kjANBgkqhkiG9w0BAQUFADB3MQswCQYDVQQGEwJJ\n"
                + "TjELMAkGA1UECAwCS0ExEjAQBgNVBAcMCUJhbmdhbG9yZTEXMBUGA1UECgwOQWxj\n"
                + "YXRlbCBMdWNlbnQxDTALBgNVBAsMBEZOQkwxHzAdBgNVBAMMFnd3dy5hbGNhdGVs\n"
                + "LWx1Y2VudC5jb20wIBcNMTUwNTA1MTEwMTQzWhgPMzM4NDA0MTcxMTAxNDNaMD0x\n"
                + "CzAJBgNVBAYTAklOMQswCQYDVQQIDAJLQTEMMAoGA1UEBwwDQkxSMRMwEQYDVQQK\n"
                + "DApsZXZlbCAyIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvT3Z\n"
                + "Ulv97vPkk0qjWaa0Bo2u1MJ7u10Iam6nyq4q4r0hSdKuGLb03pnNO1EvncsmHm2o\n"
                + "lYyYjsnucKgHtGV4rYQKTLiDVqUYeX91fgzbBX6RvnlUpx6EpvbcjtMi5C1Vd5D7\n"
                + "bWDqabcsnIJxwMcgr12C46dgb0HP0viMIOrzj6I1SOlYmalHLTpuJWeZ2wxNPA04\n"
                + "CBn9JmLiq7T7HSCkCWz/DTgnpAaszZTAD61SfArpL2o4I7ru3an+TbTSaufcIQu+\n"
                + "2vuB5d4OXf/Ee7pW5Nup+trTaMgZlbMzYSfU62tIzMLoA0BhdLBoWKyrtEQ8AJty\n"
                + "KHs+bC+yfNyOXibykQIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQBQXPESQ3E9C12+\n"
                + "RZXQTNCNGu0s7R88Q8csRF6mV4OZFTli2x+9Kgi4REoy/Ih15DVxnMJ5685F+qUT\n"
                + "HgYFBbAhEtHmi1RdEcEK57ykUWhr1c5naZAkVOnhcIrlp9kI8ZTlBZvcNKXeJ7qv\n"
                + "G2brqAIpuJx7O9uN6sQKVpppriq3BBPlDHK5y5ANsOmD/ihhXtqmwiOlze+BA3pC\n"
                + "3Vd4E9k9oUazrk0fBv1zMI/m5sfGV/dR0oRph1m0N9D5DtHxxohB3lD1g9DzzSfb\n"
                + "WAFSCHV4QWvUJFhpBRfsc8pBhtYGSiTAlrWa7SGPfX+kiAaR+kNS0XXbmKOp7kvX\n"
                + "r/9rT1ce\n";
        caCerts.add(level2CACertSignedByrootCA);
        m_dynamicX509TrustManager.initTrustManager(caCerts);
        try {
            LOGGER.info(m_dynamicX509TrustManager.getAcceptedIssuers());
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(level2CASignedCert) },
                    "RSA");

        } catch (CertificateException e) {
            fail("this certificate should be trusted: " + e.getMessage());
        }

    }

    @Test
    public void testReinitialisingTrustManagerWorksForClient() throws TrustManagerInitException {
        // Lets re-initialise the trust manager with a 3rd CA (removing the old ones)
        List<String> caCerts = new ArrayList<>();
        caCerts.add(CA3_CERT);
        m_dynamicX509TrustManager.initTrustManager(caCerts);

        X509Certificate[] acceptedIssuers = m_dynamicX509TrustManager.getAcceptedIssuers();
        assertEquals(1, acceptedIssuers.length);
        assertEquals("EMAILADDRESS=CA3@ca.org, CN=CA3, OU=CA3, O=ALU, L=CA3, ST=KA, C=IN", acceptedIssuers[0].getSubjectDN().getName());
        try {
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT1) },
                    "RSA");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
        try {
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT2) },
                    "RSA");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
        try {
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT1) },
                    "RSA");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
        try {
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT2) },
                    "RSA");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
        try {
            m_dynamicX509TrustManager.checkClientTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA3_SIGNED_CERT2) },
                    "RSA");
        } catch (CertificateException e) {
            fail("authentication should have passed");
        }
    }

    @Test
    public void testReinitialisingTrustManagerWorksForServer() throws TrustManagerInitException {
        // Lets re-initialise the trust manager with a 3rd CA (removing the old ones)
        List<String> caCerts = new ArrayList<>();
        caCerts.add(CA3_CERT);
        m_dynamicX509TrustManager.initTrustManager(caCerts);

        X509Certificate[] acceptedIssuers = m_dynamicX509TrustManager.getAcceptedIssuers();
        assertEquals(1, acceptedIssuers.length);
        assertEquals("EMAILADDRESS=CA3@ca.org, CN=CA3, OU=CA3, O=ALU, L=CA3, ST=KA, C=IN", acceptedIssuers[0].getSubjectDN().getName());
        try {
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT1) },
                    "RSA");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
        try {
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA1_SIGNED_CERT2) },
                    "RSA");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
        try {
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT1) },
                    "RSA");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
        try {
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA2_SIGNED_CERT2) },
                    "RSA");
        } catch (CertificateException e) {
            // we are ok
            LOGGER.info("Got an expected exception ");
        }
        try {
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CA3_SIGNED_CERT2) },
                    "RSA");
        } catch (CertificateException e) {
            fail("authentication should have passed");
        }
    }

    @Test
    public void testTrustManagerReturnsAcceptedIssuers() {
        X509Certificate[] acceptedIssuers = m_dynamicX509TrustManager.getAcceptedIssuers();
        assertEquals(2, acceptedIssuers.length);
        assertContainsCertificate("CN=CA2, OU=CA2, O=CA2, L=BLR, ST=KA, C=IN", acceptedIssuers);
        assertContainsCertificate("CN=127.0.0.1, OU=FNBL, O=ALU, L=BLR, ST=KA, C=IN", acceptedIssuers);
    }

    @Test
    public void testDelegation(){
        X509ExtendedTrustManager mockTM = mock(X509ExtendedTrustManager.class);
        m_dynamicX509TrustManager.setInnerTrustManager(mockTM);
        X509Certificate [] mockCerts = new X509Certificate[2];
        mockCerts[0] = mock(X509Certificate.class);
        mockCerts[1]= mock(X509Certificate.class);
        String authType = "authType";

        try {
            doThrow(new CertificateException()).when(mockTM).checkClientTrusted(mockCerts, authType);
            m_dynamicX509TrustManager.checkClientTrusted(mockCerts, authType);
            fail("expectedException here");
        } catch (CertificateException e) {
            assertTrue(e instanceof PeerCertificateException);
            assertEquals(mockCerts, ((PeerCertificateException)e).getPeerCertificates());
        }
        try {
            SSLEngine mockSslEngine = mock(SSLEngine.class);
            doThrow(new CertificateException()).when(mockTM).checkClientTrusted(mockCerts, authType, mockSslEngine);
            m_dynamicX509TrustManager.checkClientTrusted(mockCerts, authType, mockSslEngine);
            fail("expectedException here");
        } catch (CertificateException e) {
            assertTrue(e instanceof PeerCertificateException);
            assertEquals(mockCerts, ((PeerCertificateException)e).getPeerCertificates());
        }
        try {
            Socket mockSocket = mock(Socket.class);
            doThrow(new CertificateException()).when(mockTM).checkClientTrusted(mockCerts, authType, mockSocket);
            m_dynamicX509TrustManager.checkClientTrusted(mockCerts, authType, mockSocket);
            fail("expectedException here");
        } catch (CertificateException e) {
            assertTrue(e instanceof PeerCertificateException);
            assertEquals(mockCerts, ((PeerCertificateException)e).getPeerCertificates());
        }

        try {
            doThrow(new CertificateException()).when(mockTM).checkServerTrusted(mockCerts, authType);
            m_dynamicX509TrustManager.checkServerTrusted(mockCerts, authType);
            fail("expectedException here");
        } catch (CertificateException e) {
            assertTrue(e instanceof PeerCertificateException);
            assertEquals(mockCerts, ((PeerCertificateException)e).getPeerCertificates());
        }
        try {
            SSLEngine mockSslEngine = mock(SSLEngine.class);
            doThrow(new CertificateException()).when(mockTM).checkServerTrusted(mockCerts, authType, mockSslEngine);
            m_dynamicX509TrustManager.checkServerTrusted(mockCerts, authType, mockSslEngine);
            fail("expectedException here");
        } catch (CertificateException e) {
            assertTrue(e instanceof PeerCertificateException);
            assertEquals(mockCerts, ((PeerCertificateException)e).getPeerCertificates());
        }
        try {
            Socket mockSocket = mock(Socket.class);
            doThrow(new CertificateException()).when(mockTM).checkServerTrusted(mockCerts, authType, mockSocket);
            m_dynamicX509TrustManager.checkServerTrusted(mockCerts, authType, mockSocket);
            fail("expectedException here");
        } catch (CertificateException e) {
            assertTrue(e instanceof PeerCertificateException);
            assertEquals(mockCerts, ((PeerCertificateException)e).getPeerCertificates());
        }
    }

    @Test
    public void testTrustManagerWorksWithExtensions() throws TrustManagerInitException {
        List<String> caCerts = new ArrayList<>();
        caCerts.add(CA_CERTIFICATE_WITH_EXTENSION);
        m_dynamicX509TrustManager = new DynamicX509TrustManagerImpl(caCerts);
        try {
            m_dynamicX509TrustManager.checkServerTrusted(new X509Certificate[] { CertificateUtil.getX509Certificate(CERT_SIGNED_WITH_EXTENSION) },
                    "RSA");
        } catch (CertificateException e) {
            fail("checkServerTrusted should not have failed: " + e.getMessage());
        }

    }

}
