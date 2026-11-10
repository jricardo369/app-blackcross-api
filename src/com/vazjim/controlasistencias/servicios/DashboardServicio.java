package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.text.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.logica.DashboardLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.modelo.Dashboard;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("Servicios")
public class DashboardServicio {

	private static Logger log = Logger.getLogger(DashboardServicio.class);

	@GET
	@Path("/datos-dashboard/{fecha}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerDatosDashboard(@PathParam("fecha") String fecha) throws ParseException {

		log.debug("Obtener datos del dashboard de fecha " + fecha);

		DashboardLogica logica = new DashboardLogica();

		try {

			if (fecha == null || "".equals(fecha)) {
				fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			}

			Dashboard dashboard = logica.obtenerDatosDashboard(fecha);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", dashboard);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

}
