/**
 * This code is based on solutions provided by Claude Sonnet 3.5 and 
 * adapted using GitHub Copilot. It has been thoroughly reviewed 
 * and validated to ensure correctness and that it is free of errors.
 */
package es.deusto.sd.strava.client.swing;

import java.util.List;

import es.deusto.sd.strava.client.data.Article;
import es.deusto.sd.strava.client.data.Category;
import es.deusto.sd.strava.client.data.Credentials;
import es.deusto.sd.strava.client.proxies.HttpServiceProxy;
import es.deusto.sd.strava.client.proxies.IAuctionsServiceProxy;

/**
 * SwingClientController class acts as a Controller in the Model-View-Controller 
 * (MVC) architectural pattern, managing the interaction between the SwingClientGUI 
 * (the View) and the IAuctionsServiceProxy (the Model). This class is responsible 
 * for handling user input, communicating with the service layer, and updating 
 * the view accordingly.
 * 
 * The class encapsulates the logic for user authentication (login/logout), 
 * retrieving categories and articles, and placing bids on articles. By utilizing 
 * the IAuctionsServiceProxy interface, the controller can interact with various 
 * implementations of the service proxy, such as HttpServiceProxy or RestTemplateServiceProxy, 
 * without being tightly coupled to any specific implementation. This promotes flexibility 
 * and allows for easier testing and maintenance of the application.
 * 
 * (Description generated with ChatGPT 4o mini)
 */
public class SwingClientController {
	// Service proxy for interacting with the AuctionsService using HTTP-based implementation
	private IAuctionsServiceProxy serviceProxy = new HttpServiceProxy();
	// Token to be used during the session
    private String token;

	public boolean login(String email, String password) {
        try {
            Credentials credentials = new Credentials(email, password);
            token = serviceProxy.login(credentials);
            
            return true;
        } catch (RuntimeException e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public void logout() {
        serviceProxy.logout(token);
    }

    public List<Category> getCategories() {
        return serviceProxy.getAllCategories();
    }

    public List<Article> getArticlesByCategory(String categoryName, String currency) {
        return serviceProxy.getArticlesByCategory(categoryName, currency);
    }

    public Article getArticleDetails(Long articleId, String currency) {
        return serviceProxy.getArticleDetails(articleId, currency);
    }

    public void placeBid(Long articleId, Float amount, String currency) {
        serviceProxy.makeBid(articleId, amount, currency, token);
    }
}