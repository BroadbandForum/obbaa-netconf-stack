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

package org.broadband_forum.obbaa.netconf.client.tls;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.FailedFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.SucceededFuture;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.authentication.FailureInfo;
import org.broadband_forum.obbaa.netconf.api.authentication.PointOfFailure;
import org.broadband_forum.obbaa.netconf.api.authentication.SuccessInfo;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.x509certificates.ByteArrayCertificate;
import org.broadband_forum.obbaa.netconf.api.x509certificates.CertificateUtil;

public class SslFutureChannelListenerTest {

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

    private SocketChannel m_channel;
    private InetSocketAddress m_inetSocketAddress;
    private AuthenticationListener m_authenticationListener;
    private boolean m_selfSigned;
    private Set<String> m_capabilities;
    private NotificationListener m_notificationListener;
    private CallHomeListener m_callHomeListener;
    private Certificate[] m_peerCertificateChain;
    private X509Certificate m_peerCertificate;

    @Before
    public void setUp() throws CertificateException {
        m_channel = mock(SocketChannel.class);
        m_inetSocketAddress = new InetSocketAddress("127.0.0.1", 830);
        when(m_channel.remoteAddress()).thenReturn(m_inetSocketAddress);
        ChannelPipeline channelPipeline = mock(ChannelPipeline.class);
        when(m_channel.pipeline()).thenReturn(channelPipeline);

        m_authenticationListener = mock(AuthenticationListener.class);

        m_selfSigned = true;

        m_capabilities = new HashSet<String>();
        m_capabilities.add(NetconfResources.NETCONF_BASE_CAP_1_1);

        m_notificationListener = mock(NotificationListener.class);

        m_callHomeListener = mock(CallHomeListener.class);

        List<ByteArrayCertificate> byteCertificates = CertificateUtil.getByteArrayCertificates(Arrays.asList
                (PEER_CERT, CA_CERT, ROOT_CA_CERT));
        m_peerCertificateChain = CertificateUtil.getX509Certificates(byteCertificates).toArray(new Certificate[0]);
        m_peerCertificate = CertificateUtil.getX509Certificate(PEER_CERT);
    }

    @Test
    public void testOperationCompleteWhenHandShakeSuccess() throws Exception {
        // dummy executor
        ExecutorService executorService = mock(ExecutorService.class);
        doAnswer(new Answer<Future<?>>() {
            @Override
            public Future<?> answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(executorService).execute(any(Runnable.class));
        SSLEngine sSLEngine = createSSLSession();


        // prepare component to test
        SslFutureChannelListener sslFutureChannelListener = new SslFutureChannelListener(sSLEngine, m_channel,
                m_authenticationListener,
                m_selfSigned, m_capabilities, m_notificationListener, executorService, executorService,
                m_callHomeListener);

        // prepare test input handshake success future
        Future<Channel> futureChannel = new SucceededFuture<Channel>(null, m_channel);

        // invoke handshake success
        sslFutureChannelListener.operationComplete(futureChannel);

        // verify handshake success
        SuccessInfo successInfo = new SuccessInfo().setIp(m_inetSocketAddress.getAddress().getHostAddress()).setPort(
                m_inetSocketAddress.getPort()).setPeerCertificate(m_peerCertificate);
        verify(m_authenticationListener, times(1)).authenticationSucceeded(successInfo);

        verify(executorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any
                        (NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testOperationCompleteWhenHandShakeSuccessButExecutorRejected() throws Exception {
        // dummy executor
        ExecutorService executorService = mock(ExecutorService.class);
        doThrow(new RejectedExecutionException("executor service queue reached max allowed limit")).when
                (executorService).submit(
                any(Runnable.class));
        SSLEngine sSLEngine = createSSLSession();

        // prepare component to test
        SslFutureChannelListener sslFutureChannelListener = new SslFutureChannelListener(sSLEngine, m_channel,
                m_authenticationListener,
                m_selfSigned, m_capabilities, m_notificationListener, executorService, executorService,
                m_callHomeListener);

        // prepare test input handshake success future
        Future<Channel> futureChannel = new SucceededFuture<Channel>(null, m_channel);

        // invoke handshake success
        sslFutureChannelListener.operationComplete(futureChannel);

        // verify handshake success
        SuccessInfo successInfo = new SuccessInfo().setIp(m_inetSocketAddress.getAddress().getHostAddress()).setPort(
                m_inetSocketAddress.getPort()).setPeerCertificate(m_peerCertificate);
        verify(m_authenticationListener, times(1)).authenticationSucceeded(successInfo);

        verify(executorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(0)).connectionEstablished(any(NetconfClientSession.class), any
                        (NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    private SSLEngine createSSLSession() throws SSLPeerUnverifiedException {
        SSLEngine sSLEngine = mock(SSLEngine.class);
        SSLSession sSLSession = mock(SSLSession.class);
        when(sSLEngine.getSession()).thenReturn(sSLSession);
        when(sSLSession.getPeerCertificates()).thenReturn(m_peerCertificateChain);
        return sSLEngine;
    }

    @Test
    public void testOperationCompleteWhenHandShakeFailed() throws Exception {
        // dummy executor
        ExecutorService executorService = mock(ExecutorService.class);
        doAnswer(new Answer<Future<?>>() {
            @Override
            public Future<?> answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(executorService).submit(any(Runnable.class));
        SSLEngine sSLEngine = mock(SSLEngine.class);
        SSLSession sSLSession = mock(SSLSession.class);
        when(sSLEngine.getHandshakeSession()).thenReturn(sSLSession);

        // prepare component to test
        SslFutureChannelListener sslFutureChannelListener = new SslFutureChannelListener(sSLEngine, m_channel,
                m_authenticationListener,
                m_selfSigned, m_capabilities, m_notificationListener, executorService, executorService,
                m_callHomeListener);

        // prepare test input handshake failed future
        FailedFuture<Channel> failedFuture = new FailedFuture<Channel>(null, new Throwable());

        // invoke handshake success
        sslFutureChannelListener.operationComplete(failedFuture);

        // verify handshake success
        FailureInfo failureInfo = new FailureInfo().setIp(m_inetSocketAddress.getAddress().getHostAddress())
                .setPort(m_inetSocketAddress.getPort()).setPointOfFailure(PointOfFailure.server);
        verify(m_authenticationListener, times(1)).authenticationFailed(failureInfo);

        verify(executorService, times(0)).submit(any(Runnable.class));

        verify(m_callHomeListener, times(0)).connectionEstablished(any(NetconfClientSession.class), any
                        (NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

}
