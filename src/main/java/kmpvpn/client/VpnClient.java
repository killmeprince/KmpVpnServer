package kmpvpn.client;


import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


// i prefer slf4j cauze i want to track the logs in all phases of program

public class VpnClient {
    private static final Logger logger = LoggerFactory.getLogger(VpnClient.class);


    public static void main(String[] args) {
        logger.info("Vpn is running");
        VpnClient client = new VpnClient();
        client.connectToVpnServer("");
    }

    public void connectToVpnServer(String vpnServerUrl) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(vpnServerUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                logger.info("Vpn service is connected to{}", vpnServerUrl);
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.info("Server response{}" , responseBody);
            }
        } catch (IOException e) {
            logger.info("error connecting to vpn server",e);
        }
    }
    private CloseableHttpClient createSslHttpClient() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true)//all certificates
                    .build();

            return HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException  | KeyStoreException e) {
            logger.error("error creating SSL context",e);
            throw new RuntimeException();
        }
    }
    public void sendDataToVpnServer(String vpnServerUrl,String data) {
        try (CloseableHttpClient httpClient = createSslHttpClient()) {
            HttpPost post = new HttpPost(vpnServerUrl);
            post.setEntity(new StringEntity(data));
            post.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                logger.info("Data sent to Vpn server: {}", data);
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.info("VPN Server response: {}", responseBody);
            }
        }catch (IOException e) {
            logger.error("error sending data to vpn server", e);
        }
    }


}
