package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.vazjim.controlasistencias.logica.LogLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.modelo.LogInterno;

@Path("LogInterno")
public class LogInternoServicio {

	//private static Logger log = Logger.getLogger(LogInternoServicio.class);

	String json = "";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerConfiguraciones(@QueryParam("fechai")String fechai,@QueryParam("fechaf")String fechaf,
			@QueryParam("tipo")String tipo,@QueryParam("accion")String accion) {

		//log.info("Obtener log de " + fechai + " a " + fechaf + " con tipo:" + tipo + " acción:" + accion);
		LogLogica logLogica = new LogLogica();
		List<LogInterno> lista = null;

		try {
			lista = logLogica.obtener(fechai, fechaf,tipo,accion);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("LOG-OBTENER", "ES",lista);
	}

}