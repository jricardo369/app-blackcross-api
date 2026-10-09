package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.utilidades.Propiedades;

public class ConfiguracionLogica {

	//private static Logger log = Logger.getLogger(ConfiguracionLogica.class);

	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getProperties();

	public static List<Configuracion> obtener(String identificador) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		List<Configuracion> lista = new ArrayList<>();
		String querySql = "";
		Configuracion config = null;

		querySql = "SELECT id_configuracion,identificador,valor_abajo,valor_arriba FROM configuracion ";

		if (identificador != null && !"".equals(identificador)) {
			querySql = querySql + "WHERE identificador = ?;";
		}

		//log.info("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			if (identificador != null && !"".equals(identificador)) {
				st.setString(1, identificador);
			}
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				config = new Configuracion();
				config.setIdIdentificador(rs.getString(1));
				config.setIdentificador(rs.getString(2));
				config.setValorAbajo(rs.getString(3));
				config.setValorArriba(rs.getString(4));
				lista.add(config);

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
		
		//log.debug("Se obtuvo configuracion"+identificador);

		return lista;
	}

	public static Configuracion obtenerConIdentificador(String identificador) {
		Configuracion config = null;
		List<Configuracion> lista;
		try {
			lista = obtener(identificador);
			if (!lista.isEmpty()) {
				config = lista.get(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return config;
	}
	
	public String actualizarCampo(String identificador, String valor) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE configuracion SET valor_abajo = ? WHERE identificador = ?;";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setString(1, valor);
			st.setString(2, identificador);

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
		return "Se actualizo la configuración con el identificador " + identificador;
	}

}
