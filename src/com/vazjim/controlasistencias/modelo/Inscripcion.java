package com.vazjim.controlasistencias.modelo;

import java.math.BigDecimal;

public class Inscripcion {

	private int idInscripcion;
	private int idUsuario;
	private String nombreUsuario;
	private int tipoPago;
	private String descripcionTipoPago;
	private int tipoInscripcion;
	private String tipoInscripcionDesc;
	private String comentario;
	private String fechaAlta;
	private String fechaInicio;
	private int clases;
	private int clases_restantes;
	private BigDecimal monto;
	private String fechaCorte;
	private String descripcion;
	private int sociedad;
	private String vencimiento;
	
	public int getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}
	public String getNombreUsuario() {
		return nombreUsuario;
	}
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}
	public int getTipoPago() {
		return tipoPago;
	}
	public void setTipoPago(int tipoPago) {
		this.tipoPago = tipoPago;
	}
	public String getDescripcionTipoPago() {
		return descripcionTipoPago;
	}
	public void setDescripcionTipoPago(String descripcionTipoPago) {
		this.descripcionTipoPago = descripcionTipoPago;
	}
	public int getTipoInscripcion() {
		return tipoInscripcion;
	}
	public void setTipoInscripcion(int tipoInscripcion) {
		this.tipoInscripcion = tipoInscripcion;
	}
	public String getTipoInscripcionDesc() {
		return tipoInscripcionDesc;
	}
	public void setTipoInscripcionDesc(String tipoInscripcionDesc) {
		this.tipoInscripcionDesc = tipoInscripcionDesc;
	}
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	public BigDecimal getMonto() {
		return monto;
	}
	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}
	public String getFechaAlta() {
		return fechaAlta;
	}
	public void setFechaAlta(String fechaAlta) {
		this.fechaAlta = fechaAlta;
	}
	public String getFechaInicio() {
		return fechaInicio;
	}
	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}
	public int getClases() {
		return clases;
	}
	public void setClases(int clases) {
		this.clases = clases;
	}
	public int getClases_restantes() {
		return clases_restantes;
	}
	public void setClases_restantes(int clases_restantes) {
		this.clases_restantes = clases_restantes;
	}
	public int getIdInscripcion() {
		return idInscripcion;
	}
	public void setIdInscripcion(int idInscripcion) {
		this.idInscripcion = idInscripcion;
	}
	public String getFechaCorte() {
		return fechaCorte;
	}
	public void setFechaCorte(String fechaCorte) {
		this.fechaCorte = fechaCorte;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public int getSociedad() {
		return sociedad;
	}
	public void setSociedad(int sociedad) {
		this.sociedad = sociedad;
	}
	public String getVencimiento() {
		return vencimiento;
	}
	public void setVencimiento(String vencimiento) {
		this.vencimiento = vencimiento;
	}
	
	
	
	
}
