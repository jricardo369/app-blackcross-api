package com.vazjim.controlasistencias.correo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Correo;

public class CorreosAEnviar {
	
	private static Logger log = Logger.getLogger(CorreosAEnviar.class);
	
	public static boolean correoRecuperarContrasenia(String correo,String usuario,String contrasenia){
		boolean salida = true;
		CorreoElectronico e = new CorreoElectronico();
		e.setEmail(correo);
		Correo correoObj = obtenerCorreo("RECU-CONTRASENIA","ES"); 
		e.setCabecera(correoObj.getAsunto());
		String mensaje = correoObj.getMensaje().replace("P1", usuario.toUpperCase()).replace("P2", contrasenia).replace("FIRMA", firma());
//		String mensaje = 
//			  "<body>"+
//				  "<h3>Recuperar contraseña</h3>"+
//				   "<p>Hemos recibido una petición para envió de su contraseña.</p>"+
//				   "<p>Tus datos para ingresar son:</p>"+
//				   "<p>"+
//				     "<strong>Usuario:</strong><br>"+
//				     "<a>"+usuario.toUpperCase()+"</a><br>"+
//				     "<strong>Contraseña:</strong><br>"+
//				     "<a>"+contrasenia+"</a>"+	
//				   "</p>"+
//				   "<p>No es necesario contestar este mensaje</p>"+firma()+
//			  "</body>";
		e.setMensaje(mensaje);
		e.setArchivo(null);
		e.enviarCorreoElectronicoAsincronamente();
		return salida;
	}
	
	public static boolean correoUsuarioNuevo(String correo,String usuario,String contrasenia,String nombre){
		boolean salida = true;
		CorreoElectronico e = new CorreoElectronico();
		e.setEmail(correo);
		Correo correoObj = obtenerCorreo("USUARIO-NUEVO","ES"); 
		e.setCabecera(correoObj.getAsunto());
		String mensaje = correoObj.getMensaje().replace("P1", usuario.toUpperCase()).replace("P2", contrasenia).replace("FIRMA", firma());
//			  "<body>"+
//				  "<h3>Bienvenido "+nombre+", se ha creado tu usuario para ingresar a la aplicación de BlackCross</h3>"+
//				   "<p>Tus datos para poder ingresar son:</p>"+
//				   "<p>"+
//				     "<strong>Usuario:</strong><br>"+
//				     "<a>"+usuario.toUpperCase()+"</a><br>"+
//				     "<strong>Contraseña:</strong><br>"+
//				     "<a>"+contrasenia+"</a>"+	
//				   "</p>"+
//				   "<p>No es necesario contestar este mensaje</p>"+firma()+
//			  "</body>";
		e.setMensaje(mensaje);
		e.setArchivo(null);
		e.enviarCorreoElectronicoAsincronamente();
		return salida;
	}
	
	public static String firma(){
		Correo correoObj = obtenerCorreo("FIRMA","ES"); 
		String salida = correoObj.getMensaje();
//		String salida = "<div>"+
//			"<table style=\"\">"+
//				"<tbody>"+
//					"<tr>"+
//					"<td style=\"border-right: none; border-left: none;\" align=\"left\" valign=\"left\" width=\"170\" height=\"170\"><a href=\"[ENLACE DE TU SITIO WEB]\" target=\"_blank\" rel=\"noopener\">"
//					+ "<img style=\"padding-top: 2px;\" src=\"http://3.133.28.198:8080/imgs/blackcrossBlack.png\" width=\"150\" height=\"150\" /></a></td>"+
//					"<td style=\"padding-left: 2px; font-family: Helvetica, Arial, sans-serif; font-size: 13px; border-left: none; border-right: none; line-height: 16px;\" valign=\"center\" width=\"430\" height=\"170\">"+
//					"<p style=\"font-size: 18px;\"><strong>BLACKCROSS</strong></p>"+
//					"<p style=\"line-height: 4px;\">Pedro moreno # 97</p>"+
//					"<p style=\"line-height: 4px;\">La Piedad Michoac&aacute;n</p>"+
//					"<p><strong>S&iacute;guenos en:</strong></p>"+
//					"<a href=\"https://www.facebook.com/Black-Cross-LP-323898808141142\" target=\"_blank\" rel=\"noopener\"><img src=\"http://3.133.28.198:8080/imgs/facebook.png\" width=\"30\" height=\"30\" /></a> <a href=\"https://www.instagram.com/blackcrosslp/?hl=en\" target=\"_blank\" rel=\"noopener\"><img src=\"http://3.133.28.198:8080/imgs/instagram.png\" width=\"30\" height=\"30\" /></a></td>"+
//					"</tr>"+
//				"</tbody>"+
//			"</table>"+
//			"</div>";
		return salida;
	}
	
	public static Correo obtenerCorreo(String codigo, String idioma){
		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		Correo obj = null;

		querySql = "SELECT idCorreo,codigoCorreo,asunto,mensaje,idioma FROM correo  WHERE codigoCorreo = ? AND idioma = ?";

		//log.info("querySql"+querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, codigo);
			st.setString(2, idioma);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				
				obj = new Correo();
				obj.setIdCorreo(rs.getInt(1));
				obj.setCodigoCorreo(rs.getString(2));
				obj.setAsunto(rs.getString(3));
				obj.setMensaje(rs.getString(4));
				obj.setIdioma(rs.getString(5));
				
			}

		} catch (SQLException e) {
			e.printStackTrace();
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
		
		log.info("obteniendo mensaje "+codigo+" en idioma "+idioma);

		return obj;
	}
	
	public static void main(String args []){
		//correoRecuperarContrasenia("jricardo369@gmail.com","JOSER","Jeodf2");
		correoUsuarioNuevo("jricardo369@gmail.com","JOSER","Jeodf2", "Jose Ricardo Vázquez Jiménez");
	}
	

}
