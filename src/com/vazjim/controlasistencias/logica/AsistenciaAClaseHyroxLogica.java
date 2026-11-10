package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.AsistenciaAClaseHyrox;
import com.vazjim.controlasistencias.modelo.ClaseHyrox;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.modelo.InscripcionUs;
import com.vazjim.controlasistencias.modelo.LugaresClase;
import com.vazjim.controlasistencias.modelo.LugaresClaseSeccionado;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

/**
 * Logica de asistencia para las clases de tipo Hyrox.
 *
 * <p>Se basa en {@link AsistenciaAClaseLogica} pero opera sobre las tablas
 * {@code asistencia_a_clase_hyrox} y {@code clase_hyrox}.</p>
 *
 * <p>Mejoras aplicadas respecto a la clase base:
 * <ul>
 * <li>Todas las consultas usan {@link PreparedStatement} con parametros (sin concatenar valores,
 * evitando inyeccion SQL).</li>
 * <li>La liberacion de recursos se centraliza en {@link #cerrar(Connection, Statement, ResultSet)}.</li>
 * <li>En las operaciones de escritura se hace {@code rollback} si ocurre un error.</li>
 * <li>La actualizacion de asistencia valida el nombre de la columna contra una lista blanca.</li>
 * <li>Se evitan {@code NullPointerException} controlando valores nulos (horario, lugares, etc.).</li>
 * </ul>
 * </p>
 */
public class AsistenciaAClaseHyroxLogica {

	private static Logger log = Logger.getLogger(AsistenciaAClaseHyroxLogica.class);

	/** Columnas que se pueden actualizar por medio de {@link #actualizarAsistenciaHyrox}. */
	private static final Set<String> COLUMNAS_ACTUALIZABLES = new HashSet<>(
			Arrays.asList("lugar", "asistio", "id_usuario", "id_clase_hyrox", "fecha"));

	/* ------------------------------------------------------------------ */
	/* Consultas                                                          */
	/* ------------------------------------------------------------------ */

