package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.logica.AsistenciaAClaseHyroxLogica;
import com.vazjim.controlasistencias.logica.AsistenciaAClaseLogica;
import com.vazjim.controlasistencias.logica.ClaseHyroxLogica;
import com.vazjim.controlasistencias.logica.ConfiguracionLogica;
import com.vazjim.controlasistencias.logica.InscripcionLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.AsistenciaAClaseHyrox;
import com.vazjim.controlasistencias.modelo.Clase;
import com.vazjim.controlasistencias.modelo.ClaseHyrox;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.modelo.InscripcionUs;
import com.vazjim.controlasistencias.modelo.LugaresClaseSeccionado;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

/**
 * Servicios REST de asistencia para las clases Hyrox.
 *
 * <p>Es el equivalente de {@link AsistenciaAClaseServicio} pero trabaja sobre
 * las tablas {@code asistencia_a_clase_hyrox} y {@code clase_hyrox}.</p>
 */
@Path("AsistenciaClasesHyrox")
public class AsistenciaAClaseHyroxServicio {

	private static Logger log = Logger.getLogger(AsistenciaAClaseHyroxServicio.class);

	@GET
	@Path("/{idClaseHyrox}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsistenciaDeUsuarios(@PathParam("idClaseHyrox") int idClaseHyrox,
			@QueryParam("fecha") String fecha, @QueryParam("sociedad") int sociedad) throws ParseException {

		log.debug("Obtener asistencia a clase Hyrox " + idClaseHyrox + " de fecha " + fecha);
		AsistenciaAClaseHyroxLogica logica = new AsistenciaAClaseHyroxLogica();
		AsistenciaAClaseHyrox asistenciaAClase = null;

		if (sociedad == 0) {
			sociedad = 1;
		}

		try {

			if (fecha == null) {
				fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			}

			asistenciaAClase = logica.obtenerAsistenciaAClaseHyrox(idClaseHyrox, fecha, sociedad);

			if (asistenciaAClase.getUsuarios().isEmpty()) {
				ClaseHyroxLogica cl = new ClaseHyroxLogica();
				ClaseHyrox clase = cl.obtenerPorId(idClaseHyrox);
				int lugaresDisp = logica.obtenerNumLugaresUsadosHyrox(idClaseHyrox, fecha);
				int capacidad = clase != null ? parseEntero(clase.getLugares()) : 0;
				asistenciaAClase.setLugares("Lugares " + lugaresDisp + " / " + capacidad);
				asistenciaAClase.setClase(clase);
				asistenciaAClase.setFecha(fecha);
			}

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@GET
	@Path("/{idClaseHyrox}/{idEntrenador}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsistenciaDeUsuariosDeEntrenador(@PathParam("idClaseHyrox") int idClaseHyrox,
			@PathParam("idEntrenador") int idEntrenador, @QueryParam("fecha") String fecha,
			@QueryParam("sociedad") int sociedad) throws ParseException {

		log.debug("Obtener asistencia a clase Hyrox " + idClaseHyrox + " de entrenador " + idEntrenador + " de fecha "
				+ fecha);
		AsistenciaAClaseHyroxLogica logica = new AsistenciaAClaseHyroxLogica();
		AsistenciaAClaseHyrox asistenciaAClase = null;

		try {

			if (fecha == null) {
				fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			}

			asistenciaAClase = logica.obtenerAsistenciaAClaseHyroxProfesor(idClaseHyrox, fecha, idEntrenador, sociedad);

			if (asistenciaAClase.getUsuarios().isEmpty()) {
				ClaseHyroxLogica cl = new ClaseHyroxLogica();
				ClaseHyrox clase = cl.obtenerPorId(idClaseHyrox);
				asistenciaAClase.setClase(clase);
				asistenciaAClase.setFecha(fecha);
			}

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	

	@POST
	@Path("{idClaseHyrox}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String agregarUsuarioDeClase(@PathParam("idClaseHyrox") int idClaseHyrox,
			@PathParam("idUsuario") int idUsuario, @QueryParam("fecha") String fecha,
			@QueryParam("lugar") String lugar, @QueryParam("sociedad") int sociedad) throws ParseException {

		try {

			UsuarioLogica logicaUs = new UsuarioLogica();

			// Tomando usuario
			Usuario usuario = logicaUs.obtenerUsuarioConMembresiaYAsistencias(idUsuario);

			// Validando que exista el usuario
			log.debug("--- Validando que usuario exista");
			if (usuario == null) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			} else {
				// Validando que el usuario este activo
				log.debug("--- Validando que usuario este activo");
				String estatus = usuario.getEstatus() != null ? usuario.getEstatus() : "";
				if (estatus.equals("0")) {
					return MensajeLogica.obtenerMensajeCompleto("US-INACTIVO", "ES");
				}
				if (estatus.equals("3")) {
					return MensajeLogica.obtenerMensajeCompleto("US-BLOQUEADO", "ES");
				}
			}

			if(usuario.getTipoPaquete().equals("Hibrido")){
				AsistenciaAClaseLogica asistenciaAClaseCrossfit = new AsistenciaAClaseLogica();
				try {
					Clase c =asistenciaAClaseCrossfit.estaEnClaseEnFecha(fecha,idUsuario);
					if(c != null){
						return MensajeLogica.obtenerMensajeCompleto("ERR-CLASE-HYROX", "ES");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		AsistenciaAClaseHyroxLogica logica = new AsistenciaAClaseHyroxLogica();
		String asistenciaAClase = "";

		log.debug("Agregando asistencia Hyrox Id clase:" + idClaseHyrox + " / Id usuario:" + idUsuario + " / Fecha:"
				+ fecha + " / Lugar:" + lugar);

		if (fecha == null) {
			fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
		}

	

			// Buscar clase Hyrox
			ClaseHyroxLogica logicaCl = new ClaseHyroxLogica();
			ClaseHyrox c = logicaCl.obtenerPorId(idClaseHyrox);
			if (c == null) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOXISTE", "ES");
			}

			// Tomar si es fecha actual o no
			String fechaRecibida = Utilidades.generarFecha(true, false, false, "", 0, fecha);

			// Validacion horario antes de agregar
			Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("VALIDAR-HORA-PERM-ALTA");
			boolean validarHoraPermAlta = conf != null && Boolean.valueOf(conf.getValorAbajo());
			if (validarHoraPermAlta) {

				String horaYmin = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 5);
				if (idUsuario == 2) {
					System.out.println("Datos usuario agregar clase Hyrox");
					System.out.println("Hora:" + horaYmin + "|Hora clase:" + c.getHoraInicio() + "|Horario:"
							+ c.getHorario() + "|Fecha:" + fechaRecibida);
				}

				boolean horarioAntesDeClase = logica.horarioAntesDeClase(horaYmin, c.getHoraInicio(), c.getHorario(),
						fechaRecibida);
				if (!horarioAntesDeClase) {
					log.debug("Clase Hyrox fuera de horario");
					return MensajeLogica.obtenerMensajeCompletoConParametros("AS-HORAS-PERMITIDAS-ALTA", "ES",
							c.getNombre(), "", "", "");
				}
			}

			// Validando capacidad de la clase
			log.debug("--- Validando lugares permitidos");
			int numPersonasPermitidas = parseEntero(c.getLugares());
			int numPersonasActuales = logica.obtenerNumeroDePersonasDeClaseHyrox(idClaseHyrox, fecha);
			if (numPersonasActuales >= numPersonasPermitidas) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-CLASELLENA", "ES");
			}

			// Validar que no este dado de alta en alguna clase en la fecha
			log.debug("--- Validando que no este dado de alta el usuario en alguna clase en la fecha");
			ClaseHyrox clase = logica.estaEnClaseEnFechaHyrox(fecha, idUsuario);
			if (clase != null) {
				return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-USUARIOENCLASE", "ES",
						clase.getNombre(), Utilidades.diaSemana(fecha), "", "");
			}

			log.debug("--- Comparando si es fecha actual o no");
			int compFechas = Utilidades.compararFechaActualVsFecha(fechaRecibida);

			if (compFechas == 0) {

				// Validacion fuera de horario si ya paso la hora de inicio
				String hora = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 2);
				boolean horarioFueraDeClase = logica.horarioFueraDeClase(hora, c.getHoraInicio(), c.getHorario());

				if (horarioFueraDeClase) {
					log.debug("Clase Hyrox fuera de horario");
					return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-FUERAHORARIO-A", "ES",
							c.getNombre(), "", "", "");
				}

			} else if (compFechas == -1) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FECHAANTERIOR", "ES");
			}

			// Validar que tipo de inscripcion tiene
			InscripcionUs insus = logica.validoInscripcionUsuario(idUsuario, false);
			if (insus == null) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-SININSCRIPCION", "ES");
			}
			if (insus.getMensaje() != null && !insus.getMensaje().equals("")) {
				return insus.getMensaje();
			}

