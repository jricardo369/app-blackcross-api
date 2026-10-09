package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Propiedades;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class InicioSesionLogica {
	
	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getProperties();

	public Usuario iniciarSesion(String usuario, String contrasenia) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		InscripcionLogica ins = new InscripcionLogica();

		PreparedStatement st = null, stPago = null, stSb = null;
		ResultSet rs = null, rsPago = null, rsSb = null;
		Usuario u = null;

		String querySql = "";

		querySql = "SELECT id_usuario,id_rol,usuario,nombre,sexo,correo_electronico,peso,altura,imc,telefono,nivel,estatus,intentos,sociedad,total_multas,contador_faltas " + "FROM usuario ";

		querySql += " WHERE usuario = ? AND contrasenia = ?";

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, usuario);
			st.setString(2, contrasenia);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				u = new Usuario();
				u.setIdUsuario(rs.getInt(1));
				u.setIdRol(rs.getInt(2));
				u.setUsuario(rs.getString(3));
				u.setNombre(rs.getString(4));
				u.setSexo(rs.getString(5));
				u.setCorreoElectronico(rs.getString(6));
				u.setPeso(rs.getDouble(7));
				u.setAltura(rs.getDouble(8));
				u.setImc(rs.getString(9));
				u.setTelefono(rs.getString(10));
				u.setNivel(rs.getString(11));
				u.setEstatus(rs.getString(12));
				u.setIntentos(rs.getInt(13));
				u.setSociedad(rs.getInt(14));
				u.setTotalMultas(rs.getInt(15));
				u.setContadorFaltas(rs.getInt(16));
				if (rs.getInt(2) == 2) {
					Inscripcion insc = ins.obtenerUlimaInscripcionDeUsuario(u.getIdUsuario());
					if (insc != null) {
						if(insc.getTipoInscripcionDesc().equals("Mensual")){
						u.setMembresia("Membresia " + insc.getTipoInscripcionDesc() + ", vence el día " + insc.getFechaCorte());
						}else{
							u.setMembresia("Membresia de " + insc.getTipoInscripcionDesc() + " quedan " + insc.getClases_restantes() + ", vencen el día " + insc.getFechaCorte());
						}
						u.setVencimiento(insc.getVencimiento());
					} else {
						u.setMembresia("Sin membresia");
						u.setVencimiento("N/A");
					}
				}
				u.setMensaje("");

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
			if (rsPago != null) {
				try {
					rsPago.close();
				} catch (SQLException e) {
					;
				}
				rsPago = null;
			}

			if (rsSb != null) {
				try {
					rsSb.close();
				} catch (SQLException e) {
					;
				}
				rsSb = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (stPago != null) {
				try {
					stPago.close();
				} catch (SQLException e) {
					;
				}
				stPago = null;
			}

			if (stSb != null) {
				try {
					stSb.close();
				} catch (SQLException e) {
					;
				}
				stSb = null;
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

		return u;
	}

	/**
	 * Genera un token para el usuario solicitado.
	 * @param solicitador
	 * @return un token de tipo <code>String</code>
	 */
	public String generarToken(String solicitador){
		//Generando el token
		String token = Jwts.builder()
				.setSubject(solicitador)
				.signWith(SignatureAlgorithm.HS256, prop.getProperty("id_sesion").getBytes())
				.compact();
		return token;
	}
}
