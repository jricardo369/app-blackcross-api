package com.vazjim.controlasistencias.modelo;

public class ImagenProducto {
	
	private int idImagen;
	private String urlImagen;
	private String archivo;
	private int sociedad;
	private int producto;
	
	public int getIdImagen() {
		return idImagen;
	}
	public void setIdImagen(int idImagen) {
		this.idImagen = idImagen;
	}
	public String getUrlImagen() {
		return urlImagen;
	}
	public void setUrlImagen(String urlImagen) {
		this.urlImagen = urlImagen;
	}
	public String getArchivo() {
		return archivo;
	}
	public void setArchivo(String archivo) {
		this.archivo = archivo;
	}
	public int getSociedad() {
		return sociedad;
	}
	public void setSociedad(int sociedad) {
		this.sociedad = sociedad;
	}
	public int getProducto() {
		return producto;
	}
	public void setProducto(int producto) {
		this.producto = producto;
	}

}
