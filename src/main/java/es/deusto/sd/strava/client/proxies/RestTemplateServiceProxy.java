/**
 * This code is based on solutions provided by Claude Sonnet 3.5 and 
 * adapted using GitHub Copilot. It has been thoroughly reviewed 
 * and validated to ensure correctness and that it is free of errors.
 */
package es.deusto.sd.strava.client.proxies;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import es.deusto.sd.strava.client.data.Article;
import es.deusto.sd.strava.client.data.Category;
import es.deusto.sd.strava.client.data.Credentials;

/**
 * RestTemplateServiceProxy class is an implementation of the Service Proxy design pattern.
 * This class acts as an intermediary between the client and the RESTful web service,
 * encapsulating all the REST API calls using Spring's RestTemplate and handling various 
 * exceptions that may occur during these interactions. This class serves as an intermediary 
 * for the client to perform CRUD operations, such as user authentication (login/logout),
 * retrieving categories and articles, and placing bids on articles. By encapsulating 
 * the HTTP request logic and handling various exceptions, this proxy provides a cleaner 
 * interface for clients to interact with the underlying service.
 * 
 * The @Service annotation indicates that this class is a Spring service component, 
 * which allows it to be detected and managed by the Spring container. This enables 
 * dependency injection for the RestTemplate instance, promoting loose coupling and 
 * enhancing testability.
 * 
 * RestTemplate is a synchronous client provided by Spring for making HTTP requests. 
 * It simplifies the interaction with RESTful services by providing a higher-level 
 * abstraction over the lower-level `HttpURLConnection`. Particularities of using 
 * RestTemplate include its capability to automatically convert HTTP responses into 
 * Java objects using message converters, support for various HTTP methods (GET, POST, 
 * PUT, DELETE), and built-in error handling mechanisms. However, it's important to 
 * note that since RestTemplate is synchronous, it can block the calling thread, which 
 * may not be suitable for high-performance applications that require non-blocking 
 * behavior.
 * 
 * (Description generated with ChatGPT 4o mini)
 */
@Service
public class RestTemplateServiceProxy implements IAuctionsServiceProxy{

    private final RestTemplate restTemplate;

    @Value("${api.base.url}")
    private String apiBaseUrl;

    public RestTemplateServiceProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String login(Credentials credentials) {
        String url = apiBaseUrl + "/auth/login";
        
        try {
            return restTemplate.postForObject(url, credentials, String.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Login failed: Invalid credentials.");
                default -> throw new RuntimeException("Login failed: " + e.getStatusText());
            }
        }
    }
    
    @Override    
    public void logout(String token) {
        String url = apiBaseUrl + "/auth/logout";
        
        try {
            restTemplate.postForObject(url, token, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("Logout failed: Invalid token.");
                default -> throw new RuntimeException("Logout failed: " + e.getStatusText());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Category> getAllCategories() {
        String url = apiBaseUrl + "/auctions/categories";
        
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 404 -> throw new RuntimeException("No categories found.");
                default -> throw new RuntimeException("Failed to retrieve categories: " + e.getStatusText());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Article> getArticlesByCategory(String categoryName, String currency) {
        String url = apiBaseUrl + "/auctions/categories/" + categoryName + "/articles?currency=" + currency;
        
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 404 -> throw new RuntimeException("Category not found: " + categoryName);
                case 400 -> throw new RuntimeException("Invalid currency: " + currency);
                default -> throw new RuntimeException("Failed to retrieve articles: " + e.getStatusText());
            }
        }
    }

    @Override
    public Article getArticleDetails(Long articleId, String currency) {
        String url = apiBaseUrl + "/auctions/articles/" + articleId + "/details?currency=" + currency;
        
        try {
            return restTemplate.getForObject(url, Article.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 404 -> throw new RuntimeException("Article not found: ID " + articleId);
                case 400 -> throw new RuntimeException("Invalid currency: " + currency);
                default -> throw new RuntimeException("Failed to retrieve article details: " + e.getStatusText());
            }
        }
    }
    
    @Override
    public void makeBid(Long articleId, Float amount, String currency, String token) {
    	String url = apiBaseUrl + "/auctions/articles/" + articleId + "/bid?amount=" +  amount + "&currency=" + currency;
        
        try {
            restTemplate.postForObject(url, token, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw new RuntimeException("User not authenticated");
                case 404 -> throw new RuntimeException("Article not found");
                case 400 -> throw new RuntimeException("Invalid currency: " + currency);
                case 409 -> throw new RuntimeException("Bid amount must be greater than the current price");
                case 204 -> { /* Successful bid */ }
                case 500 -> throw new RuntimeException("Internal server error while processing bid");
                default -> throw new RuntimeException("Bid failed with status code: " + e.getStatusCode());
            }
        }
    }
}