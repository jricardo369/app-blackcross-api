package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;

import com.vazjim.controlasistencias.logica.AsistenciaSitioLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.AsistenciaSitio;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("AsistenciaSitio")
public class AsistenciaSitioServicio {
	
	@POST
	@Path("{usuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String agregarAsistenciaSitio(@PathParam("usuario") String usuario,@QueryParam("sociedad") int sociedad){ 
		
		AsistenciaSitioLogica logica = new AsistenciaSitioLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();
		String fecha;
		try {
			
			fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			List<Usuario> u = logicaUs.obtener("id_usuario", usuario, sociedad, false);
			if (!u.isEmpty()) {
				logica.agregarAsistenciaSitio(u.get(0).getIdUsuario(), fecha,sociedad);
			}else{
				return MensajeLogica.obtenerMensajeCompletoConRespuesta("ASISTENCIA-US-INC", "ES", null);
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
 
		return MensajeLogica.obtenerMensajeCompletoConRespuesta("ASISTENCIA-SITIO-OK", "ES", null);
		
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsuetos(@QueryParam("sociedad") int sociedad) {
		
		AsistenciaSitioLogica logica = new AsistenciaSitioLogica();
		List<AsistenciaSitio> lista = null;
		try {
			lista = logica.obtener(null, "", 0);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lista);
		
	}

}
