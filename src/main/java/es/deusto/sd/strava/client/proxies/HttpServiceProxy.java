/**
 * This code is based on solutions provided by Claude Sonnet 3.5 and 
 * adapted using GitHub Copilot. It has been thoroughly reviewed 
 * and validated to ensure correctness and that it is free of errors.
 */
package es.deusto.sd.strava.client.proxies;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.deusto.sd.strava.client.data.Article;
import es.deusto.sd.strava.client.data.Category;
import es.deusto.sd.strava.client.data.Credentials;

/**
 * HttpServiceProxy class is an implementation of the Service Proxy design pattern
 * that communicates with the AuctionsService using simple HTTP requests via Java's
 * HttpClient. This class serves as an intermediary for the client to perform 
 * CRUD operations, such as user authentication (login/logout), retrieving categories 
 * and articles, and placing bids on articles. By encapsulating the HTTP request logic 
 * and handling various exceptions, this proxy provides a cleaner interface for clients 
 * to interact with the underlying service.
 * 
 * The class uses Java's HttpClient which allows for asynchronous and synchronous 
 * communication with HTTP servers. It leverages the `HttpRequest` and `HttpResponse` 
 * classes to construct and send requests, simplifying the process of making HTTP calls. 
 * The ObjectMapper from the Jackson library is employed to serialize and deserialize 
 * JSON data, facilitating easy conversion between Java objects and their JSON 
 * representations. This is particularly useful for converting complex data structures, 
 * like the `Credentials`, `Category`, and `Article` classes, into JSON format for 
 * transmission in HTTP requests, and vice versa for processing the responses.
 * 
 * The absence of the @Service annotation indicates that this class is not managed 
 * by a Spring container, which means that it will not benefit from Spring's 
 * dependency injection features. Instead, it operates independently, which can 
 * be suitable for applications preferring a more lightweight approach without 
 * the overhead of a full Spring context.
 * 
 * (Description generated with ChatGPT 4o mini)
 */
public class HttpServiceProxy implements IAuctionsServiceProxy {
    private static final String BASE_URL = "http://localhost:8081";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpServiceProxy() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String login(Credentials credentials) {
        try {
            String credentialsJson = objectMapper.writeValueAsString(credentials);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(credentialsJson))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> response.body(); // Successful login, returns token
                case 401 -> throw new RuntimeException("Unauthorized: Invalid credentials");
                default -> throw new RuntimeException("Login failed with status code: " + response.statusCode());
            };
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error during login", e);
        }
    }

    @Override
    public void logout(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/logout"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(token))
                .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            switch (response.statusCode()) {
                case 204 -> {} // Logout successful
                case 401 -> throw new RuntimeException("Unauthorized: Invalid token, logout failed");
                default -> throw new RuntimeException("Logout failed with status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error during logout", e);
        }
    }

    @Override
    public List<Category> getAllCategories() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auctions/categories"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, Category.class));
                case 204 -> throw new RuntimeException("No Content: No categories found");
                case 500 -> throw new RuntimeException("Internal server error while fetching categories");
                default -> throw new RuntimeException("Failed to fetch categories with status code: " + response.statusCode());
            };
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error while fetching categories", e);
        }
    }
    
    @Override
    public List<Article> getArticlesByCategory(String categoryName, String currency) {
        try {
            // Encode the category name to handle spaces and special characters
            String encodedCategoryName = URLEncoder.encode(categoryName, StandardCharsets.UTF_8);
        	
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auctions/categories/" + encodedCategoryName + "/articles?currency=" + currency))
                .header("Content-Type", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, Article.class));
                case 204 -> throw new RuntimeException("No Content: Category has no articles");
                case 400 -> throw new RuntimeException("Bad Request: Currency not supported");
                case 404 -> throw new RuntimeException("Not Found: Category not found");
                case 500 -> throw new RuntimeException("Internal server error while fetching articles");
                default -> throw new RuntimeException("Failed to fetch articles with status code: " + response.statusCode());
            };
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error while fetching articles by category", e);
        }
    }

    @Override
    public Article getArticleDetails(Long articleId, String currency) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auctions/articles/" + articleId + "/details?currency=" + currency))
                .header("Content-Type", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(response.body(), Article.class);
                case 400 -> throw new RuntimeException("Bad Request: Currency not supported");
                case 404 -> throw new RuntimeException("Not Found: Article not found");
                case 500 -> throw new RuntimeException("Internal server error while fetching article details");
                default -> throw new RuntimeException("Failed to fetch article details with status code: " + response.statusCode());
            };
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error while fetching article details", e);
        }
    }

    @Override
    public void makeBid(Long articleId, Float amount, String currency, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auctions/articles/" + articleId + "/bid?amount=" + amount + "&currency=" + currency))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(token))
                .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            switch (response.statusCode()) {
                case 204 -> {} // Bid placed successfully
                case 400 -> throw new RuntimeException("Bad Request: Currency not supported");
                case 401 -> throw new RuntimeException("Unauthorized: User not authenticated");
                case 404 -> throw new RuntimeException("Not Found: Article not found");
                case 409 -> throw new RuntimeException("Conflict: Bid amount must be greater than the current price");
                case 500 -> throw new RuntimeException("Internal server error while placing a bid");
                default -> throw new RuntimeException("Failed to make a bid with status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error while making a bid", e);
        }
    }
}