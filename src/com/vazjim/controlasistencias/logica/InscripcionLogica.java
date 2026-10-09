package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.modelo.TipoInscripcion;
import com.vazjim.controlasistencias.modelo.TipoPago;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.utilidades.Utilidades;

public class InscripcionLogica {

	//private static Logger log = Logger.getLogger(InscripcionLogica.class);

	static Utilidades util = new Utilidades();

	static String respuesta = "";
	static int codigoStatus = 200;
	static String descripcion = "";
	static String mensaje = "";

	public List<Inscripcion> obtener(String fechai, String fechaf, int idUsuario, int tipoPago, int tipoIns,
			int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";
		String where = "";

		Inscripcion obj = null;
		List<Inscripcion> lista = new ArrayList<>();

		querySql = "SELECT p.id_inscripcion, u.id_usuario,u.nombre, tp.descripcion,tp.id_tipo_pago, i.id_tipo_inscripcion,"
				+ "i.tipo_inscripcion, p.comentario,p.fecha_alta,i.monto,p.fecha_inicio,p.clases,p.clases_restantes,p.fecha_corte,p.sociedad "
				+ "FROM inscripcion p " + "JOIN usuario u on u.id_usuario = p.id_usuario "
				+ "JOIN tipo_pago tp on tp.id_tipo_pago = p.id_tipo_pago "
				+ "JOIN tipo_inscripcion i on i.id_tipo_inscripcion = p.id_tipo_inscripcion ";

		where = "";

		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " p.sociedad = " + sociedad + " ";
		}

		if (idUsuario != 0 && !"".equals(idUsuario)) {
			if (!where.equals(""))
				where += " AND ";
			where += " u.id_usuario = " + idUsuario + " ";
		}

		if (tipoPago != 0 && !"".equals(tipoPago)) {
			if (!where.equals(""))
				where += " AND ";
			where += " tp.id_tipo_pago = " + tipoPago + " ";
		}

		if (tipoIns != 0 && !"".equals(tipoIns)) {
			if (!where.equals(""))
				where += " AND ";
			where += " i.id_tipo_inscripcion = " + tipoIns + " ";
		}

		if (!"".equals(fechai)) {
			if (!where.equals(""))
				where += " AND ";
			where += " p.fecha_inicio BETWEEN '" + fechai + "' AND '" + fechaf + "' ";
		}

		if (!where.equals("")) {
			if (!where.equals(""))
				where = " WHERE " + where + " ORDER BY fecha_alta desc";
			querySql += where;
		}

