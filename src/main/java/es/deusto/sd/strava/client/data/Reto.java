package es.deusto.sd.strava.client.data;

public record Reto(
		String nombre,
		long fechaInicio,
		long fechaFin,
		float objetivoDistancia,
		int objetivoTiempo,
		String deporte
	) {}