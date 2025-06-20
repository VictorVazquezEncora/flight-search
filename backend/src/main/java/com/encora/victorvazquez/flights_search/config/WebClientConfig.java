package com.encora.victorvazquez.flights_search.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.core.publisher.Mono;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class WebClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);
    private static final String AUTH_ENDPOINT = "/v1/security/oauth2/token";
    private static final int MAX_MEMORY_SIZE = 32 * 1024 * 1024;
    private static final int TIMEOUT = 30000;
    
    private final AmadeusProperties amadeusProperties;
    private final AtomicReference<String> currentToken = new AtomicReference<>();
    private final AtomicReference<Instant> tokenExpiration = new AtomicReference<>();

    public WebClientConfig(AmadeusProperties amadeusProperties) {
        System.out.println("WebClientConfig - Constructor called");
        
        if (amadeusProperties == null) {
            String error = "AmadeusProperties cannot be null";
            logger.error(error);
            System.err.println("ERROR: " + error);
            throw new IllegalArgumentException(error);
        }
        
        String baseUrl = amadeusProperties.getBaseUrl();
        String keyLength = amadeusProperties.getKey() != null ? String.valueOf(amadeusProperties.getKey().length()) : "null";
        String secretLength = amadeusProperties.getSecret() != null ? String.valueOf(amadeusProperties.getSecret().length()) : "null";
        
        System.out.println("WebClientConfig - Base URL: " + baseUrl);
        System.out.println("WebClientConfig - API Key Length: " + keyLength);
        System.out.println("WebClientConfig - Secret Length: " + secretLength);
        
        logger.warn("WebClientConfig initialization - Base URL: {}", baseUrl);
        logger.warn("WebClientConfig initialization - API Key Length: {}", keyLength);
        logger.warn("WebClientConfig initialization - Secret Length: {}", secretLength);

        this.amadeusProperties = amadeusProperties;
    }

    @Bean
    @Primary
    public WebClient amadeusWebClient(AmadeusProperties amadeusProperties) {
        System.out.println("WebClientConfig - Creating Amadeus WebClient");
        logger.warn("Creating Amadeus WebClient");
        
        String baseUrl = amadeusProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            String error = "Amadeus API base URL is not configured. Please check your application.yml";
            logger.error(error);
            System.err.println("ERROR: " + error);
            throw new IllegalStateException(error);
        }

        System.out.println("WebClientConfig - Configuring WebClient with base URL: " + baseUrl);
        logger.warn("Configuring WebClient with base URL: {}", baseUrl);

        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
            .responseTimeout(Duration.ofMillis(TIMEOUT))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS)))
            .wiretap(true)
            .keepAlive(true)
            .followRedirect(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(MAX_MEMORY_SIZE))
                .baseUrl(baseUrl)
                .filter((request, next) -> {
                    if (needsNewToken()) {
                        return refreshToken()
                            .then(Mono.fromCallable(() -> {
                                ClientRequest newRequest = ClientRequest.from(request)
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + currentToken.get())
                                    .build();
                                return next.exchange(newRequest);
                            }))
                            .flatMap(mono -> mono);
                    }
                    ClientRequest newRequest = ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + currentToken.get())
                            .build();
                    return next.exchange(newRequest);
                })
                .build();
    }

    private synchronized boolean needsNewToken() {
        Instant now = Instant.now();
        boolean needs = currentToken.get() == null || 
                       tokenExpiration.get() == null || 
                       now.isAfter(tokenExpiration.get()) ||
                       now.plus(Duration.ofMinutes(1)).isAfter(tokenExpiration.get());
        
        System.out.println("WebClientConfig - Needs new token: " + needs);
        if (needs) {
            System.out.println("WebClientConfig - Current time: " + now);
            System.out.println("WebClientConfig - Token expiration: " + tokenExpiration.get());
        }
        logger.warn("Needs new token: {}", needs);
        return needs;
    }

    private Mono<Void> refreshToken() {
        System.out.println("REFRESHING TOKEN");
        
        String apiKey = amadeusProperties.getKey();
        String apiSecret = amadeusProperties.getSecret();

        System.out.println("apiKey dough: " + apiKey);
        System.out.println("apiSecret douch: " + apiSecret);
        
        if (apiKey == null || apiKey.trim().isEmpty() ||
            apiSecret == null || apiSecret.trim().isEmpty()) {
            return Mono.error(new IllegalStateException("Amadeus API credentials are not configured. Please check your application.yml"));
        }

        apiKey = apiKey.trim();
        apiSecret = apiSecret.trim();

        System.out.println("apiKey after trim: " + apiKey);
        System.out.println("apiSecret after trim: " + apiSecret);

        WebClient authClient = WebClient.builder()
                .baseUrl(amadeusProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(MAX_MEMORY_SIZE))
                .build();

        System.out.println("authClient: " + authClient);
        String requestBody = "grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + apiSecret;
        logger.debug("Request body format: {}", requestBody.replaceAll(apiKey, "API_KEY").replaceAll(apiSecret, "API_SECRET"));

        System.out.println("requestBody: " + requestBody);

        return authClient.post()
                .uri(AUTH_ENDPOINT)
                .body(BodyInserters.fromValue(requestBody))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> {
                                    logger.error("Authentication failed. Response: {}", body);
                                    return new IllegalStateException(
                                        "Authentication failed with Amadeus API. Status: " + response.statusCode() + 
                                        ". Response: " + body);
                                }))
                .bodyToMono(AmadeusTokenResponse.class)
                .flatMap(tokenResponse -> {
                    if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                        logger.debug("Successfully obtained new access token");
                        currentToken.set(tokenResponse.getAccessToken());
                        tokenExpiration.set(Instant.now().plus(Duration.ofSeconds(tokenResponse.getExpiresIn() - 60)));
                        return Mono.empty().then();
                    } else {
                        return Mono.error(new IllegalStateException("Received null or invalid token response from Amadeus API"));
                    }
                })
                .onErrorMap(e -> {
                    logger.error("Failed to obtain Amadeus API token", e);
                    return new IllegalStateException("Failed to obtain Amadeus API token: " + e.getMessage(), e);
                });
    }
}