			log.debug("--- Paso todas las validaciones para poder agregarlo a clase Hyrox");

			if (lugar == null) {
				lugar = "0";
			}

			// Restar en caso que sea por clases
			if (insus.isEsPorClases()) {
				InscripcionLogica insLogica = new InscripcionLogica();
				int clasesRest = insus.getInscripcion().getClases_restantes() - 1;
				insLogica.actualizarClasesRestantes(insus.getInscripcion().getIdInscripcion(), clasesRest);
			}

			asistenciaAClase = logica.agregarUsuarioAClaseHyrox(idUsuario, idClaseHyrox, fecha, lugar);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@DELETE
	@Path("{idClaseHyrox}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminarUsuarioAClase(@PathParam("idClaseHyrox") int idClaseHyrox,
			@PathParam("idUsuario") int idUsuario, @QueryParam("fecha") String fecha,
			@QueryParam("sociedad") int sociedad) throws ParseException {

		AsistenciaAClaseHyroxLogica logica = new AsistenciaAClaseHyroxLogica();
		InscripcionLogica ins = new InscripcionLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();
		Usuario us = new Usuario();

		log.debug("Eliminando asistencia Hyrox Id clase:" + idClaseHyrox + " / Id usuario:" + idUsuario + " / Fecha:"
				+ fecha);

		if (fecha == null) {
			fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
		}

		String asistenciaAClase = "";

		try {

			// Buscar clase Hyrox
			ClaseHyroxLogica logicaCl = new ClaseHyroxLogica();
			ClaseHyrox c = logicaCl.obtenerPorId(idClaseHyrox);
			if (c == null) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOXISTE", "ES");
			}

			// Tomando usuario
			List<Usuario> usuario = logicaUs.obtener("id_usuario", String.valueOf(idUsuario), sociedad, false);

			log.debug("--- Validando que usuario exista");
			if (usuario.isEmpty()) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			} else {
				us = usuario.get(0);
				log.debug("--- Validando que usuario este activo");
				String estatus = us.getEstatus() != null ? us.getEstatus() : "";
				if (estatus.equals("0")) {
					return MensajeLogica.obtenerMensajeCompleto("US-INACTIVO", "ES");
				}
			}

