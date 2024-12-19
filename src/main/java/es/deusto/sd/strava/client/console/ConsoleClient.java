/**
 * This code is based on solutions provided by Claude Sonnet 3.5 and 
 * adapted using GitHub Copilot. It has been thoroughly reviewed 
 * and validated to ensure correctness and that it is free of errors.
 */
package es.deusto.sd.strava.client.console;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.deusto.sd.strava.client.data.Article;
import es.deusto.sd.strava.client.data.Category;
import es.deusto.sd.strava.client.data.Credentials;
import es.deusto.sd.strava.client.proxies.HttpServiceProxy;
import es.deusto.sd.strava.client.proxies.IAuctionsServiceProxy;

/**
 * ConsoleClient class serves as a basic client implementation for the AuctionsService 
 * in a console environment. This class manages user interactions through the console, 
 * performing operations such as user login, loading categories, retrieving articles, 
 * and placing bids. It utilizes the IAuctionsServiceProxy to interact with the service layer, 
 * enabling the application to execute various auction-related functionalities.
 * 
 * (Description generated with ChatGPT 4o mini)
 */
public class ConsoleClient {
	// Service proxy for interacting with the AuctionsService using HTTP-based implementation
	private final IAuctionsServiceProxy serviceProxy = new HttpServiceProxy();	
	// Token to be used during the session
	private String token;
	// Default email and password for login
	private String defaultEmail = "blackwidow@marvel.com";
	private String defaultPassword = "Bl@ckWid0w2023";
	
	private static final Logger logger = LoggerFactory.getLogger(ConsoleClient.class);

	public static void main(String[] args) {
		ConsoleClient client = new ConsoleClient();
		
		if (!client.performLogin() || !client.loadCategories() || !client.loadArticlesAndPlaceBid()) {
			logger.info("Exiting application due to failure in one of the steps.");
		}
	}

	public boolean performLogin() {
		try {
			Credentials credentials = new Credentials(defaultEmail, defaultPassword);

			token = serviceProxy.login(credentials);
			logger.info("Login successful. Token: {}", token);

			return true;
		} catch (RuntimeException e) {
			logger.error("Login failed: {}", e.getMessage());
			
			return false;
		}
	}

	public boolean loadCategories() {
		try {
			List<Category> categories = serviceProxy.getAllCategories();
			
			if (categories == null || categories.isEmpty()) {
				logger.info("No categories found.");
				return false;
			}
			
			categories.forEach(category -> logger.info("Category: {}", category.name()));
		
			return true;
		} catch (RuntimeException e) {
			logger.error("Failed to load categories: {}", e.getMessage());
		}
		
		return false;
	}

	public boolean loadArticlesAndPlaceBid() {
		try {
			List<Category> categories = serviceProxy.getAllCategories();
			String categoryName = categories.get(0).name();
			
			logger.info("Fetching articles for category: {}", categoryName);
			List<Article> articles = serviceProxy.getArticlesByCategory(categoryName, "EUR");

			if (articles.isEmpty()) {
				logger.info("No articles found in category: {}", categoryName);
				return false;
			}

			Article articleDetails = loadArticleDetails(articles.get(0).id());
			
			return articleDetails != null && placeBid(articleDetails);
		} catch (RuntimeException e) {
			logger.error("Failed to fetch articles by category: {}", e.getMessage());
			return false;
		}
	}

	public Article loadArticleDetails(Long articleId) {
		try {
			Article article = serviceProxy.getArticleDetails(articleId, "EUR");
			logger.info("Article Details - Title: {}, Current Price: {} {}, Bids: {}", article.title(), article.currentPrice(), article.currency(), article.bids());
			
			return article;
		} catch (RuntimeException e) {
			logger.error("Failed to load article details: {}", e.getMessage());
			return null;
		}
	}

	public boolean placeBid(Article article) {		
		try {
			Float bidAmount = article.currentPrice() + 1.0f;
			
			serviceProxy.makeBid(article.id(), bidAmount, article.currency(), token);
			logger.info("Bid placed successfully on article ID {} with amount {} {}", article.id(), bidAmount, article.currency());
			
			return true;
		} catch (RuntimeException e) {
			logger.error("Failed to place bid: {}", e.getMessage());
			return false;
		}
	}
}