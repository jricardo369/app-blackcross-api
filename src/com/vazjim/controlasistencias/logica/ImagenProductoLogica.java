package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.ImagenProducto;
import com.vazjim.controlasistencias.utilidades.Utilidades;

public class ImagenProductoLogica {
	
	private static Logger log = Logger.getLogger(ProductoLogica.class);
	
	public List<ImagenProducto> obtenerImagenesProductos(String columna, String valor, int sociedad,boolean urlHost) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		
		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<ImagenProducto> l = new ArrayList<>();

		querySql = "SELECT id_imagen,url_imagen,sociedad,producto FROM imagen_producto p ";

		String where = "";
		
		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " sociedad = " + sociedad + " ";
		}
		
		if (columna != null && !"".equals(columna)) {
			if (!where.equals(""))
				where += " AND ";
			where += " " + columna + " = '" + valor + "'";
		}
		
		if (!where.equals("")) {
			if (!where.equals(""))
				where = " WHERE " + where + " ORDER BY id_imagen";
			querySql += where;
		}

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();
			ImagenProducto p = null;

			while (rs != null && rs.next()) {

				p = new ImagenProducto();
				p.setIdImagen(rs.getInt(1));
				if (urlHost) {
					p.setUrlImagen(Utilidades.obtenerHost()+rs.getString(2));
				}else{
					p.setUrlImagen(rs.getString(2));
				}
				p.setSociedad(rs.getInt(3));
				p.setProducto(rs.getInt(4));
			    
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

		log.info("Productos obt:" + l.size());

		return l;
	}
	
	public int insertarImagen(String url,int sociedad, int producto){
		int salida = 0;
		
		ImagenProducto ip = new ImagenProducto();
		ip.setUrlImagen(url);
		ip.setSociedad(sociedad);
		ip.setProducto(producto);
		
		try {
			insertar(ip);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return salida;
	}
	
	public int insertar(ImagenProducto p) throws SQLException {

		int idInserto = 0;
		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO imagen_producto(url_imagen,sociedad,producto) VALUES(?,?,?)";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, p.getUrlImagen());
			st.setInt(2, p.getSociedad());
			st.setInt(3, p.getProducto());
			st.executeUpdate();
			conn.commit();

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
	
	public int eliminar(int idImagen) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();
		int salida = 0;

		querySql = "DELETE FROM imagen_producto WHERE id_imagen = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idImagen);

			salida = st.executeUpdate();
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
	
	public ImagenProducto obtenerImagenByIdImagen(int idImagen){
		ImagenProducto img = null;
		List<ImagenProducto> imgs;
		try {
			imgs = obtenerImagenesProductos("id_imagen", String.valueOf(idImagen), 0, false);
			if (!imgs.isEmpty()) {
				img = imgs.get(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return img;
	}

}