	/**
	 * Obtiene la asistencia registrada de una clase Hyrox para una fecha.
	 */
	public AsistenciaAClaseHyrox obtenerAsistenciaAClaseHyrox(int idClaseHyrox, String fecha, int sociedad)
			throws SQLException {

		AsistenciaAClaseHyrox asistenciaClase = new AsistenciaAClaseHyrox();
		List<Usuario> usuarios = new ArrayList<>();

		StringBuilder querySql = new StringBuilder();
		querySql.append("SELECT u.id_usuario, u.usuario, u.nombre, c.id_clase_hyrox, c.id_tipo_clase, c.nombre, ")
				.append("c.fecha, c.lugares, c.wod, c.hora_inicio, c.hora_fin, c.horario, c.profesor, ")
				.append("c.sociedad, c.estatus, c.fecha_creacion, u.sexo, a.lugar, a.asistio ")
				.append("FROM asistencia_a_clase_hyrox a ")
				.append("JOIN usuario u ON u.id_usuario = a.id_usuario ")
				.append("JOIN clase_hyrox c ON c.id_clase_hyrox = a.id_clase_hyrox ")
				.append("WHERE a.id_clase_hyrox = ? AND a.fecha = ? ");

		if (sociedad != 0) {
			querySql.append("AND c.sociedad = ? ");
		}

		querySql.append("ORDER BY a.lugar ASC");

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		ClaseHyrox clase = null;
		String lugaresClase = "0";

		try {
			st = conn.prepareStatement(querySql.toString());

			int index = 1;
			st.setInt(index++, idClaseHyrox);
			st.setString(index++, fecha);
			if (sociedad != 0) {
				st.setInt(index++, sociedad);
			}

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				if (clase == null) {
					clase = mapearClase(rs, 4);
					clase.setFecha(fecha);
					lugaresClase = clase.getLugares() != null ? clase.getLugares() : "0";

					asistenciaClase.setFecha(fecha);
					asistenciaClase.setClase(clase);
				}

				Usuario usuario = new Usuario();
				usuario.setIdUsuario(rs.getInt(1));
				usuario.setUsuario(rs.getString(2));
				usuario.setNombre(rs.getString(3));
				usuario.setSexo(rs.getString(17));
				usuario.setIniciales(Utilidades.inicialesNombre(rs.getString(3)));
				usuario.setLugar(rs.getInt(18));
				usuario.setAsistio(rs.getString(19));

				usuarios.add(usuario);
			}

			asistenciaClase.setLugares("Lugares " + usuarios.size() + " / " + lugaresClase);
			asistenciaClase.setUsuarios(usuarios);

		} catch (SQLException e) {
			log.error("Error al obtener la asistencia de la clase Hyrox " + idClaseHyrox, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return asistenciaClase;
	}

	/**
	 * Obtiene la asistencia de una clase Hyrox validando que pertenezca al entrenador indicado.
	 */
	public AsistenciaAClaseHyrox obtenerAsistenciaAClaseHyroxProfesor(int idClaseHyrox, String fecha, int idEntrenador,
			int sociedad) throws SQLException {

		AsistenciaAClaseHyrox asistenciaClase = new AsistenciaAClaseHyrox();
		List<Usuario> usuarios = new ArrayList<>();

		StringBuilder querySql = new StringBuilder();
		querySql.append("SELECT u.id_usuario, u.usuario, u.nombre, c.id_clase_hyrox, c.id_tipo_clase, c.nombre, ")
				.append("c.fecha, c.lugares, c.wod, c.hora_inicio, c.hora_fin, c.horario, c.profesor, ")
				.append("c.sociedad, c.estatus, c.fecha_creacion, u.sexo, a.lugar, a.asistio ")
				.append("FROM asistencia_a_clase_hyrox a ")
				.append("JOIN usuario u ON u.id_usuario = a.id_usuario ")
				.append("JOIN clase_hyrox c ON c.id_clase_hyrox = a.id_clase_hyrox ")
				.append("WHERE a.id_clase_hyrox = ? AND a.fecha = ? AND c.profesor = ? ");

		if (sociedad != 0) {
			querySql.append("AND c.sociedad = ? ");
		}

		querySql.append("ORDER BY a.lugar ASC");

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		ClaseHyrox clase = null;
		String lugaresClase = "0";

		try {
			st = conn.prepareStatement(querySql.toString());

			int index = 1;
			st.setInt(index++, idClaseHyrox);
			st.setString(index++, fecha);
			st.setInt(index++, idEntrenador);
			if (sociedad != 0) {
				st.setInt(index++, sociedad);
			}

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				if (clase == null) {
					clase = mapearClase(rs, 4);
					clase.setFecha(fecha);
					lugaresClase = clase.getLugares() != null ? clase.getLugares() : "0";

					asistenciaClase.setFecha(fecha);
					asistenciaClase.setClase(clase);
				}

				Usuario usuario = new Usuario();
				usuario.setIdUsuario(rs.getInt(1));
				usuario.setUsuario(rs.getString(2));
				usuario.setNombre(rs.getString(3));
				usuario.setSexo(rs.getString(17));
				usuario.setIniciales(Utilidades.inicialesNombre(rs.getString(3)));
				usuario.setLugar(rs.getInt(18));
				usuario.setAsistio(rs.getString(19));

				usuarios.add(usuario);
			}

			asistenciaClase.setLugares("Lugares " + usuarios.size() + " / " + lugaresClase);
			asistenciaClase.setUsuarios(usuarios);

		} catch (SQLException e) {
			log.error("Error al obtener la asistencia de la clase Hyrox " + idClaseHyrox
					+ " del entrenador " + idEntrenador, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return asistenciaClase;
	}

	

	/**
	 * Cuenta las personas registradas en una clase Hyrox para una fecha.
	 */
	public int obtenerNumeroDePersonasDeClaseHyrox(int idClaseHyrox, String fecha) throws SQLException {

		String querySql = "SELECT COUNT(*) FROM asistencia_a_clase_hyrox WHERE fecha = ? AND id_clase_hyrox = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		int count = 0;

		try {
			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, idClaseHyrox);

			rs = st.executeQuery();

			if (rs != null && rs.next()) {
				count = rs.getInt(1);
			}

		} catch (SQLException e) {
			log.error("Error al contar las personas de la clase Hyrox " + idClaseHyrox, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return count;
	}

	/**
	 * Indica si el usuario ya esta registrado en la clase Hyrox indicada para la fecha.
	 */
	public boolean estaEnClaseHyrox(int idClaseHyrox, int idUsuario, String fecha) throws SQLException {

		String querySql = "SELECT COUNT(*) FROM asistencia_a_clase_hyrox "
				+ "WHERE fecha = ? AND id_clase_hyrox = ? AND id_usuario = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		boolean salida = false;

		try {
			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, idClaseHyrox);
			st.setInt(3, idUsuario);

			rs = st.executeQuery();

			if (rs != null && rs.next()) {
				salida = rs.getInt(1) > 0;
			}

		} catch (SQLException e) {
			log.error("Error al validar si el usuario " + idUsuario + " esta en la clase Hyrox " + idClaseHyrox, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return salida;
	}

	/**
	 * Devuelve la clase Hyrox en la que el usuario tiene registro para la fecha, o {@code null}.
	 */
	public ClaseHyrox estaEnClaseEnFechaHyrox(String fecha, int idUsuario) throws SQLException {

		String querySql = "SELECT a.id_clase_hyrox, c.nombre FROM asistencia_a_clase_hyrox a "
				+ "JOIN clase_hyrox c ON a.id_clase_hyrox = c.id_clase_hyrox "
				+ "WHERE a.fecha = ? AND a.id_usuario = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		ClaseHyrox salida = null;

		try {
			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, idUsuario);

			rs = st.executeQuery();

			if (rs != null && rs.next()) {
				salida = new ClaseHyrox();
				salida.setIdClaseHyrox(rs.getInt(1));
				salida.setNombre(rs.getString(2));
			}

		} catch (SQLException e) {
			log.error("Error al buscar la clase Hyrox del usuario " + idUsuario + " en la fecha " + fecha, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return salida;
	}

	/**
	 * Indica si un lugar ya esta ocupado en una clase Hyrox para una fecha.
	 */
	public boolean lugarOcupadoHyrox(String fecha, String lugar, int idClaseHyrox) throws SQLException {

		String querySql = "SELECT id_usuario FROM asistencia_a_clase_hyrox "
				+ "WHERE fecha = ? AND lugar = ? AND id_clase_hyrox = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		boolean salida = false;

		try {
			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setString(2, lugar);
			st.setInt(3, idClaseHyrox);

			rs = st.executeQuery();

			if (rs != null && rs.next()) {
				salida = true;
			}

		} catch (SQLException e) {
			log.error("Error al validar el lugar " + lugar + " de la clase Hyrox " + idClaseHyrox, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return salida;
	}

	/**
	 * Devuelve el nombre de la clase Hyrox si el usuario esta registrado en ella, cadena vacia en caso contrario.
	 */
	public String estaEnClasePorFechaUsuarioClaseHyrox(String fecha, int idUsuario, int idClaseHyrox)
			throws SQLException {

		String querySql = "SELECT c.nombre FROM asistencia_a_clase_hyrox a "
				+ "JOIN clase_hyrox c ON a.id_clase_hyrox = c.id_clase_hyrox "
				+ "WHERE a.fecha = ? AND a.id_usuario = ? AND a.id_clase_hyrox = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		String salida = "";

		try {
			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, idUsuario);
			st.setInt(3, idClaseHyrox);

			rs = st.executeQuery();

			if (rs != null && rs.next()) {
				salida = rs.getString(1);
			}

		} catch (SQLException e) {
			log.error("Error al buscar la clase Hyrox " + idClaseHyrox + " del usuario " + idUsuario, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		log.debug("Esta el usuario en clase Hyrox? " + salida);
		return salida;
	}

	/**
	 * Obtiene las proximas clases Hyrox (fechas futuras) en las que el usuario tiene registrada su asistencia.
	 */
	public List<ClaseHyrox> obtenerProximasClasesHyroxUsuario(Usuario usuario) throws SQLException {

		List<ClaseHyrox> clases = new ArrayList<>();

		if (usuario == null || usuario.getIdUsuario() <= 0) {
			log.debug("No se puede obtener las proximas clases Hyrox, usuario invalido");
			return clases;
		}

		String querySql = "SELECT a.id_clase_hyrox, a.fecha, c.id_tipo_clase, c.nombre, c.lugares, c.wod, "
				+ "c.hora_inicio, c.hora_fin, c.horario, c.profesor, c.sociedad, c.estatus, c.fecha_creacion, p.nombre "
				+ "FROM asistencia_a_clase_hyrox a "
				+ "JOIN clase_hyrox c ON c.id_clase_hyrox = a.id_clase_hyrox "
				+ "LEFT JOIN usuario p ON p.id_usuario = c.profesor "
				+ "WHERE a.id_usuario = ? AND a.fecha > CURDATE() "
				+ "ORDER BY a.fecha ASC, c.hora_inicio ASC, c.horario ASC";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement(querySql);
			st.setInt(1, usuario.getIdUsuario());
			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				ClaseHyrox clase = new ClaseHyrox();
				clase.setIdClaseHyrox(rs.getInt(1));
				clase.setFecha(rs.getString(2));
				clase.setTipoClase(rs.getInt(3));
				clase.setNombre(rs.getString(4));
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
				clase.setDescripcionHorario(descripcionHorario(clase));

				clases.add(clase);
			}

		} catch (SQLException e) {
			log.error("Error al obtener las proximas clases Hyrox del usuario " + usuario.getIdUsuario(), e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return clases;
	}

	/* ------------------------------------------------------------------ */
	/* Escrituras                                                         */
	/* ------------------------------------------------------------------ */

	/**
	 * Agrega el registro de asistencia de un usuario a una clase Hyrox.
	 */
	public String agregarUsuarioAClaseHyrox(int idUsuario, int idClaseHyrox, String fecha, String lugar)
			throws SQLException {

		String querySql = "INSERT INTO asistencia_a_clase_hyrox(id_usuario,id_clase_hyrox,fecha,lugar) VALUES(?,?,?,?)";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, idUsuario);
			st.setInt(2, idClaseHyrox);
			st.setString(3, fecha);
			st.setString(4, lugar != null ? lugar : "0");

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			rollback(conn, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, null);
		}

		Mensaje m = MensajeLogica.obtenerMensaje("ASISCLASE-AGREGAR", "ES");
		return m != null ? m.getMensaje() : "";
	}

	/**
	 * Elimina el registro de asistencia de un usuario en una clase Hyrox y fecha.
	 */
	public String eliminarUsuarioAClaseHyrox(int idUsuario, int idClaseHyrox, String fecha) throws SQLException {

		String querySql = "DELETE FROM asistencia_a_clase_hyrox WHERE id_clase_hyrox = ? AND id_usuario = ? AND fecha = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idClaseHyrox);
			st.setInt(2, idUsuario);
			st.setString(3, fecha);

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			rollback(conn, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, null);
		}

		Mensaje m = MensajeLogica.obtenerMensaje("ASISCLASE-ELIMINAR", "ES");
		return m != null ? m.getMensaje() : "";
	}

	/**
	 * Elimina los registros de una clase Hyrox en varias fechas.
	 */
	public String eliminarUsuarioAClaseHyroxVariasFechas(int idClaseHyrox, List<String> fechas) throws SQLException {

		String querySql = "DELETE FROM asistencia_a_clase_hyrox WHERE id_clase_hyrox = ? AND fecha = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			if (fechas != null) {
				for (String fecha : fechas) {
					st.setInt(1, idClaseHyrox);
					st.setString(2, fecha);
					st.addBatch();
				}
				st.executeBatch();
			}

			conn.commit();

		} catch (SQLException e) {
			rollback(conn, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, null);
		}

		Mensaje m = MensajeLogica.obtenerMensaje("ASISCLASE-ELIMINAR", "ES");
		return m != null ? m.getMensaje() : "";
	}

	/**
	 * Actualiza un campo de la asistencia de un usuario en una clase Hyrox.
	 *
	 * <p>El nombre del campo se valida contra una lista blanca para evitar inyeccion SQL.</p>
	 */
	public String actualizarAsistenciaHyrox(String campo, String valor, int idUsuario, int idClaseHyrox, String fecha)
			throws SQLException {

		if (campo == null || !COLUMNAS_ACTUALIZABLES.contains(campo.toLowerCase())) {
			throw new SQLException("Campo no permitido para actualizar: " + campo);
		}

		String querySql = "UPDATE asistencia_a_clase_hyrox SET " + campo
				+ " = ? WHERE id_usuario = ? AND id_clase_hyrox = ? AND fecha = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setString(1, valor);
			st.setInt(2, idUsuario);
			st.setInt(3, idClaseHyrox);
			st.setString(4, fecha);

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			rollback(conn, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, null);
		}

		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m != null ? m.getMensaje() : "";
	}

	/* ------------------------------------------------------------------ */
	/* Lugares                                                            */
	/* ------------------------------------------------------------------ */

	/**
	 * Obtiene la lista de lugares ocupados de una clase Hyrox en una fecha.
	 */
	public List<String> obtenerLugaresUsadosHyrox(int idClaseHyrox, String fecha) throws SQLException {

		String querySql = "SELECT lugar FROM asistencia_a_clase_hyrox "
				+ "WHERE lugar IS NOT NULL AND lugar NOT IN('0') AND id_clase_hyrox = ? AND fecha = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		List<String> lugares = new ArrayList<>();

		try {
			st = conn.prepareStatement(querySql);
			st.setInt(1, idClaseHyrox);
			st.setString(2, fecha);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				lugares.add(rs.getString(1));
			}

		} catch (SQLException e) {
			log.error("Error al obtener los lugares usados de la clase Hyrox " + idClaseHyrox, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return lugares;
	}

	/**
	 * Obtiene el numero de lugares ocupados de una clase Hyrox en una fecha.
	 */
	public int obtenerNumLugaresUsadosHyrox(int idClaseHyrox, String fecha) throws SQLException {

		String querySql = "SELECT COUNT(*) FROM asistencia_a_clase_hyrox "
				+ "WHERE lugar IS NOT NULL AND lugar NOT IN('0') AND id_clase_hyrox = ? AND fecha = ?";

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		int lugares = 0;

		try {
			st = conn.prepareStatement(querySql);
			st.setInt(1, idClaseHyrox);
			st.setString(2, fecha);
			rs = st.executeQuery();

			if (rs != null && rs.next()) {
				lugares = rs.getInt(1);
			}

		} catch (SQLException e) {
			log.error("Error al contar los lugares usados de la clase Hyrox " + idClaseHyrox, e);
			throw new SQLException(e);
		} finally {
			cerrar(conn, st, rs);
		}

		return lugares;
	}

	/**
	 * Construye el mapa de lugares de una clase Hyrox, marcando los que ya estan ocupados.
	 */
	public LugaresClaseSeccionado lugaresClaseHyrox(int idClaseHyrox, String fecha) {

		LugaresClaseSeccionado lcs = new LugaresClaseSeccionado();
		List<LugaresClase> listLugares = new ArrayList<>();
		List<LugaresClase> listLugares2 = new ArrayList<>();
		List<LugaresClase> listLugares3 = new ArrayList<>();
		List<LugaresClase> listLugares4 = new ArrayList<>();
		List<LugaresClase> listLugares5 = new ArrayList<>();

		try {
			ClaseHyroxLogica cl = new ClaseHyroxLogica();
			ClaseHyrox clase = cl.obtenerPorId(idClaseHyrox);

			if (clase == null) {
				log.debug("No existe la clase Hyrox " + idClaseHyrox);
				return lcs;
			}

			int totalLugares = parseEntero(clase.getLugares(), 0);
			List<String> lugaresUsados = obtenerLugaresUsadosHyrox(idClaseHyrox, fecha);

			for (int i = 1; i <= totalLugares; i++) {
				String lugar = String.valueOf(i);

				LugaresClase lg = new LugaresClase();
				lg.setNumero(i);
				lg.setSeleccionado(lugaresUsados.contains(lugar));

				if (i <= 4) {
					listLugares.add(lg);
				} else if (i <= 8) {
					listLugares2.add(lg);
				} else if (i <= 12) {
					listLugares3.add(lg);
				} else if (i <= 16) {
					listLugares4.add(lg);
				} else {
					listLugares5.add(lg);
				}
			}

		} catch (SQLException e) {
			log.error("Error al generar los lugares de la clase Hyrox " + idClaseHyrox, e);
		}

		lcs.setL1(listLugares);
		lcs.setL2(listLugares2);
		lcs.setL3(listLugares3);
		lcs.setL4(listLugares4);
		Collections.reverse(listLugares5);
		lcs.setL5(listLugares5);

		return lcs;
	}

	/* ------------------------------------------------------------------ */
	/* Validaciones de inscripcion                                        */
	/* ------------------------------------------------------------------ */

	/**
	 * Valida que la inscripcion del usuario permita tomar clases.
	 */
	public InscripcionUs validoInscripcionUsuario(int idUsuario, boolean eliminar) {

		InscripcionUs inscripcionUs = null;
		InscripcionLogica insLogica = new InscripcionLogica();
		String mensaje = "";

		try {
			Inscripcion ins = insLogica.obtenerUlimaInscripcionDeUsuario(idUsuario);

			if (ins != null) {
				inscripcionUs = new InscripcionUs();
				inscripcionUs.setInscripcion(ins);

				if (ins.getTipoInscripcionDesc() != null && ins.getTipoInscripcionDesc().contains("Clase")) {
					log.debug("Clases de paquete:" + ins.getClases() + " - Clases restantes:" + ins.getClases_restantes()
							+ " Fecha corte:" + ins.getFechaCorte());

					Date fechaActual = new Date();
					Date fechaCorte = Utilidades.cadenaToDate(ins.getFechaCorte());
					if (fechaActual.after(fechaCorte)) {
						log.debug("La fecha permitida para usar clases se termino");
						mensaje = MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FECHAPERMITIDAPAQCLASES", "ES");
					}
					inscripcionUs.setEsPorClases(true);
					if (!eliminar && ins.getClases_restantes() == 0) {
						log.info("Ya no tiene clases");
						mensaje = MensajeLogica.obtenerMensajeCompleto("ASISCLASE-SINCLASES", "ES");
					}

				} else {
					inscripcionUs.setEsPorClases(false);
					log.debug("Fecha corte:" + ins.getFechaCorte());

					Date fechaActual = new Date();
					Date fechaCorte = Utilidades.cadenaToDate(ins.getFechaCorte());
					if (fechaActual.after(fechaCorte)) {
						log.debug(MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FINMENSUALIDAD", "ES"));
						mensaje = MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FINMENSUALIDAD", "ES");
					}
				}
				inscripcionUs.setMensaje(mensaje);

			} else {
				log.debug("No tiene inscripcion");
				inscripcionUs = new InscripcionUs();
				mensaje = MensajeLogica.obtenerMensajeCompleto("ASISCLASE-SININSCRIPCION", "ES");
				inscripcionUs.setMensaje(mensaje);
			}

		} catch (SQLException e) {
			log.error("Error al validar la inscripcion del usuario " + idUsuario, e);
			inscripcionUs = new InscripcionUs();
			inscripcionUs.setMensaje(e.getMessage());
		}

		return inscripcionUs;
	}

	/* ------------------------------------------------------------------ */
	/* Validaciones de horario                                            */
	/* ------------------------------------------------------------------ */

	public boolean horarioFueraDeClase(String hora, String horaClase, String horario) {
		return new AsistenciaAClaseLogica().horarioFueraDeClase(hora, horaClase, horario);
	}

	public boolean horarioParaFalta(String hora, String horaClase, String horario) {
		return new AsistenciaAClaseLogica().horarioParaFalta(hora, horaClase, horario);
	}

	public boolean horarioParaValFalta(String hora, String horaClase, String horario) {
		return new AsistenciaAClaseLogica().horarioParaValFalta(hora, horaClase, horario);
	}

	public boolean horarioAntesDeClase(String hora, String horaClase, String horario, String fecha) {
		return new AsistenciaAClaseLogica().horarioAntesDeClase(hora, horaClase, horario, fecha);
	}

	/* ------------------------------------------------------------------ */
	/* Utilidades internas                                                */
	/* ------------------------------------------------------------------ */

	/**
	 * Mapea una fila de {@code clase_hyrox} a partir de la columna inicial indicada (1-based).
	 */
	private ClaseHyrox mapearClase(ResultSet rs, int inicio) throws SQLException {
		ClaseHyrox clase = new ClaseHyrox();
		clase.setIdClaseHyrox(rs.getInt(inicio));
		clase.setTipoClase(rs.getInt(inicio + 1));
		clase.setNombre(rs.getString(inicio + 2));
		clase.setFecha(rs.getString(inicio + 3));
		clase.setLugares(rs.getString(inicio + 4));
		clase.setWod(rs.getString(inicio + 5));
		clase.setHoraInicio(rs.getString(inicio + 6));
		clase.setHoraFin(rs.getString(inicio + 7));
		clase.setHorario(rs.getString(inicio + 8) != null ? rs.getString(inicio + 8) : "");
		clase.setProfesor(rs.getString(inicio + 9));
		clase.setSociedad(rs.getInt(inicio + 10));
		clase.setEstatus(rs.getString(inicio + 11));
		clase.setFechaCreacion(rs.getString(inicio + 12));
		clase.setDescripcionHorario(descripcionHorario(clase));
		return clase;
	}

	private String descripcionHorario(ClaseHyrox clase) {
		String horario = clase.getHorario() != null ? clase.getHorario() : "";
		String amPm;
		if ("M".equals(horario)) {
			amPm = "AM";
		} else if ("T".equals(horario)) {
			amPm = "PM";
		} else {
			amPm = horario;
		}
		return "Clase de " + clase.getHoraInicio() + " " + amPm + " a " + clase.getHoraFin() + " " + amPm;
	}

	private static int parseEntero(String valor, int porDefecto) {
		try {
			return valor != null ? Integer.parseInt(valor.trim()) : porDefecto;
		} catch (NumberFormatException e) {
			return porDefecto;
		}
	}

	private void rollback(Connection conn, SQLException causa) {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException e) {
				log.error("Error al revertir la transaccion", e);
			}
		}
		log.error("Error en la operacion de asistencia Hyrox, se revirtio la transaccion", causa);
	}

	/**
	 * Cierra de forma segura el ResultSet, el Statement y la Connection.
	 */
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

	public boolean borrarClasesAtrasParaBorrado() {
		String querySql = "DELETE FROM control_asistencias_fitness.asistencia_a_clase_hyrox " +
				"WHERE fecha < NOW() - INTERVAL 1 MONTH;";

		try (Connection conn = Conexion.getConnectionDbPool();
				PreparedStatement st = conn.prepareStatement(querySql)) {

			st.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
