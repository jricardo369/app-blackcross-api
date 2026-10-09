package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.ImagenProducto;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.modelo.Producto;

public class ProductoLogica {

	//private static Logger log = Logger.getLogger(ProductoLogica.class);

	public List<Producto> obtenerProductos(String columna, String valor, int sociedad,boolean activas) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		ImagenProductoLogica imp = new ImagenProductoLogica();
		
		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<Producto> l = new ArrayList<>();

		querySql = "SELECT p.id_producto,titulo,descripcion_corta,descripcion_larga,costo,id_tipo,fecha_vencimiento,estatus,sociedad FROM producto p ";
				 // +"JOIN tipo_producto u ON u.id_usuario = c.profesor ";

		String where = "";
		
		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " p.sociedad = " + sociedad + " ";
		}
		
		if (activas == true) {
			if (!where.equals(""))
				where += " AND ";
			where += " p.estatus = '1'";
		}
		
		if (columna != null && !"".equals(columna)) {
			if (!where.equals(""))
				where += " AND ";
			where += " " + columna + " = '" + valor + "'";
		}
		
		if (!where.equals("")) {
			if (!where.equals(""))
				where = " WHERE " + where + " ORDER BY p.titulo";
			querySql += where;
		}

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();
			Producto p = null;

			while (rs != null && rs.next()) {

				p = new Producto();
				p.setIdProducto(rs.getInt(1));
				p.setTitulo(rs.getString(2));
				p.setDescripcionCorta(rs.getString(3));
				p.setDescripcionLarga(rs.getString(4));
				p.setCosto(rs.getBigDecimal(5));
				p.setFechaVencimiento(rs.getString(6));
				p.setEstatus(rs.getString(7));
				p.setIdSociedad(rs.getInt(8));
				
				List<ImagenProducto> imgs = imp.obtenerImagenesProductos("producto", String.valueOf(p.getIdProducto()), sociedad,true);
				p.setImagenes(imgs);
			    
				l.add(p);
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

		//log.debug("Productos obt:" + l.size());

		return l;
	}

	

	public Producto obtenerProducto(String columna, String valor,int sociedad,boolean activas) throws SQLException {

		Producto p = null;
		ProductoLogica logicaCl = new ProductoLogica();
		// Buscando clase
		List<Producto> clase = logicaCl.obtenerProductos("id_producto", String.valueOf(valor), sociedad,activas);
		if (!clase.isEmpty()) {
			p = clase.get(0);
		}

		return p;

	}

	

	public int insertar(Producto p) throws SQLException {

		int idInserto = 0;
		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO producto(titulo,descripcion_corta,descripcion_larga,costo,id_tipo,fecha_vencimiento,sociedad,oferta) VALUES(?,?,?,?,?,?,?,?);";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, p.getTitulo());
			st.setString(2, p.getDescripcionCorta());
			st.setString(3, p.getDescripcionLarga());
			st.setBigDecimal(4, p.getCosto());
			st.setInt(5, p.getIdTipo());
			st.setString(6, p.getFechaVencimiento());
			st.setInt(7, p.getIdSociedad());
			st.setInt(8, p.getOferta());
			st.executeUpdate();
			ResultSet rs=st.getGeneratedKeys();
			if(rs.next()){
				idInserto=rs.getInt(1);
			}
			conn.commit();
			
			System.out.println("inserto id:"+idInserto);

		} catch (SQLException e) {
			 e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return idInserto;
	}

	public String actualizar(Producto p) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE producto " + "SET titulo = ?, descripcion_corta = ?, descripcion_larga = ?, costo = ?, id_tipo = ?, fecha_vencimiento = ?, "
				+ "estatus = ?, sociedad = ? WHERE id_producto = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, p.getTitulo());
			st.setString(2, p.getDescripcionCorta());
			st.setString(3, p.getDescripcionLarga());
			st.setBigDecimal(4, p.getCosto());
			st.setInt(5, p.getIdTipo());
			st.setString(6, p.getFechaVencimiento());
			st.setString(7, p.getEstatus());
			st.setInt(8, p.getIdSociedad());
			st.setInt(9, p.getIdProducto());

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

	public int eliminar(int idProducto) throws SQLException {

		int respuesta = 0;
		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM producto WHERE id_producto = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idProducto);

			respuesta = st.executeUpdate();
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

		
		return respuesta;
	}

	public boolean validarDuplicidadProducto(String nombre, String horaInicio, String horaFin,String dia,String horario,int soc) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";
		int count = 0;
		boolean salida = false;

		querySql = "SELECT id_clase FROM clase WHERE nombre = ? and hora_inicio = ? and hora_fin = ? and dia=? and horario = ? and sociedad= ?";

		// log.info("query:"+querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, nombre);
			st.setString(2, horaInicio);
			st.setString(3, horaFin);
			st.setString(4, dia);
			st.setString(5, horario);
			st.setInt(6, soc);
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

		//log.debug("Clase duplicada:" + salida);

		return salida;
	}

}
