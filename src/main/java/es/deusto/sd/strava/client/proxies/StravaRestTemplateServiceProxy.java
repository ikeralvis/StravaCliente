package es.deusto.sd.strava.client.proxies;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import es.deusto.sd.strava.client.data.Credentials;
import es.deusto.sd.strava.client.data.Entrenamiento;
import es.deusto.sd.strava.client.data.Reto;
import es.deusto.sd.strava.client.data.Usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StravaRestTemplateServiceProxy implements IStravaServiceProxy {
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(StravaRestTemplateServiceProxy.class);

    @Value("${api.base.url}")
    private String apiBaseUrl;

    public StravaRestTemplateServiceProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String registrar(Usuario usuario) {
        DecimalFormat decimalFormat = new DecimalFormat("0.######", DecimalFormatSymbols.getInstance(Locale.US));
        String peso = decimalFormat.format(usuario.peso());
        String altura = decimalFormat.format(usuario.altura());

        String url = String.format(
                "%s/auth/registroUsuario?email=%s&tipoLogin=%s&nombre=%s&fechaNacimiento=%s&peso=%s&altura=%s&frecuenciaCardiacaMax=%d&frecuenciaCardiacaReposo=%d",
                apiBaseUrl,
                usuario.email(),
                usuario.tipoLogin(),
                usuario.nombre(),
                usuario.fechaNacimiento(),
                peso,
                altura,
                usuario.frecuenciaCardiacaMax(),
                usuario.frecuenciaCardiacaReposo());
        logger.info("-RestTemplate- URL: " + url);
        try {
            return restTemplate.postForObject(url, null, String.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 409:
                    logger.info("-RestTemplate-    Usuario ya registrado");
                    throw new RuntimeException("Usuario ya registrado: " + e.getStatusCode().value());
                case 401:
                    logger.info("-RestTemplate-    La contrasenya o el correo no son correctos");
                    throw new RuntimeException(
                            "La contrasenya o el correo no son correctos: " + e.getStatusCode().value());
                default:
                    throw new RuntimeException("Registro fallido: " + e.getStatusCode().value());
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
                case 401:
                    throw new RuntimeException("Token inválido");
                default:
                    throw new RuntimeException("Logout fallido: " + e.getStatusCode().value());
            }
        }
    }

    @Override
    public String login(Credentials credentials) {
        // Construir la URL con los parámetros
        String url = String.format("%s/auth/login?email=%s&contrasenya=%s",
                apiBaseUrl,
                credentials.email(),
                credentials.password());
        logger.info("-RestTemplate- URL: " + url);
        try {
            logger.info("-RestTemplate-    Procesando login");
            return restTemplate.postForObject(url, null, String.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401:
                    logger.error("-RestTemplate-    Credenciales invalidas");
                    throw new RuntimeException("Credenciales invalidas");
                default:
                    logger.error("-RestTemplate-    Login fallido: " + e.getStatusCode().value());
                    throw new RuntimeException("Login fallido: " + e.getStatusCode().value());
            }
        }
    }

    @Override
    public List<Entrenamiento> consultarEntrenamientos(String token, LocalDate fechaInicio, LocalDate fechaFin) {
        // Formatear las fechas si no son nulas
        String fechaInicioParam = (fechaInicio != null) ? fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : null;
        String fechaFinParam = (fechaFin != null) ? fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null;

        // Construir la URL con String.format en el formato esperado
        String url = String.format(
                "%s/api/entrenamientos/%s/%s?token=%s&fechaInicio=%s&fechaFin=%s",
                apiBaseUrl, // Base URL del servidor
                fechaInicioParam,
                fechaFinParam,
                token,
                fechaInicioParam,
                fechaFinParam);
        logger.info("-RestTemplate- URL: " + url);
        try {
            logger.info("-RestTemplate-    Procesando consulta de entrenamientos");
            List<Entrenamiento> entrenamientos = restTemplate.postForObject(url, null, List.class);
            return entrenamientos;
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401:
                    logger.error("-RestTemplate-    Token inválido");
                    throw new RuntimeException("Token inválido");
                default:
                    logger.error("-RestTemplate-    Consulta de entrenamientos fallida: " + e.getStatusCode().value());
                    throw new RuntimeException("Consulta de entrenamientos fallida: " + e.getStatusCode().value());
            }
        }
    }

    @Override
    public void anadirEntrenamiento(String token, Entrenamiento entrenamiento) {
        // String url = String.format("%s/api/entrenamientos?token=%s", apiBaseUrl,
        // token);
    }

    @Override
    public void anadirReto(String token, Reto reto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anadirReto'");
    }

    @Override
    public String aceptarReto(String nombreReto, String token) {
        String url = String.format("%s/api/retos/%s/aceptar",
                apiBaseUrl,
                nombreReto);
        logger.info("-RestTemplate- URL: " + url);
        try {
            logger.info("-RestTemplate-    Procesando aceptar reto");
            return restTemplate.postForObject(url, token, String.class);

        } catch (HttpStatusCodeException e) {
            logger.error("-RestTemplate- Error al aceptar reto: " + e.getStatusCode());

            switch (e.getStatusCode().value()) {
                case 401:
                    logger.error("-RestTemplate-    Token inválido");
                    throw new RuntimeException("Token inválido");
                default:
                    logger.error("-RestTemplate-    Aceptar reto fallido: " + e.getStatusCode().value());
                    throw new RuntimeException("Aceptar reto fallido: " + e.getStatusCode().value());
            }
        }
    }

    @Override
    public List<Reto> consultarRetos(String token, LocalDate fechaInicio, LocalDate fechaFin, String deporte) {
        // Formatear las fechas si no son nulas
        String fechaInicioParam = (fechaInicio != null) ? fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : null;
        String fechaFinParam = (fechaFin != null) ? fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null;

        // Construir la URL con String.format en el formato esperado
        String url = String.format("%s/api/retos", apiBaseUrl);
        logger.info("-RestTemplate- URL: " + url);
        try {
            logger.info("-RestTemplate-    Procesando consulta de retos");
            List<Reto> retos = restTemplate.getForObject(url, List.class);
            logger.info("-RestTemplate-    Retos: " + retos);
            return retos;
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401:
                    logger.error("-RestTemplate-    Token inválido");
                    throw new RuntimeException("Token inválido");
                default:
                    logger.error("-RestTemplate-    Consulta de retos fallida: " + e.getStatusCode().value());
                    throw new RuntimeException("Consulta de retos fallida: " + e.getStatusCode().value());
            }
        }
    }
}