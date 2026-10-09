package com.vazjim.controlasistencias.modelo;

import java.util.List;

public class AsistenciaAClase {
	
	private Clase clase;
	private List<Usuario> usuarios;
	private String fecha;
	private String lugares;
	
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public Clase getClase() {
		return clase;
	}
	public void setClase(Clase clase) {
		this.clase = clase;
	}
	public List<Usuario> getUsuarios() {
		return usuarios;
	}
	public void setUsuarios(List<Usuario> usuarios) {
		this.usuarios = usuarios;
	}
	public String getLugares() {
		return lugares;
	}
	public void setLugares(String lugares) {
		this.lugares = lugares;
	}

}
