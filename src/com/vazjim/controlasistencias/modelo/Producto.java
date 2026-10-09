package com.vazjim.controlasistencias.modelo;

import java.math.BigDecimal;
import java.util.List;

public class Producto {
	
	private int idProducto;
	private String titulo;
	private String descripcionCorta;
	private String descripcionLarga;
	private BigDecimal costo;
	private int idTipo;
	private String fechaVencimiento;
	private String estatus;
	private int idSociedad;
	private int oferta;
	private List<ImagenProducto> imagenes;
	private String[] imagenesArr;
	
	public int getIdProducto() {
		return idProducto;
	}
	public void setIdProducto(int idProducto) {
		this.idProducto = idProducto;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getDescripcionCorta() {
		return descripcionCorta;
	}
	public void setDescripcionCorta(String descripcionCorta) {
		this.descripcionCorta = descripcionCorta;
	}
	public String getDescripcionLarga() {
		return descripcionLarga;
	}
	public void setDescripcionLarga(String descripcionLarga) {
		this.descripcionLarga = descripcionLarga;
	}
	public BigDecimal getCosto() {
		return costo;
	}
	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}
	public int getIdTipo() {
		return idTipo;
	}
	public void setIdTipo(int idTipo) {
		this.idTipo = idTipo;
	}
	public String getFechaVencimiento() {
		return fechaVencimiento;
	}
	public void setFechaVencimiento(String fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}
	public String getEstatus() {
		return estatus;
	}
	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}
	public int getIdSociedad() {
		return idSociedad;
	}
	public void setIdSociedad(int idSociedad) {
		this.idSociedad = idSociedad;
	}
	public int getOferta() {
		return oferta;
	}
	public void setOferta(int oferta) {
		this.oferta = oferta;
	}
	public List<ImagenProducto> getImagenes() {
		return imagenes;
	}
	public void setImagenes(List<ImagenProducto> imagenes) {
		this.imagenes = imagenes;
	}
	public String[] getImagenesArr() {
		return imagenesArr;
	}
	public void setImagenesArr(String[] imagenesArr) {
		this.imagenesArr = imagenesArr;
	}
	
}
