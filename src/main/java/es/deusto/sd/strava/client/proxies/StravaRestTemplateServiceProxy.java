package es.deusto.sd.strava.client.proxies;

import org.springframework.http.HttpHeaders;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import es.deusto.sd.strava.client.data.Credentials;
import es.deusto.sd.strava.client.data.Entrenamiento;
import es.deusto.sd.strava.client.data.Reto;
import es.deusto.sd.strava.client.data.TokenPorID;
import es.deusto.sd.strava.client.data.Usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
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
    public void registrar(Usuario usuario) {
        String url = String.format("%s/auth/registroUsuario?nombre=%s&fechaNacimiento=%s&peso=%f&altura=%f&frecuenciaCardiacaMax=%d&frecuenciaCardiacaReposo=%d", 
                               apiBaseUrl, 
                               usuario.nombre(), 
                               usuario.fechaNacimiento(), 
                               usuario.peso(), 
                               usuario.altura(), 
                               usuario.frecuenciaCardiacaMax(), 
                               usuario.frecuenciaCardiacaReposo());
        logger.info("-RestTemplate- URL: " + url);
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 409:
                    throw new RuntimeException("Usuario ya registrado");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'consultarEntrenamientos'");
    }

    @Override
    public void anadirEntrenamiento(String token, Entrenamiento entrenamiento) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anadirEntrenamiento'");
    }

    @Override
    public void anadirReto(String token, Reto reto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anadirReto'");
    }
}