/**
 * This code is based on solutions provided by Claude Sonnet 3.5 and 
 * adapted using GitHub Copilot. It has been thoroughly reviewed 
 * and validated to ensure correctness and that it is free of errors.
 */
package es.deusto.sd.strava.client.data;

import java.time.LocalDate;


public record Entrenamiento(
	    String titulo,
	    String deporte,
	    float distancia,
		LocalDate fechaInicio,
		String horaInicio,
	    int duracion
	) {}