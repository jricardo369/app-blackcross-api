package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.LogInterno;
import com.vazjim.controlasistencias.utilidades.Utilidades;

public class LogLogica {

	static Utilidades util = new Utilidades();

	static String respuesta = "";
	static int codigoStatus = 200;
	static String descripcion = "";
	static String mensaje = "";

	//private static Logger log = Logger.getLogger(LogLogica.class);

	public List<LogInterno> obtener(String fechai, String fechaf, String tipo, String accion) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		LogInterno obj = null;
		List<LogInterno> lista = new ArrayList<>();

		querySql = "SELECT tipo,fecha,accion,usuario,datos_entrada,datos_salida FROM log_interno  WHERE fecha BETWEEN ? AND ? ";
		
		if (tipo != null && !"".equals(tipo)) {
			querySql = querySql + " AND tipo = ? ";
		}
		
		if (accion != null && !"".equals(accion)) {
			querySql = querySql + " AND accion like '%"+accion+"%' "; 
		}

		//log.info("querySql"+querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fechai);
			st.setString(2, fechaf);
			if (tipo != null && !"".equals(tipo)) {
				st.setString(3, tipo);
			}
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				obj = new LogInterno();
				obj.setTipo(rs.getString(1));
				obj.setFecha(rs.getString(2));
				obj.setAccion(rs.getString(3));
				obj.setUsuario(rs.getString(4));
				obj.setDatosEntrada(rs.getString(5));
				obj.setDatosSalida(rs.getString(6));
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
			if (conn != null) {
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

	public String insertar(LogInterno log) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO log_interno(tipo,accion,usuario,datos_entrada,datos_salida) " + "VALUES(?,?,?,?,?)";

		try {

			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, log.getTipo());
			st.setString(2, log.getAccion());
			st.setString(3, log.getUsuario());
			st.setString(4, log.getDatosEntrada());
			st.setString(5, log.getDatosSalida());

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
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		return "log insertado";
	}

	public static String insertarLogInterno(LogInterno log) {

		String salida = "";
		
		try {
			LogLogica logica = new LogLogica();
			salida = logica.insertar(log);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return salida;

	}
	
	public static String insertarLogInternoAccion(String tipo,String accion,int idUsuario) {

		String salida = "";
		
		try {
			LogLogica logica = new LogLogica();
			LogInterno l = new LogInterno();
			l.setTipo(tipo);
			l.setAccion(accion);
			l.setUsuario(String.valueOf(idUsuario));
			salida = logica.insertar(l);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return salida;

	}

}
