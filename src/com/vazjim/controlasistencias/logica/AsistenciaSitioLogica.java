package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.AsistenciaSitio;
import com.vazjim.controlasistencias.modelo.Mensaje;

public class AsistenciaSitioLogica {
	
	public String agregarAsistenciaSitio(int idUsuario, String fecha,int sociedad) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO asistencia_sitio(id_usuario,fecha_alta,sociedad) " + "VALUES(?,?,?)";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, idUsuario);
			st.setString(2, fecha);
			st.setInt(3, sociedad);

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

		Mensaje m = MensajeLogica.obtenerMensaje("ASISCLASE-AGREGAR", "ES");
		return m.getMensaje();
	}
	
	public List<AsistenciaSitio> obtener(String columna, String valor,int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<AsistenciaSitio> lista = new ArrayList<>();
		AsistenciaSitio obj;

		querySql = "SELECT a.id_asistencia_sitio,a.fecha_alta,u.nombre,u.id_usuario "
				+ "FROM asistencia_sitio a "
				+ "JOIN usuario u on u.id_usuario = a.id_usuario ";
		
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
				where = " WHERE " + where + " ORDER BY a.fecha_alta DESC";
			querySql += where;
		}else{
			querySql = querySql + " ORDER BY a.fecha_alta DESC";
		}

		System.out.println("query:" + querySql);

		
		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				obj = new AsistenciaSitio();
				obj.setIdAsistenciaSitio(rs.getInt(1));
				obj.setFechaAlta(rs.getString(2));
				obj.setNombreUsuario(rs.getString(3));
				obj.setIdUsuario(rs.getInt(4));
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

		
		return lista;
	}

}
