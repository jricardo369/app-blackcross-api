package com.vazjim.controlasistencias.modelo;

import java.util.List;

public class AsistenciaAClaseHyrox {
	
	private ClaseHyrox clase;
	private List<Usuario> usuarios;
	private String fecha;
	private String lugares;
	
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public ClaseHyrox getClase() {
		return clase;
	}
	public void setClase(ClaseHyrox clase) {
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