package com.vazjim.controlasistencias.conexion;



import java.sql.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;



public class Conexion {

	public static Connection getConnectionDbPool() {

		InitialContext initCtx = null;
		Connection conn = null;

		try {
			
			initCtx = new InitialContext();

			DataSource ds = (DataSource) initCtx.lookup("java:comp/env/jdbc/control_asistencias_fitness");

			if (ds != null) {
				conn = ds.getConnection();
			}

		} catch (SQLException| NamingException e) {
			throw new RuntimeException(e);
		}

		return conn;
	}

}
