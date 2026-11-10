package com.vazjim.controlasistencias.modelo;

/**
 * Socio con una membresia por vencer o ya agotada.
 *
 * Mapea la consulta `usuariosMembresiasAVencer` de {@link com.vazjim.controlasistencias.logica.UsuarioLogica}.
 * El campo {@code vencimiento} trae el texto listo para mostrar en la aplicacion
 * ("No cuenta con clases", "Le quedan N clases disponibles",
 * "Esta a punto de acabarse su membresia", "Membresia vigente hasta dd/mm/yyyy").
 */
public class MembresiaAVencer {

	private int idUsuario;
	private int idRol;
	private String usuario;
	private String nombre;
	private String sexo;
	private String correoElectronico;
	private String telefono;
	private int intentos;
	private String estatus;
	private String fechaInicio;
	private String fechaCorte;
	private int clases;
	private String tipoInscripcion;
	private String vencimiento;

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public int getIdRol() {
		return idRol;
	}

	public void setIdRol(int idRol) {
		this.idRol = idRol;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	public String getCorreoElectronico() {
		return correoElectronico;
	}

	public void setCorreoElectronico(String correoElectronico) {
		this.correoElectronico = correoElectronico;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public int getIntentos() {
		return intentos;
	}

	public void setIntentos(int intentos) {
		this.intentos = intentos;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}

	public String getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public String getFechaCorte() {
		return fechaCorte;
	}

	public void setFechaCorte(String fechaCorte) {
		this.fechaCorte = fechaCorte;
	}

	public int getClases() {
		return clases;
	}

	public void setClases(int clases) {
		this.clases = clases;
	}

	public String getTipoInscripcion() {
		return tipoInscripcion;
	}

	public void setTipoInscripcion(String tipoInscripcion) {
		this.tipoInscripcion = tipoInscripcion;
	}

	public String getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(String vencimiento) {
		this.vencimiento = vencimiento;
	}

}
