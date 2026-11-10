package com.vazjim.controlasistencias.logica;

import java.sql.SQLException;
import java.util.Properties;

import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Propiedades;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class InicioSesionLogica {
	
	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getProperties();

	public Usuario iniciarSesion(String usuario, String contrasenia) throws SQLException {

		UsuarioLogica usuarioObj = new UsuarioLogica();

		return usuarioObj.iniciarSesion(usuario, contrasenia);
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
