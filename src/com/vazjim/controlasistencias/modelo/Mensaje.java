package com.vazjim.controlasistencias.modelo;

public class Mensaje {

	private String idMensaje;
	private String codigo;
	private String tipo;
	private String mensaje;
	private String descripcion;
	private String idioma;
	private int codigoEstatus;
	
	public String getIdMensaje() {
		return idMensaje;
	}
	public void setIdMensaje(String idMensaje) {
		this.idMensaje = idMensaje;
	}
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getIdioma() {
		return idioma;
	}
	public void setIdioma(String idioma) {
		this.idioma = idioma;
	}
	public int getCodigoEstatus() {
		return codigoEstatus;
	}
	public void setCodigoEstatus(int codigoEstatus) {
		this.codigoEstatus = codigoEstatus;
	}
	
}

