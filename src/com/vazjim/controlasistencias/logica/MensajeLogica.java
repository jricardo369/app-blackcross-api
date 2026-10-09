package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.poi.ss.formula.functions.T;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.utilidades.Propiedades;
import com.vazjim.controlasistencias.utilidades.Utilidades;

public class MensajeLogica {

	static Utilidades util = new Utilidades();

	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getPropertiesErrores();

	static String respuesta = "";
	static int codigoStatus = 200;
	static String descripcion = "";
	static String mensaje = "";

	// private static Logger log = Logger.getLogger(MensajeLogica.class);

	public Mensaje obtener(String codigo, String idioma) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		Mensaje obj = null;

		querySql = "SELECT id_mensaje,codigo,tipo,mensaje,descripcion,idioma,codigoEstatus FROM mensaje  WHERE codigo = ? AND idioma = ?";

		// log.info("querySql"+querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, codigo);
			st.setString(2, idioma);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				obj = new Mensaje();
				obj.setIdMensaje(rs.getString(1));
				obj.setCodigo(rs.getString(2));
				obj.setTipo(rs.getString(3));
				obj.setMensaje(rs.getString(4));
				obj.setDescripcion(rs.getString(5));
				obj.setIdioma(rs.getString(6));
				obj.setCodigoEstatus(rs.getInt(7));

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

		// log.info("obteniendo mensaje " + codigo + " en idioma " + idioma);

		return obj;
	}

	public String insertar(Mensaje mensaje) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO mensaje(codigo,tipo,mensajes,idioma) " + "VALUES(?,?,?,?,?)";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, mensaje.getCodigo());
			st.setString(2, mensaje.getTipo());
			st.setString(3, mensaje.getMensaje());
			st.setString(4, mensaje.getIdioma());
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
		return "El mensaje se creo correctamente";
	}

	public static Mensaje obtenerMensaje(String codigo, String idioma) {
		MensajeLogica ml = new MensajeLogica();
		Mensaje m = null;
		try {
			m = ml.obtener(codigo, idioma);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return m;
	}

	public static String obtenerMensajeCompleto(String codigo, String idioma) {

		if (codigo != null && !"".equals(codigo)) {
			Mensaje m = MensajeLogica.obtenerMensaje(codigo, idioma);
			if (m == null) {
				mensaje = "No se encontro mensaje del error " + codigo;
				descripcion = "No se encontro mensaje del error " + codigo;
				codigoStatus = 500;
				return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
			} else {
				mensaje = m.getMensaje();
				descripcion = m.getDescripcion();
				codigoStatus = m.getCodigoEstatus();
				return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
			}
		} else {
			return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
		}

	}
	
	public static String obtenerMensajeDesc(String codigo, String idioma) {

		if (codigo != null && !"".equals(codigo)) {
			Mensaje m = MensajeLogica.obtenerMensaje(codigo, idioma);
			if (m == null) {
				return "No se encontro mensaje del error " + codigo;
			} else {
				return m.getDescripcion();
			}
		} else {
			return mensaje;
		}

	}
	
	public static String obtenerMensajeCompletoConParametros(String codigo, String idioma,String p1,String p2,String p3,String p4) {

		if (codigo != null && !"".equals(codigo)) {
			Mensaje m = MensajeLogica.obtenerMensaje(codigo, idioma);
			if (m == null) {
				mensaje = "No se encontro mensaje del error " + codigo;
				descripcion = "No se encontro mensaje del error " + codigo;
				codigoStatus = 500;
				return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
			} else {
				mensaje = descripcion = m.getMensaje().replace("P1", p1).replace("P2", p2).replace("P3", p3).replace("P4", p4);
				descripcion = m.getDescripcion().replace("P1", p1).replace("P2", p2).replace("P3", p3).replace("P4", p4);
				codigoStatus = m.getCodigoEstatus();
				return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
			}
		} else {
			return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
		}

	}

	@SuppressWarnings("hiding")
	public static <T> String obtenerMensajeCompletoConRespuesta(String codigo, String idioma, T respuesta) {

		if (codigo != null && !"".equals(codigo)) {
			Mensaje m = MensajeLogica.obtenerMensaje(codigo, idioma);
			if (m == null) {
				mensaje = "No se encontro mensaje del error " + codigo;
				descripcion = "No se encontro mensaje del error " + codigo;
				codigoStatus = 500;
				return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
			} else {
				mensaje = m.getMensaje();
				descripcion = m.getDescripcion();
				codigoStatus = m.getCodigoEstatus();
				return util.generarRespuesta(respuesta, codigoStatus, mensaje, descripcion).toString();
			}
		} else {
			codigoStatus = 200;
			mensaje = "";
			descripcion = "";
			return util.generarRespuesta(respuesta, codigoStatus, mensaje, descripcion).toString();
		}

	}

	public static String obtenerSoloCompleto(String codigo, String idioma) {

		Mensaje m = MensajeLogica.obtenerMensaje(codigo, idioma);
		if (m == null) {
			mensaje = "No se encontro mensaje del error " + codigo;
			descripcion = "No se encontro mensaje del error " + codigo;
			codigoStatus = 500;
			return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
		} else {
			mensaje = m.getMensaje();
			descripcion = m.getDescripcion();
			codigoStatus = m.getCodigoEstatus();
			return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
		}

	}

	public static String errorBD(Exception e) {
		codigoStatus = 500;
		mensaje = prop.getProperty("error-conexion-bd");
		descripcion = e.getMessage();
		return util.generarRespuesta("", codigoStatus, mensaje, descripcion).toString();
	}

}
