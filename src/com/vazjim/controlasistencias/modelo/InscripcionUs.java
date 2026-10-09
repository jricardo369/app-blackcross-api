package com.vazjim.controlasistencias.modelo;

public class InscripcionUs {
	
	private String mensaje;
	private boolean esPorClases;
	private Inscripcion inscripcion;
	
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	public boolean isEsPorClases() {
		return esPorClases;
	}
	public void setEsPorClases(boolean esPorClases) {
		this.esPorClases = esPorClases;
	}
	public Inscripcion getInscripcion() {
		return inscripcion;
	}
	public void setInscripcion(Inscripcion inscripcion) {
		this.inscripcion = inscripcion;
	}

}
