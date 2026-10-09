package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Asueto;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.utilidades.Utilidades;

public class AsuetoLogica {

	private static Logger log = Logger.getLogger(AsuetoLogica.class);

	public List<Asueto> obtener(String columna, String valor,int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<Asueto> lista = new ArrayList<>();
		Asueto obj;

		querySql = "SELECT  a.id_asueto,a.id_clase,a.fecha,c.nombre,c.sociedad FROM asueto a JOIN clase c ON c.id_clase = a.id_clase";
		
		String where = "";
		
		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " a.sociedad = " + sociedad + " ";
		}
		
		if (columna != null && !"".equals(columna)) {
			if (!where.equals(""))
				where += " AND ";
			where += " " + columna + " = '" + valor + "'";
		}

		if (!where.equals("")) {
			if (!where.equals(""))
				where = " WHERE " + where + " ORDER BY a.fecha DESC";
			querySql += where;
		}

		System.out.println("query:" + querySql);

		
		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				obj = new Asueto();
				obj.setIdAsueto(rs.getInt(1));
				obj.setIdClase(rs.getInt(2));
				obj.setFecha(rs.getString(3));
				obj.setClaseDescripcion(rs.getString(4));
				obj.setFechaDescripcion(Utilidades.fechaEnLetra(rs.getString(3)));
				lista.add(obj);
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

		log.info("Se obtuvieron asuetos:" + lista.size());
		return lista;
	}

	public String insertar(Asueto obj) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO asueto(id_clase,fecha,sociedad) " + "VALUES(?,?,?)";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, obj.getIdClase());
			st.setString(2, obj.getFecha());
			st.setInt(3, obj.getSociedad());
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
		Mensaje m = MensajeLogica.obtenerMensaje("ASUETO-CREAR", "ES");
		return m.getMensaje();
	}
	
	public String insertarVariosAsuetos(int idClase,List<String> fechas,int sociedad) throws SQLException {
		
		log.info("fechas a insertar " + fechas);

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO asueto(id_clase,fecha,sociedad) " + "VALUES(?,?,?);";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			for (String string : fechas) {
				st.setInt(1, idClase);
				st.setString(2, string);
				st.setInt(3, sociedad);
				st.addBatch();
			}
			
			st.executeBatch();
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
		Mensaje m = MensajeLogica.obtenerMensaje("ASUETO-CREAR", "ES");
		return m.getMensaje();
	}

	public String eliminarAsueto(int idAsueto,String fecha) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		try {

			if (idAsueto == 0) {

				log.info("elminará por fecha");
				querySql = "DELETE FROM asueto WHERE fecha = ?";
				conn.setAutoCommit(false);
				st = conn.prepareStatement(querySql);
				st.setString(1, fecha);

			} else if ("".equals("")) {

				log.info("elminará por asueto");
				querySql = "DELETE FROM asueto WHERE id_asueto = ?";
				conn.setAutoCommit(false);
				st = conn.prepareStatement(querySql);
				st.setInt(1, idAsueto);

			}

			st.execute();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
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

		Mensaje m = MensajeLogica.obtenerMensaje("ASUETO-ELIMINAR", "ES");
		return m.getMensaje();
	}

	public boolean esAsuetoGeneral(String fecha, int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		String querySql = "";
		int count = 0;
		boolean salida = false;

		querySql = "SELECT count(id_asueto) FROM asueto  a WHERE a.fecha = ? AND a.id_clase = 0 AND a.sociedad = ?;";

		// log.info(querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, sociedad);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				count = rs.getInt(1);

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

		log.info("Asueto?" + salida);
		return salida;
	}

	public boolean asuetoDuplicado(int idClase, String fecha,int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		String querySql = "";
		int count = 0;
		boolean salida = false;

		querySql = "SELECT count(id_asueto) FROM asueto  a WHERE a.fecha = ? AND a.id_clase = ? AND a.sociedad = ?;";

		// log.info(querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, idClase);
			st.setInt(3, sociedad);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				count = rs.getInt(1);

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

		log.info("Asueto?" + salida);
		return salida;
	}

}
