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

@Service
public class StravaRestTemplateServiceProxy implements IStravaServiceProxy {
    private final RestTemplate restTemplate;

    @Value("${api.base.url}")
    private String apiBaseUrl;

    public StravaRestTemplateServiceProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void registrar(Usuario usuario) {
        String url = String.format("%s/registroUsuario?nombre=%s&fechaNacimiento=%s&peso=%f&altura=%f&frecuenciaCardiacaMax=%d&frecuenciaCardiacaReposo=%d", 
                               apiBaseUrl, 
                               usuario.nombre(), 
                               usuario.fechaNacimiento(), 
                               usuario.peso(), 
                               usuario.altura(), 
                               usuario.frecuenciaCardiacaMax(), 
                               usuario.frecuenciaCardiacaReposo());
        
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
    public TokenPorID login(Credentials credentials) {
        // Construir la URL con los parámetros
        String url = String.format("%s/auth/login?email=%s&contrasenya=%s",
                apiBaseUrl,
                credentials.email(),
                credentials.password());
        try {
            return restTemplate.postForObject(url, null, TokenPorID.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode().value()) {
                case 401:
                    throw new RuntimeException("Credenciales inválidas");
                default:
                    throw new RuntimeException("Login fallido: " + e.getStatusCode().value());
            }
        }

    }
}