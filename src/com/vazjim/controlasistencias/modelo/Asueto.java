package com.vazjim.controlasistencias.modelo;

public class Asueto {

	private int idAsueto;
	private int idClase;
	private String fecha;
	private int tipo;
	private String claseDescripcion;
	private String fechaDescripcion;
	private int sociedad;
	
	public int getIdAsueto() {
		return idAsueto;
	}
	public void setIdAsueto(int idAsueto) {
		this.idAsueto = idAsueto;
	}
	public int getIdClase() {
		return idClase;
	}
	public void setIdClase(int idClase) {
		this.idClase = idClase;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public int getTipo() {
		return tipo;
	}
	public void setTipo(int tipo) {
		this.tipo = tipo;
	}
	public String getClaseDescripcion() {
		return claseDescripcion;
	}
	public void setClaseDescripcion(String claseDescripcion) {
		this.claseDescripcion = claseDescripcion;
	}
	public String getFechaDescripcion() {
		return fechaDescripcion;
	}
	public void setFechaDescripcion(String fechaDescripcion) {
		this.fechaDescripcion = fechaDescripcion;
	}
	public int getSociedad() {
		return sociedad;
	}
	public void setSociedad(int sociedad) {
		this.sociedad = sociedad;
	}
	
}
