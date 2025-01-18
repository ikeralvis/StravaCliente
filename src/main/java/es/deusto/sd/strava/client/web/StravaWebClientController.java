/**
 * This code is based on solutions provided by Claude Sonnet 3.5 and 
 * adapted using GitHub Copilot. It has been thoroughly reviewed 
 * and validated to ensure correctness and that it is free of errors.
 */
package es.deusto.sd.strava.client.web;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.deusto.sd.strava.client.data.Credentials;
import es.deusto.sd.strava.client.data.Entrenamiento;
import es.deusto.sd.strava.client.data.Reto;
import es.deusto.sd.strava.client.data.Usuario;
import es.deusto.sd.strava.client.proxies.IStravaServiceProxy;
import jakarta.servlet.http.HttpServletRequest;

/**
 * WebClientController class serves as the primary controller for the web client
 * application built with Spring Boot. It orchestrates the interactions between
 * the web application and the AuctionsService through the
 * RestTemplateServiceProxy, managing HTTP requests and responses while serving
 * Thymeleaf templates.
 * 
 * The use of the `@Controller` annotation in the WebClientController class
 * signifies that this class serves as a front controller in the Spring MVC
 * architecture. This annotation allows Spring to recognize and manage the class
 * as a web component, enabling it to handle HTTP requests and produce responses
 * based on user interactions.
 * 
 * Spring Boot's `@Controller` facilitates the use of model attributes through
 * the `Model` interface. The `model.addAttribute()` method is used to add
 * attributes to the model, making them accessible in the Thymeleaf templates.
 * This method takes a key-value pair, where the key is the name of the
 * attribute that can be referenced in the template, and the value is the actual
 * data to be passed. For instance, when `model.addAttribute("currentUrl",
 * currentUrl)` is called, the current URL is stored in the model with the key
 * "currentUrl", allowing it to be easily accessed in the corresponding
 * Thymeleaf view. This mechanism enables the dynamic rendering of content based
 * on the application state, ensuring that user interfaces are responsive and
 * adaptable to user interactions.
 * 
 * The methods of the controller return a `String`, which represents the name of
 * the Thymeleaf template to be rendered. This design pattern allows the
 * controller to define the appropriate view for each action. For instance, when
 * the `home` method is called, it returns the string "index", which tells
 * Spring to render the `index.html` Thymeleaf template. The mapping methods not
 * only process data but also dictate the presentation layer, facilitating a
 * clear separation between business logic and user interface concerns.
 * 
 * This class uses two distinct mappings to handle the login process, allowing
 * for a clear separation of responsibilities and improving code organization.
 * 
 * The `@GetMapping("/login")` method is responsible for displaying the login
 * page. This method prepares and returns the view containing the login form,
 * ensuring that users can easily access the interface needed to enter their
 * credentials.
 * 
 * On the other hand, the `@PostMapping("/login")` method handles the submission
 * of the form, processing user input, validating credentials, and managing the
 * authentication logic. This separation allows each method to have a single
 * responsibility, making the code easier to understand and maintain.
 * 
 * (Description generated with ChatGPT 4o mini)
 */
@Controller
public class StravaWebClientController {

	@Autowired
	private IStravaServiceProxy stravaServiceProxy;
	private static final Logger logger = LoggerFactory.getLogger(StravaWebClientController.class);
	private String token; // Stores the session token

