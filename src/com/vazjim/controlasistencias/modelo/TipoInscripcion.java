package com.vazjim.controlasistencias.modelo;

import java.math.BigDecimal;

public class TipoInscripcion {
	
	private int id;
	private String tipo_inscripcion;
	private int valor;
	private BigDecimal monto;
	private int anio;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTipo_inscripcion() {
		return tipo_inscripcion;
	}
	public void setTipo_inscripcion(String tipo_inscripcion) {
		this.tipo_inscripcion = tipo_inscripcion;
	}
	public int getValor() {
		return valor;
	}
	public void setValor(int valor) {
		this.valor = valor;
	}
	public BigDecimal getMonto() {
		return monto;
	}
	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}
	public int getAnio() {
		return anio;
	}
	public void setAnio(int anio) {
		this.anio = anio;
	}
	
	

}
