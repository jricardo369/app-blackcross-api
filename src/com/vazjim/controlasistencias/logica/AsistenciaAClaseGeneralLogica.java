 package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.AsistenciaAClaseGeneral;
import com.vazjim.controlasistencias.utilidades.Utilidades;

/**
 * Logica para consultar de forma unificada las asistencias de un usuario en las
 * tablas {@code asistencia_a_clase} y {@code asistencia_a_clase_hyrox}.
 */
public class AsistenciaAClaseGeneralLogica {

	private static Logger log = Logger.getLogger(AsistenciaAClaseGeneralLogica.class);

	/**
	 * Obtiene todas las asistencias del usuario en clases normales y Hyrox.
	 */
	public List<AsistenciaAClaseGeneral> obtenerAsistenciasUsuario(int idUsuario) throws SQLException {
		return obtenerAsistenciasUsuario(idUsuario, null, null);
	}

	/**
	 * Obtiene las asistencias del usuario en clases normales y Hyrox.
	 *
	 * <p>La consulta une ambas tablas con {@code UNION ALL}. Si {@code fechaInicio} y/o
	 * {@code fechaFin} vienen informados se aplica el rango de fechas sobre {@code a.fecha}.</p>
	 *
	 * @param idUsuario   usuario del que se obtienen las asistencias
	 * @param fechaInicio fecha inicial (inclusive), puede ser {@code null}
	 * @param fechaFin    fecha final (inclusive), puede ser {@code null}
	 * @return lista de asistencias ordenada de la mas reciente a la mas antigua
	 * @throws SQLException
	 */
	public List<AsistenciaAClaseGeneral> obtenerAsistenciasUsuario(int idUsuario, String fechaInicio, String fechaFin)
			throws SQLException {

		List<AsistenciaAClaseGeneral> asistencias = new ArrayList<>();

		if (idUsuario <= 0) {
			log.debug("No se pueden obtener las asistencias, usuario invalido: " + idUsuario);
			return asistencias;
		}

		StringBuilder filtroFechas = new StringBuilder();
		List<String> parametrosFecha = new ArrayList<>();

		if (fechaInicio != null && !fechaInicio.trim().isEmpty()) {
			filtroFechas.append(" AND a.fecha >= ?");
			parametrosFecha.add(fechaInicio.trim());
		}
		if (fechaFin != null && !fechaFin.trim().isEmpty()) {
			filtroFechas.append(" AND a.fecha <= ?");
			parametrosFecha.add(fechaFin.trim());
		}

		StringBuilder querySql = new StringBuilder();
		querySql.append("SELECT 'CROSSFIT' AS tipo, a.id_usuario, a.id_clase, c.nombre, c.horario, ")
				.append("c.hora_inicio, c.hora_fin, c.profesor, p.nombre AS profesor_nombre, ")
				.append("a.fecha, a.lugar, a.asistio, c.estatus ")
				.append("FROM asistencia_a_clase a ")
				.append("JOIN clase c ON c.id_clase = a.id_clase ")
				.append("LEFT JOIN usuario p ON p.id_usuario = c.profesor ")
				.append("WHERE a.id_usuario = ? AND a.fecha >= current_date()").append(filtroFechas)
				.append(" UNION ALL ")
				.append("SELECT 'HYROX' AS tipo, a.id_usuario, a.id_clase_hyrox AS id_clase, c.nombre, c.horario, ")
				.append("c.hora_inicio, c.hora_fin, c.profesor, p.nombre AS profesor_nombre, ")
				.append("a.fecha, a.lugar, a.asistio, c.estatus ")
				.append("FROM asistencia_a_clase_hyrox a ")
				.append("JOIN clase_hyrox c ON c.id_clase_hyrox = a.id_clase_hyrox ")
				.append("LEFT JOIN usuario p ON p.id_usuario = c.profesor ")
				.append("WHERE a.id_usuario = ? AND a.fecha >= current_date()").append(filtroFechas)
				.append(" ORDER BY fecha DESC, hora_inicio DESC");

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement(querySql.toString());

			int index = 1;
			index = bindParametros(st, index, idUsuario, parametrosFecha);
			bindParametros(st, index, idUsuario, parametrosFecha);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				AsistenciaAClaseGeneral asistencia = new AsistenciaAClaseGeneral();

				asistencia.setTipo(rs.getString(1));
				asistencia.setIdUsuario(rs.getInt(2));
				asistencia.setIdClase(rs.getInt(3));
				asistencia.setNombre(rs.getString(4));
				asistencia.setHorario(rs.getString(5) != null ? rs.getString(5) : "");
				asistencia.setHoraInicio(rs.getString(6));
				asistencia.setHoraFin(rs.getString(7));
				asistencia.setProfesor(rs.getString(8));
				asistencia.setProfesorNombre(rs.getString(9));
				asistencia.setFecha(rs.getString(10));
				asistencia.setLugar(rs.getInt(11));
				asistencia.setAsistio(rs.getString(12));
				asistencia.setEstatus(rs.getString(13));
				asistencia.setDescripcionHorario(descripcionHorario(asistencia));

				if (asistencia.getFecha() != null && !asistencia.getFecha().isEmpty()) {
					asistencia.setDia(Utilidades.diaSemana(asistencia.getFecha()));
					asistencia.setDiaDescripcion(Utilidades.fechaEnLetra(asistencia.getFecha()));
				}

				asistencias.add(asistencia);
			}

		} catch (SQLException e) {
			log.error("Error al obtener las asistencias del usuario " + idUsuario, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return asistencias;
	}

	/**
	 * Obtiene las asistencias del usuario en clases normales y Hyrox.
	 *
	 * <p>La consulta une ambas tablas con {@code UNION ALL}. Si {@code fechaInicio} y/o
	 * {@code fechaFin} vienen informados se aplica el rango de fechas sobre {@code a.fecha}.</p>
	 *
	 * @param idUsuario   usuario del que se obtienen las asistencias
	 * @param fechaInicio fecha inicial (inclusive), puede ser {@code null}
	 * @param fechaFin    fecha final (inclusive), puede ser {@code null}
	 * @return lista de asistencias ordenada de la mas reciente a la mas antigua
	 * @throws SQLException
	 */
	public List<AsistenciaAClaseGeneral> obtenerHistoricoAsistenciasUsuario(int idUsuario)
			throws SQLException {

		List<AsistenciaAClaseGeneral> asistencias = new ArrayList<>();

		if (idUsuario <= 0) {
			log.debug("No se pueden obtener las asistencias, usuario invalido: " + idUsuario);
			return asistencias;
		}

		StringBuilder filtroFechas = new StringBuilder();

		// No se aplican filtros de fecha ya que se obtienen todas las asistencias históricas

		StringBuilder querySql = new StringBuilder();
		querySql.append("SELECT 'CROSSFIT' AS tipo, a.id_usuario, a.id_clase, c.nombre, c.horario, ")
				.append("c.hora_inicio, c.hora_fin, c.profesor, p.nombre AS profesor_nombre, ")
				.append("a.fecha, a.lugar, a.asistio, c.estatus ")
				.append("FROM asistencia_a_clase a ")
				.append("JOIN clase c ON c.id_clase = a.id_clase ")
				.append("LEFT JOIN usuario p ON p.id_usuario = c.profesor ")
				.append("WHERE a.id_usuario = ? AND a.fecha < current_date()").append(filtroFechas)
				.append(" UNION ALL ")
				.append("SELECT 'HYROX' AS tipo, a.id_usuario, a.id_clase_hyrox AS id_clase, c.nombre, c.horario, ")
				.append("c.hora_inicio, c.hora_fin, c.profesor, p.nombre AS profesor_nombre, ")
				.append("a.fecha, a.lugar, a.asistio, c.estatus ")
				.append("FROM asistencia_a_clase_hyrox a ")
				.append("JOIN clase_hyrox c ON c.id_clase_hyrox = a.id_clase_hyrox ")
				.append("LEFT JOIN usuario p ON p.id_usuario = c.profesor ")
				.append("WHERE a.id_usuario = ? AND a.fecha < current_date()").append(filtroFechas)
				.append(" ORDER BY fecha DESC, hora_inicio DESC");

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement(querySql.toString());

			int index = 1;
			index = bindParametros(st, index, idUsuario, new ArrayList<>());
			bindParametros(st, index, idUsuario, new ArrayList<>());

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				AsistenciaAClaseGeneral asistencia = new AsistenciaAClaseGeneral();

				asistencia.setTipo(rs.getString(1));
				asistencia.setIdUsuario(rs.getInt(2));
				asistencia.setIdClase(rs.getInt(3));
				asistencia.setNombre(rs.getString(4));
				asistencia.setHorario(rs.getString(5) != null ? rs.getString(5) : "");
				asistencia.setHoraInicio(rs.getString(6));
				asistencia.setHoraFin(rs.getString(7));
				asistencia.setProfesor(rs.getString(8));
				asistencia.setProfesorNombre(rs.getString(9));
				asistencia.setFecha(rs.getString(10));
				asistencia.setLugar(rs.getInt(11));
				asistencia.setAsistio(rs.getString(12));
				asistencia.setEstatus(rs.getString(13));
				asistencia.setDescripcionHorario(descripcionHorario(asistencia));

				if (asistencia.getFecha() != null && !asistencia.getFecha().isEmpty()) {
					asistencia.setDia(Utilidades.diaSemana(asistencia.getFecha()));
					asistencia.setDiaDescripcion(Utilidades.fechaEnLetra(asistencia.getFecha()));
				}

				asistencias.add(asistencia);
			}

		} catch (SQLException e) {
			log.error("Error al obtener las asistencias del usuario " + idUsuario, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return asistencias;
	}

	

	/**
	 * Cuenta las asistencias del usuario en ambas tablas.
	 */
	public int contarAsistenciasUsuario(int idUsuario) throws SQLException {

		String querySql = "SELECT (SELECT COUNT(*) FROM asistencia_a_clase WHERE id_usuario = ?) "
				+ "+ (SELECT COUNT(*) FROM asistencia_a_clase_hyrox WHERE id_usuario = ?)";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		int total = 0;

		try {
			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);
			st.setInt(2, idUsuario);

			rs = st.executeQuery();

			if (rs != null && rs.next()) {
				total = rs.getInt(1);
			}

		} catch (SQLException e) {
			log.error("Error al contar las asistencias del usuario " + idUsuario, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return total;
	}

	private int bindParametros(PreparedStatement st, int index, int idUsuario, List<String> parametrosFecha)
			throws SQLException {
		st.setInt(index++, idUsuario);
		for (String parametro : parametrosFecha) {
			st.setString(index++, parametro);
		}
		return index;
	}

	private String descripcionHorario(AsistenciaAClaseGeneral asistencia) {
		String horario = asistencia.getHorario() != null ? asistencia.getHorario() : "";
		String amPm;
		if ("M".equals(horario)) {
			amPm = "AM";
		} else if ("T".equals(horario) || "V".equals(horario)) {
			amPm = "PM";
		} else {
			amPm = horario;
		}
		return "Clase de " + asistencia.getHoraInicio() + " " + amPm + " a " + asistencia.getHoraFin() + " " + amPm;
	}

	private static void cerrar(Connection conn, Statement st, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.debug("Error al cerrar el ResultSet", e);
			}
		}
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				log.debug("Error al cerrar el Statement", e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.debug("Error al cerrar la Connection", e);
			}
		}
	}

}
