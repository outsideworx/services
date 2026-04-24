package application;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@org.springframework.cache.annotation.EnableCaching
@org.springframework.boot.autoconfigure.SpringBootApplication
public class SpringApplication {
    public static void main(String[] args) throws Exception {
        trustAllSsl();
        org.springframework.boot.SpringApplication.run(SpringApplication.class, args);
    }

    private static void trustAllSsl() throws Exception {
        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }}, null);
        SSLContext.setDefault(sslContext);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}