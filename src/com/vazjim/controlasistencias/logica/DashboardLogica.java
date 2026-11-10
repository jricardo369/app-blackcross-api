package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Dashboard;

public class DashboardLogica {

	private static Logger log = Logger.getLogger(DashboardLogica.class);

	/**
	 * Obtiene los indicadores del panel: socios activos y asistencias registradas
	 * en la fecha indicada. Las asistencias solo cuentan si el usuario esta activo.
	 *
	 * @param fecha fecha en formato yyyy-MM-dd
	 * @return indicadores del dashboard
	 * @throws SQLException
	 */
	public Dashboard obtenerDatosDashboard(String fecha) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		Dashboard dashboard = new Dashboard();

		String querySql = "SELECT "
				+ "(SELECT COUNT(*) FROM usuario WHERE estatus = 1) AS usuarios_activos, "
				+ "(SELECT COUNT(*) FROM asistencia_a_clase a "
				+ "JOIN usuario u ON u.id_usuario = a.id_usuario "
				+ "WHERE a.fecha = ? AND u.estatus = 1) AS asistencias";

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				dashboard.setUsuariosActivos(rs.getInt("usuarios_activos"));
				dashboard.setAsistencias(rs.getInt("asistencias"));
			}

		} catch (SQLException e) {
			log.error("Error al obtener los datos del dashboard", e);
			throw new SQLException(e);
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

		return dashboard;
	}

}
