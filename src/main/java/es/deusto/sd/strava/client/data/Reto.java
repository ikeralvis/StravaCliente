package es.deusto.sd.strava.client.data;

public record Reto(
		String nombre,
		long fechaInicio,
		long fechaFin,
		double objetivo,
		String tipoObjetivo,
		String deporte
	) {}