			// Validar que no este dado de alta en otra clase en la fecha
			log.debug("--- Validando que el usuario no este en otra clase en la fecha");
			ClaseHyrox clase = logica.estaEnClaseEnFechaHyrox(fecha, idUsuario);
			if (clase != null && clase.getIdClaseHyrox() != idClaseHyrox) {
				return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-USUARIOENCLASE", "ES",
						clase.getNombre(), Utilidades.diaSemana(fecha), "", "");
			}

			// Validar que tipo de inscripcion tiene
			InscripcionUs insus = logica.validoInscripcionUsuario(idUsuario, true);
			if (insus == null) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-SININSCRIPCION", "ES");
			}
			if (insus.getMensaje() != null && !insus.getMensaje().equals("")) {
				return insus.getMensaje();
			}

			// Tomar si es fecha actual o no
			String fechaRecibida = Utilidades.generarFecha(true, false, false, "", 0, fecha);
			log.debug("--- Comparando si es fecha actual o no");
			int compFechas = Utilidades.compararFechaActualVsFecha(fechaRecibida);

			if (compFechas == 0) {

				String hora = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 2);

				log.debug("-----Validar si ya paso el horario de la clase Hyrox");
				boolean horarioFueraDeClase = logica.horarioFueraDeClase(hora, c.getHoraInicio(), c.getHorario());

				if (horarioFueraDeClase) {
					log.debug("Clase Hyrox fuera de horario");
					return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-FUERAHORARIO-A", "ES",
							c.getNombre(), "", "", "");
				}

				log.debug("-----Validar si se elimina fuera de clase con horario");
				String horaInicio = c.getHoraInicio();

				log.debug("Total faltas:" + us.getTotalMultas() + " Contador multas:" + us.getContadorFaltas());
				boolean horarioFueraPorFalta = logica.horarioParaValFalta(hora, horaInicio, c.getHorario());

				Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("TOTAL-FALTAS-PERMITIDAS");
				int totalMultasPermitidas = conf != null ? parseEntero(conf.getValorAbajo()) : 0;
				log.debug("Total totalMultasPermitidas:" + totalMultasPermitidas);
				log.debug("horarioFueraPorFalta:" + horarioFueraPorFalta);

				if (horarioFueraPorFalta) {

					Inscripcion insc = ins.obtenerUlimaInscripcionDeUsuario(us.getIdUsuario());
					if (insc.getTipoInscripcionDesc() != null && insc.getTipoInscripcionDesc().equals("Mensual")) {
						int cF = us.getContadorFaltas() + 1;
						log.debug("Clase fuera de horario para multa. Contador Faltas:" + cF);

						if (cF >= totalMultasPermitidas) {
							int ct = us.getTotalMultas() + 1;
							logicaUs.actualizarCampo("total_multas", String.valueOf(ct), idUsuario);
							logicaUs.actualizarCampo("contador_faltas", "0", idUsuario);
						} else {
							logicaUs.actualizarCampo("contador_faltas", String.valueOf(cF), idUsuario);
						}
					} else {
						int cr = insc.getClases_restantes() + 1;
						ins.actualizarClasesRestantes(insc.getIdInscripcion(), cr);
					}

				} else {
					log.debug("Fecha actual y posible eliminar");
					// Sumar en caso que sea por clases
					InscripcionLogica insLogica = new InscripcionLogica();
					Inscripcion insc = insLogica.obtenerUlimaInscripcionDeUsuario(idUsuario);
					int clasesRest = insc.getClases_restantes() + 1;
					insLogica.actualizarClasesRestantes(insc.getIdInscripcion(), clasesRest);
				}

			} else if (compFechas == -1) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FECHAANTERIOR", "ES");
			} else if (compFechas > 0) {

				// Sumar en caso que sea por clases
				if (insus.isEsPorClases()) {
					InscripcionLogica insLogica = new InscripcionLogica();
					Inscripcion insc = insLogica.obtenerUlimaInscripcionDeUsuario(idUsuario);
					int clasesRest = insc.getClases_restantes() + 1;
					insLogica.actualizarClasesRestantes(insc.getIdInscripcion(), clasesRest);
				}
			}

			log.debug("--- Paso todas las validaciones para poder eliminar");
			asistenciaAClase = logica.eliminarUsuarioAClaseHyrox(idUsuario, idClaseHyrox, fecha);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@GET
	@Path("/lugares/{idClaseHyrox}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerLugaresDisponibles(@PathParam("idClaseHyrox") int idClaseHyrox,
			@QueryParam("fecha") String fecha) throws ParseException {

		log.debug("Obtener lugares disponibles a clase Hyrox " + idClaseHyrox + " de fecha " + fecha);
		AsistenciaAClaseHyroxLogica logica = new AsistenciaAClaseHyroxLogica();

		if (fecha == null) {
			fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
		}

		LugaresClaseSeccionado lcs = logica.lugaresClaseHyrox(idClaseHyrox, fecha);

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lcs);

	}

	@GET
	@Path("/validar-inscripcion/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String validaInscripcion(@PathParam("idUsuario") int idUsuario) throws ParseException {

		log.debug("Validar inscripcion " + idUsuario);
		AsistenciaAClaseHyroxLogica logica = new AsistenciaAClaseHyroxLogica();

		InscripcionUs insus = logica.validoInscripcionUsuario(idUsuario, false);
		if (insus != null && insus.getMensaje() != null && !insus.getMensaje().equals("")) {
			return insus.getMensaje();
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", insus);

	}

	@GET
	@Path("/proximas-clases/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerProximasClasesUsuario(@PathParam("idUsuario") int idUsuario) throws ParseException {

		log.debug("Obtener proximas clases Hyrox del usuario " + idUsuario);
		AsistenciaAClaseHyroxLogica logica = new AsistenciaAClaseHyroxLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();

		try {

			Usuario usuario = logicaUs.obtenerUsuarioById(idUsuario);
			if (usuario == null) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			}

			List<ClaseHyrox> clases = logica.obtenerProximasClasesHyroxUsuario(usuario);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", clases);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	private static int parseEntero(String valor) {
		try {
			return valor != null ? Integer.parseInt(valor.trim()) : 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

}
