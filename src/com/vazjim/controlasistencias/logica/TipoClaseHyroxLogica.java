package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.modelo.TipoClaseHyrox;

public class TipoClaseHyroxLogica {

	private static Logger log = Logger.getLogger(TipoClaseHyroxLogica.class);

	public List<TipoClaseHyrox> obtener(String columna, String valor) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<TipoClaseHyrox> tipos = new ArrayList<>();
		TipoClaseHyrox tipo;

		querySql = "SELECT id_tipo_clase_hyrox, descripcion FROM tipo_clase_hyrox ";

		String where = "";

		if (columna != null && !"".equals(columna)) {
			if (!where.equals(""))
				where += " AND ";
			where += " " + columna + " = '" + valor + "'";
		}

		if (!where.equals("")) {
			where = " WHERE " + where + " ORDER BY id_tipo_clase_hyrox";
			querySql += where;
		} else {
			querySql += " ORDER BY id_tipo_clase_hyrox";
		}

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				tipo = new TipoClaseHyrox();
				tipo.setIdTipoClaseHyrox(rs.getInt(1));
				tipo.setDescripcion(rs.getString(2));

				tipos.add(tipo);
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

		return tipos;
	}

	public TipoClaseHyrox obtenerPorId(int idTipoClaseHyrox) throws SQLException {
		TipoClaseHyrox t = null;
		List<TipoClaseHyrox> tipo = obtener("id_tipo_clase_hyrox", String.valueOf(idTipoClaseHyrox));
		if (!tipo.isEmpty()) {
			t = tipo.get(0);
		}
		return t;
	}

	public String insertar(TipoClaseHyrox tipo) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO tipo_clase_hyrox(descripcion) VALUES(?);";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, tipo.getDescripcion());
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

	public String actualizar(TipoClaseHyrox tipo) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE tipo_clase_hyrox SET descripcion = ? WHERE id_tipo_clase_hyrox = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, tipo.getDescripcion());
			st.setInt(2, tipo.getIdTipoClaseHyrox());

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

	public String eliminar(int idTipoClaseHyrox) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM tipo_clase_hyrox WHERE id_tipo_clase_hyrox = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idTipoClaseHyrox);

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

	public boolean validarDuplicidad(String descripcion) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";
		int count = 0;
		boolean salida = false;

		querySql = "SELECT id_tipo_clase_hyrox FROM tipo_clase_hyrox WHERE descripcion = ?";

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, descripcion);
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
