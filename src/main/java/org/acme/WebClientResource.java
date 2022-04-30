package org.acme;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.client.api.QuarkusRestClientProperties;

import javax.enterprise.context.ApplicationScoped;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class WebClientResource {
    Map<String, WebClient> baseUrlToClient = new ConcurrentHashMap<>();

    public Uni<String> doGETRequest(String fullUrl) {
        return Uni.createFrom().item(fullUrl)
            .flatMap(url -> {
                String[] baseAndRelative = splitUrl(fullUrl);
                return registerService(baseAndRelative[0]).getByEndpoint(baseAndRelative[1]);
            })
            .onFailure().invoke(e -> Log.errorf("GET request to %s failed: %s", fullUrl, e))
            .onFailure().recoverWithNull();
    }

    // List of client properties:
    // https://github.com/quarkusio/quarkus/blob/main/independent-projects/
    // resteasy-reactive/client/runtime/src/main/java/org/jboss/resteasy/reactive/client/api/
    // QuarkusRestClientProperties.java
    private WebClient registerService(String baseUrl) {
        return baseUrlToClient.computeIfAbsent(baseUrl, key ->
            RestClientBuilder.newBuilder()
                .baseUri(URI.create(key))
                .followRedirects(true)
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(1000, TimeUnit.MILLISECONDS)
                .property(QuarkusRestClientProperties.CONNECTION_TTL, 25000)
                .property(QuarkusRestClientProperties.CONNECTION_POOL_SIZE, 200)
                .property(QuarkusRestClientProperties.NAME, "my-single-client")
                .property(QuarkusRestClientProperties.SHARED, true)
                .build(WebClient.class)
        );
    }

    private static String[] splitUrl(String fullUrl) {
        try {
            var url = new URL(fullUrl);
            String baseUrl = StringUtils.splitByWholeSeparator(fullUrl, url.getPath())[0];
            String relativePath = StringUtils.removeStart(fullUrl.replace(baseUrl, ""), "/");
            return new String[]{baseUrl, relativePath};
        } catch (ArrayIndexOutOfBoundsException | MalformedURLException e) {
            return new String[]{};
        }
    }
}
