package com.vazjim.controlasistencias.modelo;

/**
 * Representa una asistencia unificada a una clase normal o a una clase Hyrox.
 *
 * <p>Se utiliza para devolver en una sola lista las asistencias provenientes de
 * las tablas {@code asistencia_a_clase} y {@code asistencia_a_clase_hyrox}.</p>
 */
public class AsistenciaAClaseGeneral {

	/** Origen de la asistencia: {@code CLASE} o {@code HYROX}. */
	private String tipo;
	private int idUsuario;
	private int idClase;
	private String nombre;
	private String fecha;
	private String horario;
	private String horaInicio;
	private String horaFin;
	private String profesor;
	private String profesorNombre;
	private String estatus;
	private int lugar;
	private String asistio;
	private String descripcionHorario;
	private String dia;
	private String diaDescripcion;

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public int getIdClase() {
		return idClase;
	}

	public void setIdClase(int idClase) {
		this.idClase = idClase;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}

	public String getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}

	public String getHoraFin() {
		return horaFin;
	}

	public void setHoraFin(String horaFin) {
		this.horaFin = horaFin;
	}

	public String getProfesor() {
		return profesor;
	}

	public void setProfesor(String profesor) {
		this.profesor = profesor;
	}

	public String getProfesorNombre() {
		return profesorNombre;
	}

	public void setProfesorNombre(String profesorNombre) {
		this.profesorNombre = profesorNombre;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}

	public int getLugar() {
		return lugar;
	}

	public void setLugar(int lugar) {
		this.lugar = lugar;
	}

	public String getAsistio() {
		return asistio;
	}

	public void setAsistio(String asistio) {
		this.asistio = asistio;
	}

	public String getDescripcionHorario() {
		return descripcionHorario;
	}

	public void setDescripcionHorario(String descripcionHorario) {
		this.descripcionHorario = descripcionHorario;
	}

	public String getDia() {
		return dia;
	}

	public void setDia(String dia) {
		this.dia = dia;
	}

	public String getDiaDescripcion() {
		return diaDescripcion;
	}

	public void setDiaDescripcion(String diaDescripcion) {
		this.diaDescripcion = diaDescripcion;
	}

}
