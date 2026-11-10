package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.ClaseHyrox;
import com.vazjim.controlasistencias.modelo.Mensaje;

public class ClaseHyroxLogica {

	private static Logger log = Logger.getLogger(ClaseHyroxLogica.class);

	public List<ClaseHyrox> obtener(String columna, String valor, int sociedad, boolean activas) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<ClaseHyrox> clases = new ArrayList<>();
		ClaseHyrox clase;

		querySql = "SELECT ch.id_clase_hyrox, ch.id_tipo_clase, ch.nombre, ch.fecha, ch.lugares, ch.wod, ch.hora_inicio, ch.hora_fin, ch.horario, ch.profesor, ch.sociedad, ch.estatus, ch.fecha_creacion, u.nombre, t.descripcion FROM clase_hyrox ch "
				+ "JOIN usuario u ON u.id_usuario = ch.profesor "
				+ "JOIN tipo_clase_hyrox t ON t.id_tipo_clase_hyrox = ch.id_tipo_clase ";

		String where = "";

		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " ch.sociedad = " + sociedad + " ";
		}

		if (activas == true) {
			if (!where.equals(""))
				where += " AND ";
			where += " ch.estatus = '1' ";
		}

		if (columna != null && !"".equals(columna)) {
			if (!where.equals(""))
				where += " AND ";
			where += " " + columna + " = '" + valor + "'";
		}

		if (!where.equals("")) {
			where = " WHERE " + where + " ORDER BY ch.fecha, ch.hora_inicio";
			querySql += where;
		} else {
			querySql += " ORDER BY ch.fecha, ch.hora_inicio";
		}

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();
			String horario = "";

			while (rs != null && rs.next()) {

				clase = new ClaseHyrox();
				clase.setIdClaseHyrox(rs.getInt(1));
				clase.setTipoClase(rs.getInt(2));
				clase.setNombre(rs.getString(3));
				clase.setFecha(rs.getString(4));
				clase.setLugares(rs.getString(5));
				clase.setWod(rs.getString(6));
				clase.setHoraInicio(rs.getString(7));
				clase.setHoraFin(rs.getString(8));
				clase.setHorario(rs.getString(9) != null ? rs.getString(9) : "");
				clase.setProfesor(rs.getString(10));
				clase.setSociedad(rs.getInt(11));
				clase.setEstatus(rs.getString(12));
				clase.setFechaCreacion(rs.getString(13));
				clase.setProfesorNombre(rs.getString(14));
				clase.setDescripcionTipo(rs.getString(15));

				if (clase.getHorario().equals("M")) {
					horario = "AM";
				} else if (clase.getHorario().equals("T")) {
					horario = "PM";
				} else {
					horario = clase.getHorario();
				}

				clase.setDescripcionHorario("Clase de " + clase.getHoraInicio() + " " + horario + " a " + clase.getHoraFin() + " " + horario);

				clases.add(clase);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		return clases;
	}

	public ClaseHyrox obtenerPorId(int idClaseHyrox) throws SQLException {
		ClaseHyrox c = null;
		List<ClaseHyrox> clase = obtener("ch.id_clase_hyrox", String.valueOf(idClaseHyrox), 0, false);
		if (!clase.isEmpty()) {
			c = clase.get(0);
		}
		return c;
	}


	public List<ClaseHyrox> obtenerPorFecha(String fecha, int sociedad) throws SQLException {

		String claseLlena = MensajeLogica.obtenerMensajeDesc("C-CLASE-LLENA", "ES");

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		List<ClaseHyrox> clases = new ArrayList<>();
		ClaseHyrox clase;

		LocalDate dia = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		LocalDate inicioSemana = dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate finSemana = dia.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

		String querySql = "SELECT ch.id_clase_hyrox, ch.id_tipo_clase, ch.nombre AS nombre_clase, ch.fecha, ch.lugares AS lugares_totales, " + 
						"ch.wod, ch.hora_inicio, ch.hora_fin, ch.horario, ch.profesor, ch.sociedad, ch.estatus, ch.fecha_creacion, " + 
						"u.nombre AS nombre_profesor, t.descripcion AS descripcion_tipo_clase, " + 
						"COUNT(ac_total.id_clase_hyrox) AS lugares_ocupados " + 
						"FROM clase_hyrox ch " + 
						"JOIN usuario u ON u.id_usuario = ch.profesor " + 
						"JOIN tipo_clase_hyrox t ON t.id_tipo_clase_hyrox = ch.id_tipo_clase " + 
						"LEFT JOIN asistencia_a_clase_hyrox ac_total ON ac_total.id_clase_hyrox = ch.id_clase_hyrox AND ac_total.fecha = ch.fecha " + 
						"WHERE ch.fecha BETWEEN ? AND ? ";

		if (sociedad != 0) {
			querySql += "AND ch.sociedad = ? ";
		}

		querySql += "GROUP BY ch.id_clase_hyrox, ch.id_tipo_clase, ch.nombre, ch.fecha, ch.lugares, ch.wod, ch.hora_inicio, ch.hora_fin, " + 
					"ch.horario, ch.profesor, ch.sociedad, ch.estatus, ch.fecha_creacion, u.nombre, t.descripcion " + 
					"ORDER BY ch.fecha, ch.hora_inicio";

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, inicioSemana.toString());
			st.setString(2, finSemana.toString());

			if (sociedad != 0) {
				st.setInt(3, sociedad);
			}

			rs = st.executeQuery();
			String horario = "";

			while (rs != null && rs.next()) {

				clase = new ClaseHyrox();
				clase.setIdClaseHyrox(rs.getInt(1));
				clase.setTipoClase(rs.getInt(2));
				clase.setNombre(rs.getString(3));
				clase.setFecha(rs.getString(4));
				clase.setLugares(rs.getString(5));
				clase.setWod(rs.getString(6));
				clase.setHoraInicio(rs.getString(7));
				clase.setHoraFin(rs.getString(8));
				clase.setHorario(rs.getString(9) != null ? rs.getString(9) : "");
				clase.setProfesor(rs.getString(10));
				clase.setSociedad(rs.getInt(11));
				clase.setEstatus(rs.getString(12));
				clase.setFechaCreacion(rs.getString(13));
				clase.setProfesorNombre(rs.getString(14));
				clase.setDescripcionTipo(rs.getString(15));

				if (clase.getHorario().equals("M")) {
					horario = "AM";
				} else if (clase.getHorario().equals("T")) {
					horario = "PM";
				} else {
					horario = clase.getHorario();
				}

				clase.setDescripcionHorario("Clase de " + clase.getHoraInicio() + " " + horario + " a " + clase.getHoraFin() + " " + horario);

				String lugaresOcupados = rs.getString(16);

				if (lugaresOcupados.equals(clase.getLugares())) {
					clase.setLugaresDisponibles(claseLlena);
				} else {
					if (lugaresOcupados.equals("")) {
						lugaresOcupados = "0";
					}
					clase.setLugaresDisponibles(lugaresOcupados + " / " + clase.getLugares());
				}

				clases.add(clase);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		return clases;
	}

	public List<ClaseHyrox> obtenerPorFechaParaUsuario(String fecha, int idUsuario, int sociedad) throws SQLException {

		String claseLlena = MensajeLogica.obtenerMensajeDesc("C-CLASE-LLENA", "ES");

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		List<ClaseHyrox> clases = new ArrayList<>();
		ClaseHyrox clase;

		LocalDate dia = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		LocalDate inicioSemana = dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate finSemana = dia.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

		String querySql = "SELECT ch.id_clase_hyrox, ch.id_tipo_clase, ch.nombre AS nombre_clase, ch.fecha, ch.lugares AS lugares_totales, "+
						"ch.wod, ch.hora_inicio, ch.hora_fin, ch.horario, ch.profesor, ch.sociedad, ch.estatus, ch.fecha_creacion, "+
						"u.nombre AS nombre_profesor, t.descripcion AS descripcion_tipo_clase, "+
						"COUNT(ac_total.id_clase_hyrox) AS lugares_ocupados, "+
						"MAX(CASE WHEN ac_usuario.id_usuario = ? THEN 1 ELSE 0 END) AS usuario_asistira, "+
						"MAX(ac_usuario.lugar) AS lugar_usuario "+
						"FROM clase_hyrox ch "+
						"JOIN usuario u ON u.id_usuario = ch.profesor "+
						"JOIN tipo_clase_hyrox t ON t.id_tipo_clase_hyrox = ch.id_tipo_clase "+
						"LEFT JOIN asistencia_a_clase_hyrox ac_total ON ac_total.id_clase_hyrox = ch.id_clase_hyrox AND ac_total.fecha = ch.fecha "+
						"LEFT JOIN asistencia_a_clase_hyrox ac_usuario ON ac_usuario.id_clase_hyrox = ch.id_clase_hyrox AND ac_usuario.fecha = ch.fecha AND ac_usuario.id_usuario = ? "+
						"WHERE ch.fecha BETWEEN ? AND ? ";

		if (sociedad != 0) {
			querySql += "AND ch.sociedad = ? ";
		}

		querySql += "GROUP BY ch.id_clase_hyrox, ch.id_tipo_clase, ch.nombre, ch.fecha, ch.lugares, ch.wod, ch.hora_inicio, ch.hora_fin, "+
					"ch.horario, ch.profesor, ch.sociedad, ch.estatus, ch.fecha_creacion, u.nombre, t.descripcion " +
					"ORDER BY ch.fecha, ch.hora_inicio";

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);
			st.setInt(2, idUsuario);
			st.setString(3, inicioSemana.toString());
			st.setString(4, finSemana.toString());

			if (sociedad != 0) {
				st.setInt(5, sociedad);
			}

			rs = st.executeQuery();
			String horario = "";

			while (rs != null && rs.next()) {

				clase = new ClaseHyrox();
				clase.setIdClaseHyrox(rs.getInt(1));
				clase.setTipoClase(rs.getInt(2));
				clase.setNombre(rs.getString(3));
				clase.setFecha(rs.getString(4));
				clase.setLugares(rs.getString(5));
				clase.setWod(rs.getString(6));
				clase.setHoraInicio(rs.getString(7));
				clase.setHoraFin(rs.getString(8));
				clase.setHorario(rs.getString(9) != null ? rs.getString(9) : "");
				clase.setProfesor(rs.getString(10));
				clase.setSociedad(rs.getInt(11));
				clase.setEstatus(rs.getString(12));
				clase.setFechaCreacion(rs.getString(13));
				clase.setProfesorNombre(rs.getString(14));
				clase.setDescripcionTipo(rs.getString(15));

				if (clase.getHorario().equals("M")) {
					horario = "AM";
				} else if (clase.getHorario().equals("T")) {
					horario = "PM";
				} else {
					horario = clase.getHorario();
				}

				clase.setDescripcionHorario("Clase de " + clase.getHoraInicio() + " " + horario + " a " + clase.getHoraFin() + " " + horario);

				String lugaresOcupados = rs.getString(16);

				if (lugaresOcupados.equals(clase.getLugares())) {
					clase.setLugaresDisponibles(claseLlena);
				} else {
					if (lugaresOcupados.equals("")) {
						lugaresOcupados = "0";
					}
					clase.setLugaresDisponibles(lugaresOcupados + " / " + clase.getLugares());
				}

				if (idUsuario != 0) {
					boolean estaEnClase = rs.getBoolean(17);
					if (estaEnClase) {
						//clase.setLugar(estaEnClase.getLugar());
						clase.setAsistencia("true");
					} else {
						clase.setAsistencia("false");
					}

				}

				clases.add(clase);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		return clases;
	}


	public String insertar(ClaseHyrox clase) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		if (clase.getNombre() == null || clase.getNombre().trim().isEmpty()) {
			throw new SQLException("El campo 'nombre' es obligatorio y no puede estar vacío");
		}

		querySql = "INSERT INTO clase_hyrox(id_tipo_clase,nombre,fecha,lugares,wod,hora_inicio,hora_fin,horario,profesor,sociedad,estatus,fecha_creacion) VALUES(?,?,?,?,?,?,?,?,?,?,?,CURDATE());";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, clase.getTipoClase());
			st.setString(2, clase.getNombre());
			st.setString(3, clase.getFecha());
			st.setString(4, clase.getLugares());
			st.setString(5, clase.getWod());
			st.setString(6, clase.getHoraInicio());
			st.setString(7, clase.getHoraFin());
			st.setString(8, clase.getHorario());
			st.setString(9, clase.getProfesor());
			st.setInt(10, clase.getSociedad());
			st.setString(11, clase.getEstatus() != null ? clase.getEstatus() : "1");
			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("CLASES-CREAR", "ES");
		return m.getMensaje();
	}

	public String actualizar(ClaseHyrox clase) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE clase_hyrox SET id_tipo_clase = ?, nombre = ?, fecha = ?, lugares = ?, wod = ?, hora_inicio = ?, hora_fin = ?, horario = ?, profesor = ?, sociedad = ?, estatus = ? WHERE id_clase_hyrox = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, clase.getTipoClase());
			st.setString(2, clase.getNombre());
			st.setString(3, clase.getFecha());
			st.setString(4, clase.getLugares());
			st.setString(5, clase.getWod());
			st.setString(6, clase.getHoraInicio());
			st.setString(7, clase.getHoraFin());
			st.setString(8, clase.getHorario());
			st.setString(9, clase.getProfesor());
			st.setInt(10, clase.getSociedad());
			st.setString(11, clase.getEstatus());
			st.setInt(12, clase.getIdClaseHyrox());

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("CLASES-ACTUALIZAR", "ES");
		return m.getMensaje();
	}

	public String eliminar(int idClaseHyrox) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM clase_hyrox WHERE id_clase_hyrox = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idClaseHyrox);

			st.execute();
			conn.commit();

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}

		Mensaje m = MensajeLogica.obtenerMensaje("CLASES-ELIMINAR", "ES");
		return m.getMensaje();
	}

	public boolean validarDuplicidad(String fecha, String horaInicio, String horaFin, int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";
		int count = 0;
		boolean salida = false;

		querySql = "SELECT id_clase_hyrox FROM clase_hyrox WHERE fecha = ? and hora_inicio = ? and hora_fin = ? and sociedad = ?";

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setString(2, horaInicio);
			st.setString(3, horaFin);
			st.setInt(4, sociedad);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				count++;
			}

			if (count > 0) {
				salida = true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		return salida;
	}

}
