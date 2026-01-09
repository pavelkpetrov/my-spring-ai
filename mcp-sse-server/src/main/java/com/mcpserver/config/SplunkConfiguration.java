package com.mcpserver.config;

import com.splunk.SSLSecurityProtocol;
import com.splunk.Service;
import com.splunk.ServiceArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * Configuration for Splunk service connection
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SplunkConfiguration {

    private final SplunkProperties splunkProperties;

    /**
     * Creates and configures Splunk Service bean
     */
    @Bean
    public Service splunkService() {
        try {
            // Disable SSL verification if configured
            if (!splunkProperties.getSslVerify()) {
                disableSSLVerification();
            }

            ServiceArgs serviceArgs = new ServiceArgs();
            serviceArgs.setHost(splunkProperties.getHost());
            serviceArgs.setPort(splunkProperties.getPort());
            serviceArgs.setUsername(splunkProperties.getUsername());
            serviceArgs.setPassword(splunkProperties.getPassword());
            serviceArgs.setScheme(splunkProperties.getScheme());

            if (!splunkProperties.getSslVerify()) {
                serviceArgs.setSSLSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
            }

            Service service = Service.connect(serviceArgs);

            log.info("Successfully connected to Splunk at {}://{}:{}",
                    splunkProperties.getScheme(),
                    splunkProperties.getHost(),
                    splunkProperties.getPort());

            return service;
        } catch (Exception e) {
            log.error("Failed to connect to Splunk: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Splunk connection", e);
        }
    }

    /**
     * Disables SSL certificate verification (for development/testing only)
     */
    private void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            log.warn("SSL verification has been disabled - use only for development/testing!");
        } catch (Exception e) {
            log.error("Failed to disable SSL verification", e);
        }
    }
}