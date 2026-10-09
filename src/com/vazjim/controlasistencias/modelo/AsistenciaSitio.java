package com.vazjim.controlasistencias.modelo;

public class AsistenciaSitio {
	
	private int idAsistenciaSitio;
	private String fechaAlta;
	private String nombreUsuario;
	private int idUsuario;
	
	public int getIdAsistenciaSitio() {
		return idAsistenciaSitio;
	}
	public void setIdAsistenciaSitio(int idAsistenciaSitio) {
		this.idAsistenciaSitio = idAsistenciaSitio;
	}
	public String getFechaAlta() {
		return fechaAlta;
	}
	public void setFechaAlta(String fechaAlta) {
		this.fechaAlta = fechaAlta;
	}
	public String getNombreUsuario() {
		return nombreUsuario;
	}
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}
	public int getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}	

}