		System.out.println("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				obj = new Inscripcion();
				obj.setIdInscripcion(rs.getInt(1));
				obj.setIdUsuario(rs.getInt(2));
				obj.setNombreUsuario(rs.getString(3));
				obj.setDescripcionTipoPago("Se pago con " + rs.getString(4));
				obj.setTipoPago(rs.getInt(5));
				obj.setTipoInscripcion(rs.getInt(6));
				obj.setTipoInscripcionDesc(rs.getString(7));
				obj.setComentario(rs.getString(8));
				obj.setFechaAlta(rs.getString(9).equals("") ? "" : rs.getString(9).substring(0, 10));
				obj.setMonto(rs.getBigDecimal(10));
				obj.setFechaInicio(rs.getString(11).equals("") ? "" : rs.getString(11).substring(0, 10));
				obj.setClases(rs.getInt(12));
				obj.setClases_restantes(rs.getInt(13));
				obj.setFechaCorte(rs.getString(14).equals("") ? "" : rs.getString(14).substring(0, 10));
				obj.setDescripcion("Inscripcion tipo " + obj.getTipoInscripcionDesc() + " a partir del día "
						+ obj.getFechaInicio().substring(0, 10));
				obj.setSociedad(rs.getInt(15));
				if (!obj.getTipoInscripcionDesc().equals("Mensual")) {
					if (obj.getClases_restantes() != 0) {
						obj.setVencimiento("Le restan " + obj.getClases_restantes() + " días antes de "
								+ obj.getFechaCorte().substring(0, 10));
					} else {
						obj.setVencimiento("No cuenta con días");
					}
				} else {
					obj.setVencimiento("Vence " + obj.getFechaCorte().substring(0, 10));
				}

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

	public Inscripcion obtenerUlimaInscripcionDeUsuario(int idUsuario) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		Inscripcion obj = null;

		querySql = "SELECT p.id_inscripcion, u.id_usuario,u.nombre, tp.descripcion,tp.id_tipo_pago, i.id_tipo_inscripcion,"
				+ "i.tipo_inscripcion, p.comentario,p.fecha_alta,i.monto,p.fecha_inicio,p.clases,p.clases_restantes,p.fecha_corte,p.sociedad "
				+ "FROM inscripcion p " + "JOIN usuario u on u.id_usuario = p.id_usuario "
				+ "JOIN tipo_pago tp on tp.id_tipo_pago = p.id_tipo_pago "
				+ "JOIN tipo_inscripcion i on i.id_tipo_inscripcion = p.id_tipo_inscripcion "
				+ "WHERE p.id_usuario = ? ORDER BY p.id_inscripcion DESC limit 1";

		System.out.println("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				obj = new Inscripcion();
				obj.setIdInscripcion(rs.getInt(1));
				obj.setIdUsuario(rs.getInt(2));
				obj.setNombreUsuario(rs.getString(3));
				obj.setDescripcionTipoPago(rs.getString(4));
				obj.setTipoPago(rs.getInt(5));
				obj.setTipoInscripcion(rs.getInt(6));
				obj.setTipoInscripcionDesc(rs.getString(7));
				obj.setComentario(rs.getString(8));
				obj.setFechaAlta(rs.getString(9).equals("") ? "" : rs.getString(9).substring(0, 10));
				obj.setMonto(rs.getBigDecimal(10));
				obj.setFechaInicio(rs.getString(11).equals("") ? "" : rs.getString(11).substring(0, 10));
				obj.setClases(rs.getInt(12));
				obj.setClases_restantes(rs.getInt(13));
				obj.setFechaCorte(rs.getString(14).equals("") ? "" : rs.getString(14).substring(0, 10));
				obj.setDescripcion("Inscripcion tipo " + obj.getTipoInscripcionDesc() + " a partir del día "
						+ obj.getFechaInicio().substring(0, 10));
				obj.setSociedad(rs.getInt(15));
				if (!obj.getTipoInscripcionDesc().equals("Mensual")) {
					if (obj.getClases_restantes() != 0) {
						obj.setVencimiento("Le restan " + obj.getClases_restantes() + " clases antes de "
								+ obj.getFechaCorte().substring(0, 10));
					} else {
						obj.setVencimiento("No cuenta con días");
					}
				} else {
					obj.setVencimiento("Vence " + obj.getFechaCorte().substring(0, 10));
				}

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

		return obj;
	}

	public Inscripcion obtenerInscripcionPorId(int idInscripcion) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		Inscripcion obj = null;

		querySql = "SELECT p.id_inscripcion, u.id_usuario,u.nombre, tp.descripcion,tp.id_tipo_pago, i.id_tipo_inscripcion,"
				+ "i.tipo_inscripcion, p.comentario,p.fecha_alta,i.monto,p.fecha_inicio,p.clases,p.clases_restantes,p.fecha_corte,p.sociedad "
				+ "FROM inscripcion p " + "JOIN usuario u on u.id_usuario = p.id_usuario "
				+ "JOIN tipo_pago tp on tp.id_tipo_pago = p.id_tipo_pago "
				+ "JOIN tipo_inscripcion i on i.id_tipo_inscripcion = p.id_tipo_inscripcion "
				+ "WHERE p.id_inscripcion = ? ";

		System.out.println("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idInscripcion);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				obj = new Inscripcion();
				obj.setIdInscripcion(rs.getInt(1));
				obj.setIdUsuario(rs.getInt(2));
				obj.setNombreUsuario(rs.getString(3));
				obj.setDescripcionTipoPago(rs.getString(4));
				obj.setTipoPago(rs.getInt(5));
				obj.setTipoInscripcion(rs.getInt(6));
				obj.setTipoInscripcionDesc(rs.getString(7));
				obj.setComentario(rs.getString(8));
				obj.setFechaAlta(rs.getString(9).equals("") ? "" : rs.getString(9).substring(0, 10));
				obj.setMonto(rs.getBigDecimal(10));
				obj.setFechaInicio(rs.getString(11).equals("") ? "" : rs.getString(11).substring(0, 10));
				obj.setClases(rs.getInt(12));
				obj.setClases_restantes(rs.getInt(13));
				obj.setFechaCorte(rs.getString(14).equals("") ? "" : rs.getString(14).substring(0, 10));
				obj.setDescripcion("Inscripcion tipo " + obj.getTipoInscripcionDesc() + " a partir del día "
						+ obj.getFechaInicio().substring(0, 10));
				obj.setSociedad(rs.getInt(15));
				if (!obj.getTipoInscripcionDesc().equals("Mensual")) {
					if (obj.getClases_restantes() != 0) {
						obj.setVencimiento("Le restan " + obj.getClases_restantes() + " días antes de "
								+ obj.getFechaCorte().substring(0, 10));
					} else {
						obj.setVencimiento("No cuenta con días");
					}
				} else {
					obj.setVencimiento("Vence " + obj.getFechaCorte().substring(0, 10));
				}

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

		return obj;
	}

	public String insertar(Inscripcion ins) throws SQLException {

		PreparedStatement st = null;
		int val = 0;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();
		TipoInscripcion ti = obtenerTipoInscripcion(ins.getTipoInscripcion());

		querySql = "INSERT INTO inscripcion(id_usuario,id_tipo_pago,id_tipo_inscripcion,comentario,fecha_inicio,clases,clases_restantes,fecha_corte,sociedad) "
				+ "VALUES(?,?,?,?,?,?,?,?,?)";

		try {

			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, ins.getIdUsuario());
			st.setInt(2, ins.getTipoPago());
			st.setInt(3, ins.getTipoInscripcion());
			String c = ins.getComentario() == null ? "" : Utilidades.quitarCaracteresEsp(ins.getComentario());
			st.setString(4, c);
			st.setString(5, ins.getFechaInicio());
			
			if (ti.getTipo_inscripcion().contains("Clase")) {
				
				st.setInt(6, ti.getValor());
				st.setInt(7, ti.getValor());

			} else {
				st.setInt(6, 0);
				st.setInt(7, 0);
			}

			if (ti.getTipo_inscripcion().contains("Semana")) {
				
				System.out.println("tipo clase no mensual");
				// Tomar dias permitidos para clases
				val = ti.getValor();
				String fechac = Utilidades.sumarDiasAFechasSinDomingos(ins.getFechaInicio(), val);
				st.setString(8, fechac);
				
			} else if (ti.getTipo_inscripcion().contains("Mensual") || ti.getTipo_inscripcion().contains("Mes")) {
				
				Configuracion confDiasCorte = ConfiguracionLogica.obtenerConIdentificador("DIAS-CORTE-CLASES");
				
				System.out.println("tipo clase mensual");
				System.out.println("fecha:"+ins.getFechaInicio()+"|"+val);
				
				val = ti.getValor();
				String fecha = Utilidades.ultimoDiaDeFecha(ins.getFechaInicio());
				//String fecha = ins.getFechaInicio();
				System.out.println("fecha base a sumar:"+fecha);
				
				//int valorASumar = ti.getValor();
				//System.out.println("valor a sumar:"+valorASumar);
				//String fechaMeses = Utilidades.ultimoDiaDeFecha(Utilidades.sumarMesesAFecha(fecha, valorASumar));
				
				int diasCorte = Integer.valueOf(confDiasCorte.getValorAbajo());
				
				System.out.println("fecha meses:"+fecha);
				String fechac = Utilidades.sumarDiasAFechas(fecha, diasCorte);
				System.out.println("fecha meses + "+diasCorte+" dias:"+fechac);
				st.setString(8, fechac);
				
			}

			st.setInt(9, ins.getSociedad());

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
		Mensaje m = MensajeLogica.obtenerMensaje("INS-INSERTAR", "ES");
		return m.getMensaje();
	}

	public boolean actualizarClasesRestantes(int idInscripcion, int clasesRestantes) throws SQLException {

		boolean salida = true;
		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE inscripcion SET clases_restantes = ? WHERE id_inscripcion = ?";

		try {

			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, clasesRestantes);
			st.setInt(2, idInscripcion);

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
		return salida;
	}

	public boolean eliminarInscripcion(int idInscripcion) throws SQLException {

		boolean salida = true;
		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM inscripcion WHERE id_inscripcion = ?";

		try {

			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idInscripcion);

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
		return salida;
	}
	
	public boolean eliminarInscripcionesUsuario(int idUsuario) throws SQLException {

		boolean salida = true;
		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM inscripcion WHERE id_usuario = ?";

		try {

			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);

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
		return salida;
	}

	public List<TipoInscripcion> obtenerTiposInscripcion(int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		TipoInscripcion obj = null;
		List<TipoInscripcion> lista = new ArrayList<>();

		querySql = "SELECT id_tipo_inscripcion,tipo_inscripcion,valor,monto,anio FROM tipo_inscripcion ";

		String where = "";

		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " sociedad = " + sociedad + " ";
		}

		if (!where.equals("")) {
			if (!where.equals(""))
				where = " WHERE " + where + " AND anio = year(curdate()) ";
			querySql += where;
		} else {
			where += " AND anio = year(curdate()";
		}

		System.out.println("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				obj = new TipoInscripcion();
				obj.setId(rs.getInt(1));
				obj.setTipo_inscripcion(rs.getString(2));
				obj.setValor(rs.getInt(3));
				obj.setMonto(rs.getBigDecimal(4));
				obj.setAnio(rs.getInt(5));
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

	public TipoInscripcion obtenerTipoInscripcion(int id) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		TipoInscripcion obj = null;

		querySql = "SELECT id_tipo_inscripcion,tipo_inscripcion,valor,monto,anio FROM tipo_inscripcion WHERE id_tipo_inscripcion = ?";

		System.out.println("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, id);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				obj = new TipoInscripcion();
				obj.setId(rs.getInt(1));
				obj.setTipo_inscripcion(rs.getString(2));
				obj.setValor(rs.getInt(3));
				obj.setMonto(rs.getBigDecimal(4));
				obj.setAnio(rs.getInt(5));

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

		return obj;
	}

	public List<TipoPago> obtenerTiposPagos(int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		TipoPago obj = null;
		List<TipoPago> lista = new ArrayList<>();

		querySql = "SELECT id_tipo_pago,descripcion,codigo FROM tipo_pago ";

		String where = "";

		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " sociedad = " + sociedad + " ";
		}

		if (!where.equals("")) {
			if (!where.equals(""))
				where = " WHERE " + where + " ";
			querySql += where;
		}

		System.out.println("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				obj = new TipoPago();
				obj.setId(rs.getInt(1));
				obj.setDescripcion(rs.getString(2));
				obj.setCodigo(rs.getString(3));
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

	public static void main(String args[]) {
		String fechac = Utilidades.sumarDiasAFechas("2023-07-17", 30);
		System.out.println("fechac:" + fechac);
		String fechaUltimoDiaMes = Utilidades.ultimoDiaDeSiguienteMes("2023-07-17");
		System.out.println("fechac:" + fechaUltimoDiaMes);
		String fechaCorteMensual = Utilidades.sumarDiasAFechas(fechaUltimoDiaMes, 5);
		System.out.println("fechac:" + fechaCorteMensual);

	}

}
