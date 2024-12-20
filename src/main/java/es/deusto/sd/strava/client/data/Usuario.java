/**
 * This code is based on solutions provided by Claude Sonnet 3.5 and 
 * adapted using GitHub Copilot. It has been thoroughly reviewed 
 * and validated to ensure correctness and that it is free of errors.
 */
package es.deusto.sd.strava.client.data;

import java.time.LocalDate;
import java.util.List;

public record Usuario(
	String nombre,
	String email,
	Float peso,
	Float altura,
	LocalDate fechaNacimiento,
	Integer frecuenciaCardiacaMax,
	Integer frecuenciaCardiacaReposo,
	List<Entrenamiento> entrenamientos,
	List<Reto> retosAceptados,
	String tipoLogin
) {}
