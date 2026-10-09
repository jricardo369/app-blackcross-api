package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Propiedades;
import com.vazjim.controlasistencias.utilidades.Utilidades;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class UsuarioLogica {

	private static Logger log = Logger.getLogger(UsuarioLogica.class);
	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getProperties();

	public List<Usuario> obtener(String columna, String valor,int sociedad,boolean conMensaje) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		
		InscripcionLogica ins = new InscripcionLogica();
		AsistenciaAClaseLogica al = null;

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<Usuario> usuarios = new ArrayList<>();
		Usuario usuario;

		querySql = "SELECT id_usuario,id_rol,usuario,contrasenia,nombre,sexo,correo_electronico,peso,altura,imc,telefono,nivel,estatus,intentos,total_multas,contador_faltas,sociedad FROM usuario ";

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
				where = " WHERE " + where + " ORDER BY id_usuario";
			querySql += where;
		}

		System.out.println("query:" + querySql);

		try {

			if (conMensaje) {
				al = new AsistenciaAClaseLogica();
			}
			
			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				usuario = new Usuario();
				usuario.setIdUsuario(rs.getInt(1));
				usuario.setIdRol(rs.getInt(2));
				usuario.setUsuario(rs.getString(3));
				usuario.setContrasenia(rs.getString(4));
				usuario.setNombre(rs.getString(5));
				usuario.setSexo(rs.getString(6));
				usuario.setCorreoElectronico(rs.getString(7));
				usuario.setPeso(rs.getDouble(8));
				usuario.setAltura(rs.getDouble(9));
				usuario.setImc(rs.getString(10));
				usuario.setTelefono(rs.getString(11));
				usuario.setNivel(rs.getString(12));
				usuario.setEstatus(rs.getString(13));
				usuario.setIntentos(rs.getInt(14));
				usuario.setTotalMultas(rs.getInt(15));
				usuario.setContadorFaltas(rs.getInt(16));
				usuario.setSociedad(rs.getInt(17));
				if (rs.getInt(2) == 2) { 
					Inscripcion insc = ins.obtenerUlimaInscripcionDeUsuario(usuario.getIdUsuario());
					if (insc != null) {
						if(insc.getTipoInscripcionDesc().equals("Mensual")){
							usuario.setMembresia("Membresia " + insc.getTipoInscripcionDesc());
							usuario.setMembresiaDesc("Membresia " + insc.getTipoInscripcionDesc() + ", vence el día " + insc.getFechaCorte());
							usuario.setTipoPaquete(insc.getTipoInscripcionDesc());
						}else{
							usuario.setMembresia("Membresia por " + insc.getTipoInscripcionDesc());
							//usuario.setMembresiaDesc("Membresia por " + insc.getTipoInscripcionDesc() + " le quedan " + insc.getClases_restantes() + ", vencen el día " + insc.getFechaCorte());
							usuario.setMembresiaDesc("Membresia por " + insc.getTipoInscripcionDesc() + " le quedan " + insc.getClases_restantes());
							usuario.setClasesRestantes(insc.getClases_restantes());
							usuario.setTipoPaquete(insc.getTipoInscripcionDesc());
						}
						usuario.setVencimiento(insc.getVencimiento());
					} else {
						usuario.setMembresia("Sin membresia");
						usuario.setMembresiaDesc("No cuentas con membresia");
						usuario.setVencimiento("N/A");
					}
				}
				usuario.setIniciales(Utilidades.inicialesNombre(usuario.getNombre()));
				
				if (conMensaje) {
					String m = al.obtenerMensajeUsuario(usuario.getIdUsuario());
					usuario.setMensaje(m);
				}
				

				usuarios.add(usuario);
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

		return usuarios;
	}
	
	public Usuario obtenerUsuarioById(int idUsuario) throws SQLException {
		Usuario us = null;
		List<Usuario> usuarios = obtener("id_usuario", String.valueOf(idUsuario), 0, false);
		if (!usuarios.isEmpty()) {
			us = usuarios.get(0);
		}
		return us;
	}

	public String insertar(Usuario usuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();
		String cee = "";

		querySql = "INSERT INTO usuario(id_rol,usuario,contrasenia,nombre,sexo,correo_electronico,peso,altura,imc,telefono,nivel,estatus,intentos,sociedad) " + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, usuario.getIdRol());
			st.setString(2, usuario.getUsuario());
			st.setString(3, usuario.getContrasenia());
			st.setString(4, usuario.getNombre());
			st.setString(5, usuario.getSexo());
			String ce = Utilidades.quitarCaracteresEsp(usuario.getCorreoElectronico());
			cee = ce.equals("") ? "" : ce;
			st.setString(6, cee);
			st.setDouble(7, usuario.getPeso());
			st.setDouble(8, usuario.getAltura());
			st.setString(9, usuario.getImc());
			st.setString(10, usuario.getTelefono());
			st.setString(11, usuario.getNivel());
			st.setString(12, "1");
			st.setInt(13, 0);
			st.setInt(14, usuario.getSociedad());
			st.executeUpdate();
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
		Mensaje m = MensajeLogica.obtenerMensaje("US-CREADO", "ES");
		return m.getMensaje();
	}

	public String actualizar(Usuario usuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE usuario " + "SET id_rol = ?, usuario = ?, contrasenia = ?, nombre = ?, sexo = ?, correo_electronico = ?, " + "peso = ?,altura = ?,imc = ?,telefono = ?,nivel = ?,estatus = ?,intentos = ? " + "WHERE id_usuario = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, usuario.getIdRol());
			st.setString(2, usuario.getUsuario());
			st.setString(3, usuario.getContrasenia());
			st.setString(4, usuario.getNombre());
			st.setString(5, usuario.getSexo());
			String ce = Utilidades.quitarCaracteresEsp(usuario.getCorreoElectronico());
			st.setString(6, ce);
			st.setDouble(7, usuario.getPeso());
			st.setDouble(8, usuario.getAltura());
			st.setString(9, usuario.getImc());
			st.setString(10, usuario.getTelefono());
			st.setString(11, usuario.getNivel());
			st.setString(12, "1");
			st.setInt(13, 0);
			st.setInt(14, usuario.getIdUsuario());

			st.executeUpdate();
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
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}
	
	public String actualizarDatosPerfilUsuario(Usuario usuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE usuario SET correo_electronico = ?,telefono = ? WHERE id_usuario = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, usuario.getCorreoElectronico());
			st.setString(2, usuario.getTelefono());
			st.setInt(3, usuario.getIdUsuario());

			st.executeUpdate();
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
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}

	public String actualizarCampo(String campo, String valor, int idUsuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE usuario SET " + campo + " = ? WHERE id_usuario = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setString(1, valor);
			st.setInt(2, idUsuario);

			st.executeUpdate();
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
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}

	public String cambiarEstatusUsuario(int idUsuario, String estatus, int intentos) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		if (intentos == 0) {
			querySql = "UPDATE usuario " + "SET estatus = ?, intentos = 0 " + "WHERE id_usuario = ?";
		} else {
			querySql = "UPDATE usuario " + "SET estatus = ? " + "WHERE id_usuario = ?";
		}

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, estatus);
			st.setInt(2, idUsuario);

			st.executeUpdate();
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
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}

	public String cambioContrasenia(int idUsuario, String contrasenia) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE usuario " + "SET contrasenia = ? " + "WHERE id_usuario = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, contrasenia);
			st.setInt(2, idUsuario);

			st.executeUpdate();
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
		Mensaje m = MensajeLogica.obtenerMensaje("US-CONT-CAM", "ES");
		return m.getMensaje();
	}

	public String obtenerContrasenia(int idUsuario) throws SQLException {

		PreparedStatement st = null;
		ResultSet rs = null;
		Connection conn = Conexion.getConnectionDbPool();

		String querySql = "";
		String pass = "";

		querySql = "SELECT password FROM usuario  WHERE id_usuario = ?";

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				pass = rs.getString(1);
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
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		// Haciendo decoding del pass
		byte[] decoded = java.util.Base64.getDecoder().decode(pass);
		pass = new String(decoded);

		return pass;
	}

	/**
	 * Genera un token para el usuario solicitado.
	 * 
	 * @param solicitador
	 * @return un token de tipo <code>String</code>
	 */
	public String generarToken(String solicitador) {
		// Generando el token
		String token = Jwts.builder().setSubject(solicitador).signWith(SignatureAlgorithm.HS256, prop.getProperty("id_sesion").getBytes()).compact();
		return token;
	}

	public boolean usuarioExiste(String usuario) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		int count = 0;
		String querySql = "";
		boolean salida = false;

		querySql = "SELECT id_usuario FROM usuario WHERE usuario = ?;";
		// log.info("querySql"+querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, usuario);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				count = 1;
			}

			if (count == 1) {
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

		log.info("Usuario existe?" + salida);
		return salida;
	}
	
	public String limpiarClasesPosteriorAFecha(int idUsuario,String fecha) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM asistencia_a_clase where id_usuario = ? and fecha >= ?;";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);
			st.setString(2, fecha);

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
	
	public String limpiarToddasLasClasesUs(int idUsuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM asistencia_a_clase where id_usuario = ?;";

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

		Mensaje m = MensajeLogica.obtenerMensaje("CLASES-ELIMINAR", "ES");
		return m.getMensaje();
	}
	
	public String eliminarUsuario(int idUsuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM usuario WHERE id_usuario = ?;";

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

		Mensaje m = MensajeLogica.obtenerMensaje("US-BORRADO", "ES");
		return m.getMensaje();
	}
	
	public String actualizarDeExcel(String query) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = query;
		System.out.println(querySql);
		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.executeUpdate();
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
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}
	

}