	// Add current URL and token to all views
	@ModelAttribute
	public void addAttributes(Model model, HttpServletRequest request) {
		String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).toUriString();
		model.addAttribute("currentUrl", currentUrl); // Makes current URL available in all templates
		model.addAttribute("token", token); // Makes token available in all templates
	}

	@GetMapping("/")
	public String inicio(Model model) {
		return "inicio";
	}

	@GetMapping("/index")
	public String home() {
		return "indexStrava";
	}

	@GetMapping("/registrarStrava")
	public String showRegister(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
			Model model) {
		return "registrarStrava";
	}

	@PostMapping("/registrarStrava")
	public String performRegister(
			@RequestParam("nombre") String nombre,
			@RequestParam("email") String email,
			@RequestParam(name = "peso", required = false) Float peso,
			@RequestParam(name = "altura", required = false) Float altura,
			@RequestParam(name = "fechaNacimiento") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
			@RequestParam(name = "frecuenciaCardiacaMax", required = false) Integer frecuenciaCardiacaMax,
			@RequestParam(name = "frecuenciaCardiacaReposo", required = false) Integer frecuenciaCardiacaReposo,
			@RequestParam("tipoLogin") String tipoLogin,
			RedirectAttributes redirectAttributes,
			Model model) {

		try {
			Usuario usuario = new Usuario(
					nombre,
					email,
					peso,
					altura,
					fechaNacimiento,
					frecuenciaCardiacaMax,
					frecuenciaCardiacaReposo,
					tipoLogin);
			logger.info("-Controller-\tRegistrando usuario: " + usuario.toString());
			stravaServiceProxy.registrar(usuario);

			// Si todo va bien, redirigimos a una página de éxito o a la vista de
			// entrenamiento del usuario
			redirectAttributes.addFlashAttribute("message", "Usuario registrado exitosamente");
			logger.info("-Controller-\tUsuario registrado exitosamente");
			return "indexStrava";

		} catch (RuntimeException e) {
			logger.info("-Controller-    Registro fallido: " + e);
			model.addAttribute("errorMessage", "Registro fallido: " + e.getMessage());
			return "registrarStrava";
		}
	}

	@GetMapping("/login")
	public String showLoginPage(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
			Model model) {
		// Add redirectUrl to the model if needed
		model.addAttribute("redirectUrl", redirectUrl);
		logger.info("-Controller-\tMostrando pagina de login");
		return "loginStrava"; // Return your login template
	}

	@PostMapping("/login")
	public String performLogin(@RequestParam("email") String email, @RequestParam("password") String password,
			@RequestParam(value = "redirectUrl", required = false) String redirectUrl, Model model) {
		Credentials credentials = new Credentials(email, password);

		try {
			logger.info("-Controller-	Las credenciales para hacer login son: email: " + credentials.email()
					+ " password: "
					+ credentials.password());
			String tokenId = stravaServiceProxy.login(credentials);
			logger.info("-Controller-\tEl token de la sesion es: " + tokenId);
			token = tokenId;
			// Redirect to the original page or root if redirectUrl is null
			return "indexStrava";
		} catch (RuntimeException e) {
			model.addAttribute("errorMessage", "Login failed: " + e.getMessage());
			return "loginStrava"; // Return to login page with error message
		}
	}

	@GetMapping("/logout")
	public String performLogout(@RequestParam(value = "redirectUrl", defaultValue = "/") String redirectUrl,
			Model model) {
		try {
			stravaServiceProxy.logout(token);
			token = null; // Clear the token after logout
			model.addAttribute("successMessage", "Logout successful.");
		} catch (RuntimeException e) {
			model.addAttribute("errorMessage", "Logout failed: " + e.getMessage());
		}

		// Redirect to the specified URL after logout
		return "redirect:" + redirectUrl;
	}

	@GetMapping("/entrenamientos")
	public String conseguirEntrenamientosUsuario(
			@RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
			@RequestParam(value = "fechaFin", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin,
			Model model,
			RedirectAttributes redirectAttributes) {

		if (!estaLogeado(token)) {
			logger.warn("-Controller-\tUsuario no logeado, redirigiendo a la página de login");
			return "redirect:/login?redirectUrl=/usuarios/entrenamientos";
		}

		try {
			// Llama al servicio proxy para obtener los entrenamientos del usuario
			List<Entrenamiento> entrenamientos = stravaServiceProxy.consultarEntrenamientos(token, fechaInicio,
					fechaFin);
			logger.info(
					"-Controller-\tEntrenamientos obtenidos: " + entrenamientos.size() + " entrenamientos encontrados");
			model.addAttribute("entrenamientos", entrenamientos);
			// Agrega los entrenamientos al modelo para mostrarlos en la vista
			model.addAttribute("entrenamientos", entrenamientos);
			model.addAttribute("fechaInicio", fechaInicio);
			model.addAttribute("fechaFin", fechaFin);

			return "entrenamientos";
		} catch (RuntimeException e) {
			logger.error("-Controller-\tError al obtener los entrenamientos: " + e.getMessage(), e);
			redirectAttributes.addFlashAttribute("errorMessage",
					"Error al obtener los entrenamientos: " + e.getMessage());
			return "errorPage"; // Redirige a una página de error o a otra apropiada
		}

	}

	@PostMapping("/anadirEntrenamientos")
	public String anadirEntrenamientos(
			@RequestParam("titulo") String titulo,
			@RequestParam("deporte") String deporte,
			@RequestParam("distancia") String distanciaStr,
			@RequestParam("fechaInicio") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
			@RequestParam("horaInicio") String horaInicio,
			@RequestParam("duracion") int duracion,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (!estaLogeado(token)) {
			logger.warn("-Controller-\tUsuario no logeado, redirigiendo a la página de login");
			return "redirect:/login?redirectUrl=/entrenamientos";
		}
		float distancia = 0;
		try {
			distancia = Float.parseFloat(distanciaStr.replace(",", "."));

			logger.info("-Controller-\tCreando entrenamiento: " + titulo + " " + deporte + " " + distancia + " "
					+ duracion + " " + fechaInicio + " " + horaInicio);
			// Llamar al proxy para crear el entrenamiento
			stravaServiceProxy.anadirEntrenamiento(token, titulo, deporte, distancia, duracion, fechaInicio,
					horaInicio);

			redirectAttributes.addFlashAttribute("successMessage", "Entrenamiento creado con éxito");
			return "redirect:/entrenamientos"; // Redirige a la lista de entrenamientos
		} catch (NumberFormatException er) {
			logger.error("-Controller-\tError al crear entrenamiento: " + er.getMessage(), er);
			redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el entrenamiento: " + er.getMessage());
			return "redirect:/entrenamientos";
		} catch (RuntimeException e) {
			logger.error("-Controller-\tError al crear entrenamiento: " + e.getMessage(), e);
			redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el entrenamiento: " + e.getMessage());
			return "redirect:/entrenamientos";
		}
	}

	@PostMapping("/reto")
	public String anadirReto(
			@RequestParam("nombre") String nombre,
			@RequestParam("deporte") String deporte,
			@RequestParam("objetivoDistancia") float objetivoDistancia,
			@RequestParam("objetivoTiempo") int objetivoTiempo,
			@RequestParam("fechaInicio") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
			@RequestParam("fechaFin") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
			Model model,

			RedirectAttributes redirectAttributes) {

		try {
			logger.info("-Controller-\tAgregando reto: " + nombre + " " + deporte + " " + objetivoDistancia + " "
					+ objetivoTiempo + " " + fechaInicio + " " + fechaFin);
			stravaServiceProxy.anadirReto(token, nombre, deporte, objetivoDistancia, objetivoTiempo, fechaInicio,
					fechaFin);
			logger.info("-Controller-\tReto agregado exitosamente");
			// Redirigir con un mensaje de éxito
			redirectAttributes.addFlashAttribute("message", "Reto agregado exitosamente");
			return "redirect:/retos"; // Redirigir a la página del usuario

		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "Hubo un error al agregar el reto");
			return "errorPage"; // Redirigir a la página del usuario
		}
	}

	public boolean estaLogeado(String token) {
		if (token != null) {
			return true;
		}
		return false;
	}

	@GetMapping("/retos")
	public String obtenerRetos(
			@RequestParam(value = "startDate", required = false) LocalDate startDate,
			@RequestParam(value = "endDate", required = false) LocalDate endDate,
			@RequestParam(value = "sport", required = false) String sport,
			Model model,
			RedirectAttributes redirectAttributes) {

		if (!estaLogeado(token))
			return "redirect:/login?redirectUrl=/usuarios/retos";

		try {
			// Llama al servicio proxy para obtener los retos del usuario
			List<Reto> retos = stravaServiceProxy.consultarRetosActivos();
			List<Reto> retosAceptados = stravaServiceProxy.retosAceptados(token);
			// Agrega los retos al modelo para mostrarlos en la vista
			model.addAttribute("retos", retos);
			model.addAttribute("retosAceptados", retosAceptados);

			return "retos";
		} catch (RuntimeException e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage",
					"Error al obtener los retos: " + e.getMessage());
			return "errorPage"; // Redirige a una página de error o a otra apropiada
		}

	}

	@PostMapping("/retos/aceptar")
	public String aceptarReto(
			@RequestParam("retoNombre") String retoNombre,
			Model model,
			RedirectAttributes redirectAttributes) {

		if (!estaLogeado(token)) {
			return "redirect:/login?redirectUrl=/retos";
		}

		try {
			// Llama al método del proxy para aceptar el reto
			stravaServiceProxy.aceptarReto(retoNombre, token);

		} catch (RuntimeException e) {
			e.printStackTrace();
			// Mensaje de error
			redirectAttributes.addFlashAttribute("errorMessage", "Error al aceptar el reto: " + e.getMessage());
		}

		// Redirige de vuelta a la página de retos
		return "redirect:/retos";
	}
}
