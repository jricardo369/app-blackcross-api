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

import com.vazjim.controlasistencias.logica.AsistenciaAClaseLogica;
import com.vazjim.controlasistencias.logica.ClaseLogica;
import com.vazjim.controlasistencias.logica.ConfiguracionLogica;
import com.vazjim.controlasistencias.logica.InscripcionLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.AsistenciaAClase;
import com.vazjim.controlasistencias.modelo.Clase;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.modelo.InscripcionUs;
import com.vazjim.controlasistencias.modelo.LugaresClaseSeccionado;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("AsistenciaClases")
public class AsistenciaAClaseServicio {

	private static Logger log = Logger.getLogger(AsistenciaAClaseServicio.class);

	@GET
	@Path("/{idClase}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsistenciaDeUsuarios(@PathParam("idClase") int idClase, @QueryParam("fecha") String fecha,@QueryParam("sociedad") int sociedad)
			throws ParseException {

		log.debug("Otener asistencia a clase " + idClase + " de fecha " + fecha);
		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		AsistenciaAClase asistenciaAClase = null;
		if (sociedad == 0) {
			sociedad = 1;
		}

		try {

			if (fecha == null) {
				fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			}
			asistenciaAClase = logica.obtenerAsistenciaAClase(idClase, fecha,sociedad);

			if (asistenciaAClase.getUsuarios().isEmpty()) {
				ClaseLogica cl = new ClaseLogica();
				Clase clase = cl.obtenerClase("id_clase", String.valueOf(idClase),sociedad,false);
				int lugaresDisp = logica.obtenerNumLugaresUsados(idClase, fecha);
				asistenciaAClase.setLugares("Lugares " + lugaresDisp + " / " + clase.getPersonas());
				asistenciaAClase.setClase(clase);
			}
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}
	
	@GET
	@Path("/{idClase}/{idEntrenador}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsistenciaDeUsuariosDeEntrenador(@PathParam("idClase") int idClase,@PathParam("idEntrenador") int idEntrenador, @QueryParam("fecha") String fecha,@QueryParam("sociedad") int sociedad)
			throws ParseException {

		log.debug("Otener asistencia a clase " + idClase + " de entrenador "+idEntrenador +" de fecha " + fecha);
		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		AsistenciaAClase asistenciaAClase = null;

		try {

			if (fecha == null) {
				fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			}
			asistenciaAClase = logica.obtenerAsistenciaAClaseProfesor(idClase, fecha, idEntrenador,sociedad);

			if (asistenciaAClase.getUsuarios().isEmpty()) {
				ClaseLogica cl = new ClaseLogica();
				Clase clase = cl.obtenerClase("id_clase", String.valueOf(idClase),sociedad,true);
				asistenciaAClase.setClase(clase);
			}
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@SuppressWarnings("unused")
	@POST
	@Path("{idClase}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String agregarUsuarioDeClase(@PathParam("idClase") int idClase, @PathParam("idUsuario") int idUsuario,
			@QueryParam("fecha") String fecha,@QueryParam("lugar") String lugar,@QueryParam("sociedad") int sociedad) throws ParseException {

		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();
		String asistenciaAClase = "";
		boolean horarioFueraDeClase = false;
		boolean horarioAntesDeClase = false;

		System.out.println("Agregando asistencia Id clase:" + idClase + " / Id usuario:" + idUsuario + " / Fecha:" + fecha+ " / Lugar:" + lugar);

		if (fecha == null) {
			fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
		}
		try {
			
			//Validar duplicidad en lugar,fecha y clase
			/*boolean vd = logica.lugarOcupado(fecha, lugar, idClase);
			if (vd) {
				return MensajeLogica.obtenerMensajeCompleto("AC-ASISCLASE-DUP", "ES");
			}*/

			// Buscar clase
			ClaseLogica logicaCl = new ClaseLogica();
			Clase c = logicaCl.obtenerClase("id_clase", String.valueOf(idClase),sociedad,true);
			if (c == null) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOXISTE", "ES");
			}
			
			// Tomar si es fecha actual ono
			String fechaRecibida = Utilidades.generarFecha(true, false, false, "", 0, fecha);
			
			//Validacion horario antes de agregar 2025-10-08
			Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("VALIDAR-HORA-PERM-ALTA");
			boolean validarHoraPermAlta = Boolean.valueOf(conf.getValorAbajo());
			//System.out.println("validarHoraPermAlta:"+validarHoraPermAlta);
			if(validarHoraPermAlta){
				
				
				
				String horaYmin = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 5);
				if(idUsuario == 2){
					System.out.println("Datos usuario agregar clase");
					System.out.println("Hora:"+horaYmin+"|Hora clase:"+ c.getHoraInicio()+"|Horario:"+ c.getHorario()+"|Fecha:"+fechaRecibida);
				}
				horarioAntesDeClase = logica.horarioAntesDeClase(horaYmin, c.getHoraInicio(), c.getHorario(),fechaRecibida);
				if(!horarioAntesDeClase){
					log.debug("Clase fuera de horario");
					return MensajeLogica.obtenerMensajeCompletoConParametros("AS-HORAS-PERMITIDAS-ALTA", "ES",
							c.getNombre(), "", "", "");
				}
			}

			// Tomando usuario
			List<Usuario> usuario = logicaUs.obtener("id_usuario", String.valueOf(idUsuario),sociedad,false);
			// Validando que exista el usuario
			log.debug("--- Validando que usuario exista");
			if (usuario.isEmpty()) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			} else {
				// Validando que el usuario este activo
				log.debug("--- Validando que usuario este activo");
				String estatus = usuario.get(0).getEstatus() != null ? usuario.get(0).getEstatus() : "";
				if (estatus.equals("0")) {
					return MensajeLogica.obtenerMensajeCompleto("US-INACTIVO", "ES");
				}
				if (estatus.equals("3")) {
					return MensajeLogica.obtenerMensajeCompleto("US-BLOQUEADO", "ES");
				}
			}

			// Tomando personas permitidas de clase
			log.debug("--- Validando personas permitidas");
			int numPersonasPermitidas = Integer.valueOf(c.getPersonas());
			int numPersonsasActuales = logica.obtenerNumeroDePersonasDeClase(idClase, fecha);
			if (numPersonasPermitidas == numPersonsasActuales) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-CLASELLENA", "ES");
			}

			// Validar que no este dado de alta en la clase
			log.debug("--- Validando que no este dado de alta el usuario en alguna clase en la fecha");
			Clase clase = logica.estaEnClaseEnFecha(fecha, idUsuario);
			if (clase != null) {
				return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-USUARIOENCLASE", "ES",
						clase.getNombre(), Utilidades.diaSemana(fecha), "", "");
			}

			log.debug("--- Comparando si es fecha actual o no");
			int compFechas = Utilidades.compararFechaActualVsFecha(fechaRecibida);
			
			if (compFechas == 0) {
				
				//Validacion fuera de horario si es a las 6 y quiere apartar a las 7 
				String hora = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 2);
				horarioFueraDeClase = logica.horarioFueraDeClase(hora, c.getHoraInicio(), c.getHorario());

				if (horarioFueraDeClase) {
					log.debug("Clase fuera de horario");
					return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-FUERAHORARIO-A", "ES",
							c.getNombre(), "", "", "");
				}
				
			} else if (compFechas == -1) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FECHAANTERIOR", "ES");
			}

			// Validar que tipo de inscripcion tiene
			InscripcionUs insus = logica.validoInscripcionUsuario(Integer.valueOf(idUsuario),false);
			if (!insus.getMensaje().equals("")) {
				return insus.getMensaje();
			}
			
			System.out.println("Es por clases:"+insus.isEsPorClases());
			
			if (insus == null) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FECHAANTERIOR", "ES");
			}

			log.debug("--- Paso todas las validaciones para poder agregarlo a clase");
			
			if(lugar == null){
				lugar = "0";
			}
			
			// Restar en caso que sea por clases
			if (insus.isEsPorClases()) {
				InscripcionLogica insLogica = new InscripcionLogica();
				int clasesRest = insus.getInscripcion().getClases_restantes() - 1;
				insLogica.actualizarClasesRestantes(insus.getInscripcion().getIdInscripcion(), clasesRest);
			}
						
			asistenciaAClase = logica.agregarUsuarioAClase(idUsuario, idClase, fecha,lugar);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@DELETE
	@Path("{idClase}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminarUsuarioAClase(@PathParam("idClase") int idClase, @PathParam("idUsuario") int idUsuario,
			@QueryParam("fecha") String fecha,@QueryParam("sociedad") int sociedad) throws ParseException {

		boolean horarioFueraDeClase = false;

		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		InscripcionLogica ins = new InscripcionLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();
		Usuario us = new Usuario();

		log.debug("Eliminando asistencia Id clase:" + idClase + " / Id usuario:" + idUsuario + " / Fecha:" + fecha);

		if (fecha == null) {
			fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
		}

		String asistenciaAClase = "";
		try {

			// Buscar clase
			ClaseLogica logicaCl = new ClaseLogica();
			Clase c = logicaCl.obtenerClase("id_clase", String.valueOf(idClase),sociedad,true);
			if (c == null) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOXISTE", "ES");
			}

			// Tomando usuario
			List<Usuario> usuario = logicaUs.obtener("id_usuario", String.valueOf(idUsuario),sociedad,false);
			// Validando que exista el usuario
			log.debug("--- Validando que usuario exista");
			if (usuario.isEmpty()) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			} else {
				us = usuario.get(0);
				// Validando que el usuario este activo
				log.debug("--- Validando que usuario este activo");
				String estatus = us.getEstatus() != null ? us.getEstatus() : "";
				if (estatus.equals("0")) {
					return MensajeLogica.obtenerMensajeCompleto("US-INACTIVO", "ES");
				}
			}

			// Validar que no este dado de alta en la clase
			log.debug("--- Validando que no este dado de alta el usuario en alguna clase en la fecha");
			Clase clase = logica.estaEnClaseEnFecha(fecha, idUsuario);
			if (clase != null) {
				if (Integer.valueOf(clase.getIdClase()) != idClase) {
					return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-USUARIOENCLASE", "ES",
							clase.getNombre(), Utilidades.diaSemana(fecha), "", "");
				}
			}
			
			// Validar que tipo de inscripcion tiene 
			InscripcionUs insus = logica.validoInscripcionUsuario(Integer.valueOf(idUsuario),true);
			if (!insus.getMensaje().equals("")) {
				return insus.getMensaje();
			}
			System.out.println("Es por clases:"+insus.isEsPorClases());

			// Tomar si es fecha actual o no
			String fechaRecibida = Utilidades.generarFecha(true, false, false, "", 0, fecha);
			log.debug("--- Comparando si es fecha actual o no");
			int compFechas = Utilidades.compararFechaActualVsFecha(fechaRecibida);

			if (compFechas == 0) {

				String hora = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 2);
				
				log.debug("·····Validar si ya paso el horario de la clase");
				horarioFueraDeClase = logica.horarioFueraDeClase(hora, c.getHoraInicio(), c.getHorario());

				if (horarioFueraDeClase) {
					log.debug("Clase fuera de horario");
					return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-FUERAHORARIO-A", "ES",
							c.getNombre(), "", "", "");
				}
				
				log.debug("·····Validar si se es fuera de clase con horario");
				String horaInicio = c.getHoraInicio();

				log.debug("Total faltas:"+us.getTotalMultas() + " Contador multas:"+us.getContadorFaltas());
				horarioFueraDeClase = logica.horarioParaValFalta(hora, horaInicio, c.getHorario());
				
				Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("TOTAL-FALTAS-PERMITIDAS");
				int totalMultasPermitidas = Integer.valueOf(conf.getValorAbajo());
				log.debug("Total totalMultasPermitidas:"+totalMultasPermitidas);

				log.debug("horarioFueraDeClase:"+horarioFueraDeClase);
				if (horarioFueraDeClase) {
					
					Inscripcion insc = ins.obtenerUlimaInscripcionDeUsuario(us.getIdUsuario());
					if(insc.getTipoInscripcionDesc().equals("Mensual")){
						int cF = us.getContadorFaltas() + 1;
						log.debug("Clase fuera de horario para multa");
						log.debug("Contador Faltas:"+cF);
						boolean val = us.getContadorFaltas() >= totalMultasPermitidas;
						log.debug("Contador Faltas mayor que multas permitidas "+val);
						if (cF >= totalMultasPermitidas) {
							int ct = us.getTotalMultas() + 1;
							logicaUs.actualizarCampo("total_multas", String.valueOf(ct), idUsuario);
							logicaUs.actualizarCampo("contador_faltas", "0", idUsuario);
						}else{
							logicaUs.actualizarCampo("contador_faltas", String.valueOf(cF) , idUsuario);
						}
					}else{
						int cr = insc.getClases_restantes()+1;
						ins.actualizarClasesRestantes(insc.getIdInscripcion(), cr);
					}
					
					//return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FUERAHORARIO-E", "ES");
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
				
				// Restar en caso que sea por clases
				if (insus.isEsPorClases()) {
					InscripcionLogica insLogica = new InscripcionLogica();
					Inscripcion insc = insLogica.obtenerUlimaInscripcionDeUsuario(idUsuario);
					int clasesRest = insc.getClases_restantes() + 1;
					insLogica.actualizarClasesRestantes(insc.getIdInscripcion(), clasesRest);
				}
			}

			log.debug("--- Paso todas las validaciones para poder eliminar");
			asistenciaAClase = logica.eliminarUsuarioAClase(idUsuario, idClase, fecha);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}
	
	@GET
	@Path("/lugares/{idClase}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerLugaresDisponibles(@PathParam("idClase") int idClase, @QueryParam("fecha") String fecha)
			throws ParseException {

		log.debug("Otener lugares disponibles a clase " + idClase + " de fecha " + fecha);
		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();	
		
		LugaresClaseSeccionado lcs = logica.lugaresClase(idClase, fecha);
				
		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lcs);

		

	}
	
	@GET
	@Path("/validar-inscripcion/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String validaInscripcion(@PathParam("idUsuario") int idUsuario)
			throws ParseException {

		log.debug("Validar inscripcion " + idUsuario);
		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();	
		
		InscripcionUs insus = logica.validoInscripcionUsuario(idUsuario, false);
		if (!insus.getMensaje().equals("")) {
			return insus.getMensaje();
		}
		System.out.println("Es por clases:"+insus.isEsPorClases());
				
		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", insus);

		

	}
	
	@GET
	@Path("/validar-eliminar-asistencia")
	@Produces(MediaType.APPLICATION_JSON)
	public String validarBajaAsistencia(@QueryParam("fecha") String fecha,@QueryParam("horaInicio") String horaInicio,@QueryParam("horarioClase") String horarioClase)
			throws ParseException {

		AsistenciaAClaseLogica al = new AsistenciaAClaseLogica();

		boolean horarioFueraPorFalta = false;
		String hora = "";
		String horaYMin = "";
		//hora = "5";
		try {
			hora = Utilidades.generarFecha(false, true, false, "", 0, "2025-10-05").substring(0, 2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		System.out.println("Hora:"+hora+"|HoraYMin:"+horaYMin+"|horaInicio:"+horaInicio+"|horarioClase:"+horarioClase);	
	
		horarioFueraPorFalta = al.horarioParaValFalta(hora, horaInicio, horarioClase);		
		
		System.out.println("Horario fuera de clase:"+horarioFueraPorFalta);	
		
		
				
		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", "");

		

	}
	
	@GET
	@Path("/validar-alta-asistencia")
	@Produces(MediaType.APPLICATION_JSON)
	public String validarAltaAsistencia(@QueryParam("fecha") String fecha, @QueryParam("horaInicio") String horaInicio,
			@QueryParam("horarioClase") String horarioClase,
			@QueryParam("idClase") int idClase)
			throws ParseException {

		StringBuilder salida = new StringBuilder();
		AsistenciaAClaseLogica al = new AsistenciaAClaseLogica();

		boolean horarioAntesDeClase = false;

		

		salida.append("\n--------------------------");
		salida.append("\nVALIDANDO POR ID CLASE\n");

		ClaseLogica logicaCl = new ClaseLogica();

		try {

			Clase c = logicaCl.obtenerClase("id_clase", String.valueOf(idClase), 1, true);
			if (c == null) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOXISTE", "ES");
			}

			// Convertir fecha a String
			String fechaRecibida = Utilidades.generarFecha(true, false, false, "", 0, fecha);
			// Hora y min
			String horaYmin = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 5);
			
			System.out.println("Clase :" + c.getNombre() + "| Hora inicio:" + c.getHoraInicio() + "| Horario:"+ c.getHorario() + "| Hora y min obt:" + horaYmin+ "\n");
			salida.append("Clase :" + c.getNombre() + "| Hora inicio:" + c.getHoraInicio() + "| Horario:"+ c.getHorario() + "| Hora y min obt:" + horaYmin+ "\n");
		
			horarioAntesDeClase = al.horarioAntesDeClase(horaYmin, c.getHoraInicio(), c.getHorario(),fechaRecibida);
			if (!horarioAntesDeClase) {
				log.debug("Clase fuera de horario");
				return MensajeLogica.obtenerMensajeCompletoConParametros("AS-HORAS-PERMITIDAS-ALTA", "ES",
						c.getNombre(), "", "", "");
			}
			

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return salida.toString();

	}

	public static void main(String args[]) throws SQLException {
		
	}

}