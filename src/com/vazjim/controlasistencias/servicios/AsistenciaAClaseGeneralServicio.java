package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.logica.AsistenciaAClaseGeneralLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.AsistenciaAClaseGeneral;
import com.vazjim.controlasistencias.modelo.Usuario;

/**
 * Servicios REST para consultar las asistencias unificadas (clases normales y Hyrox)
 * de un usuario.
 */
@Path("AsistenciaClasesGeneral")
public class AsistenciaAClaseGeneralServicio {

	private static Logger log = Logger.getLogger(AsistenciaAClaseGeneralServicio.class);

	/**
	 * Obtiene las asistencias del usuario en clases normales y Hyrox.
	 *
	 * <p>Los parámetros {@code fechaInicio} y {@code fechaFin} son opcionales
	 * (formato {@code yyyy-MM-dd}) y se podrán utilizar para filtrar por rango
	 * cuando sea necesario.</p>
	 */
	@GET
	@Path("/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsistenciasUsuario(@PathParam("idUsuario") int idUsuario,
			@QueryParam("fechaInicio") String fechaInicio, @QueryParam("fechaFin") String fechaFin) {

		log.debug("Obtener asistencias generales del usuario " + idUsuario);

		AsistenciaAClaseGeneralLogica logica = new AsistenciaAClaseGeneralLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();

		try {

			Usuario usuario = logicaUs.obtenerUsuarioById(idUsuario);
			if (usuario == null) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			}

			String inicio = (fechaInicio == null || fechaInicio.trim().isEmpty()) ? null : fechaInicio.trim();
			String fin = (fechaFin == null || fechaFin.trim().isEmpty()) ? null : fechaFin.trim();

			List<AsistenciaAClaseGeneral> asistencias = logica.obtenerAsistenciasUsuario(idUsuario, inicio, fin);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistencias);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	/**
	 * Obtiene las asistencias del usuario en clases normales y Hyrox.
	 *
	 * <p>Los parámetros {@code fechaInicio} y {@code fechaFin} son opcionales
	 * (formato {@code yyyy-MM-dd}) y se podrán utilizar para filtrar por rango
	 * cuando sea necesario.</p>
	 */
	@GET
	@Path("historico/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerHistoricoAsistenciasUsuario(@PathParam("idUsuario") int idUsuario) {

		log.debug("Obtener asistencias generales del usuario " + idUsuario);

		AsistenciaAClaseGeneralLogica logica = new AsistenciaAClaseGeneralLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();

		try {

			Usuario usuario = logicaUs.obtenerUsuarioById(idUsuario);
			if (usuario == null) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			}

			List<AsistenciaAClaseGeneral> asistencias = logica.obtenerHistoricoAsistenciasUsuario(idUsuario);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistencias);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	/**
	 * Devuelve el total de asistencias del usuario sumando las clases normales y las Hyrox.
	 */
	@GET
	@Path("/contar/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String contarAsistenciasUsuario(@PathParam("idUsuario") int idUsuario) {

		log.debug("Contar asistencias generales del usuario " + idUsuario);

		AsistenciaAClaseGeneralLogica logica = new AsistenciaAClaseGeneralLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();

		try {

			Usuario usuario = logicaUs.obtenerUsuarioById(idUsuario);
			if (usuario == null) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			}

			int total = logica.contarAsistenciasUsuario(idUsuario);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", total);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

}
