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
import com.vazjim.controlasistencias.modelo.TipoPaquete;

public class TipoPaqueteLogica {

	private static Logger log = Logger.getLogger(TipoPaqueteLogica.class);

	public List<TipoPaquete> obtener(String columna, String valor) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<TipoPaquete> tipos = new ArrayList<>();
		TipoPaquete tipo;

		querySql = "SELECT id_tipo_paquete, titulo, descripcion FROM tipo_paquete ";

		String where = "";

		if (columna != null && !"".equals(columna)) {
			if (!where.equals(""))
				where += " AND ";
			where += " " + columna + " = '" + valor + "'";
		}

		if (!where.equals("")) {
			where = " WHERE " + where + " ORDER BY id_tipo_paquete";
			querySql += where;
		} else {
			querySql += " ORDER BY id_tipo_paquete";
		}

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				tipo = new TipoPaquete();
				tipo.setIdTipoPaquete(rs.getInt(1));
				tipo.setTitulo(rs.getString(2));
				tipo.setDescripcion(rs.getString(3));

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

	public TipoPaquete obtenerPorId(int idTipoPaquete) throws SQLException {
		TipoPaquete t = null;
		List<TipoPaquete> tipo = obtener("id_tipo_paquete", String.valueOf(idTipoPaquete));
		if (!tipo.isEmpty()) {
			t = tipo.get(0);
		}
		return t;
	}

	public String insertar(TipoPaquete tipo) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO tipo_paquete(titulo, descripcion) VALUES(?, ?);";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, tipo.getTitulo());
			st.setString(2, tipo.getDescripcion());
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

	public String actualizar(TipoPaquete tipo) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE tipo_paquete SET titulo = ?, descripcion = ? WHERE id_tipo_paquete = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, tipo.getTitulo());
			st.setString(2, tipo.getDescripcion());
			st.setInt(3, tipo.getIdTipoPaquete());

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

	public String eliminar(int idTipoPaquete) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM tipo_paquete WHERE id_tipo_paquete = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idTipoPaquete);

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

		querySql = "SELECT id_tipo_paquete FROM tipo_paquete WHERE descripcion = ?";

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
