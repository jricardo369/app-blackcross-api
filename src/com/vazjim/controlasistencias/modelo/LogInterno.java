package com.vazjim.controlasistencias.modelo;

public class LogInterno {
	
	private String tipo;
	private String fecha;
	private String accion;
	private String usuario;
	private String datosEntrada;
	private String datosSalida;
	
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getAccion() {
		return accion;
	}
	public void setAccion(String accion) {
		this.accion = accion;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getDatosEntrada() {
		return datosEntrada;
	}
	public void setDatosEntrada(String datosEntrada) {
		this.datosEntrada = datosEntrada;
	}
	public String getDatosSalida() {
		return datosSalida;
	}
	public void setDatosSalida(String datosSalida) {
		this.datosSalida = datosSalida;
	}
	
	public LogInterno(String tipo, String accion, String usuario, String datosEntrada, String datosSalida) {
		super();
		this.tipo = tipo;
		this.accion = accion;
		this.usuario = usuario;
		this.datosEntrada = datosEntrada;
		this.datosSalida = datosSalida;
	}
	
	public LogInterno(){
		super();
	}
	
	
	
}
