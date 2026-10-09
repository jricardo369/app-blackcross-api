package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vazjim.controlasistencias.correo.CorreosAEnviar;
import com.vazjim.controlasistencias.logica.AsistenciaAClaseLogica;
import com.vazjim.controlasistencias.logica.ConfiguracionLogica;
import com.vazjim.controlasistencias.logica.InscripcionLogica;
import com.vazjim.controlasistencias.logica.LogLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.CambioContrasenia;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.modelo.InscripcionUs;
import com.vazjim.controlasistencias.modelo.LogInterno;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("Usuarios")
public class UsuarioServicio {

	//private static Logger log = Logger.getLogger(UsuarioServicio.class);

	String json = "";
	String mensaje = "";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerUsuarios(@QueryParam("sociedad")int sociedad) {

		//log.info("Obtener usuarios");
		UsuarioLogica usuarioObj = new UsuarioLogica();
		List<Usuario> lUsuarios = null;
		try {
			lUsuarios = usuarioObj.obtener(null, null,sociedad,false);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lUsuarios);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}
	
	@GET
	@Path("/entrenadores")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerUsuariosEntrenadores(@QueryParam("sociedad")int sociedad) {

		//log.info("Obtener usuarios entrenadores");
		UsuarioLogica usuarioObj = new UsuarioLogica();
		List<Usuario> lUsuarios = null;
		try {
			lUsuarios = usuarioObj.obtener("id_rol", "3",sociedad,false);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lUsuarios);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@GET
	@Path("/{columna}/{valor}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerUsuarioColumnaYValor(@PathParam("columna") String columna, @PathParam("valor") String valor,@QueryParam("sociedad")int sociedad) {

		//log.info("columna:" + columna + "/valor:" + valor);
		UsuarioLogica usuarioObj = new UsuarioLogica();
		List<Usuario> lUsuarios = null;
		try {
			lUsuarios = usuarioObj.obtener(columna, valor,sociedad,false);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lUsuarios);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}
	
	@GET
	@Path("/usuario-con-mensaje/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerUsuarioConMensaje(@PathParam("idUsuario") String idUsuario, @QueryParam("sociedad")int sociedad) {

		//log.info("idUsuario:" + idUsuario);
		UsuarioLogica usuarioObj = new UsuarioLogica();
		List<Usuario> lUsuarios = null;
		try {
			lUsuarios = usuarioObj.obtener("id_usuario", idUsuario,sociedad,true);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lUsuarios);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String crearUsuario(String JSONusuario) {

		Utilidades util = new Utilidades();

		UsuarioLogica usuarioObj = new UsuarioLogica();
		Gson gSon = new GsonBuilder().create();
		Usuario usuario = new Usuario();

		json = util.isJSONValid(JSONusuario);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		usuario = (Usuario) gSon.fromJson(JSONusuario, Usuario.class);
		try {

			// Obtener usuario
			List<Usuario> ls = usuarioObj.obtener("usuario", usuario.getUsuario(),usuario.getSociedad(),false);

			if (!ls.isEmpty()) {
				return MensajeLogica.obtenerMensajeCompleto("US-EXISTENTE", "ES");
			}

			mensaje = usuarioObj.insertar(usuario);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", mensaje);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String actualizarUsuario(String JSONusuario) {

		Utilidades util = new Utilidades();
		UsuarioLogica usuarioObj = new UsuarioLogica();
		Gson gSon = new GsonBuilder().create();
		Usuario usuario = new Usuario();
		InscripcionLogica insc = new InscripcionLogica();

		json = util.isJSONValid(JSONusuario);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		usuario = (Usuario) gSon.fromJson(JSONusuario, Usuario.class);
		try {
			mensaje = usuarioObj.actualizar(usuario);
			
			if (usuario.getIdRol() == 2) {
				Inscripcion i = insc.obtenerUlimaInscripcionDeUsuario(usuario.getIdUsuario());
				if (i != null) {
					if (!i.getTipoInscripcionDesc().equals("Mensual")) {
						insc.actualizarClasesRestantes(i.getIdInscripcion(), usuario.getClasesRestantes());
					}
				}
			}
			
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", mensaje);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@PUT
	@Path("actualizar-datos-perfil")
	@Produces(MediaType.APPLICATION_JSON)
	public String actualizarDatosUsuarioPerfil(String JSONusuario) {

		//log.info("Actualizar datos perfil usuario");

		Utilidades util = new Utilidades();
		UsuarioLogica usuarioObj = new UsuarioLogica();
		Gson gSon = new GsonBuilder().create();
		Usuario usuario = new Usuario();

		json = util.isJSONValid(JSONusuario);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		usuario = (Usuario) gSon.fromJson(JSONusuario, Usuario.class);

		// Validar que la contraseña anterior sea correcta tomando usuario
		List<Usuario> us;
		//log.info("Usuario:" + usuario.getIdUsuario() + "/Correo:" + usuario.getCorreoElectronico() + "/Tel:"+ usuario.getTelefono());

		try {

			// Validar que el id usuario venga lleno
			if (usuario.getIdUsuario() == 0) {
				return MensajeLogica.obtenerMensajeCompleto("US-IDUSUARIO-VACIO", "ES");
			}
			us = usuarioObj.obtener("id_usuario", String.valueOf(usuario.getIdUsuario()),usuario.getSociedad(),false);
			// Validando que exista el usuario
			if (us.isEmpty()) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			} else {
				// Validando que los datos vengan
				if ("".equals(usuario.getCorreoElectronico()) || "".equals(usuario.getTelefono())) {
					return MensajeLogica.obtenerMensajeCompleto("US-DATOSENTRADA-INCOMPLETOS", "ES");
				} else {
					// Actulizar datos recibidos en el usuario
					mensaje = usuarioObj.actualizarDatosPerfilUsuario(usuario);
					return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", mensaje);
				}
			}
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@PUT
	@Path("cambio-contrasenia")
	@Produces(MediaType.APPLICATION_JSON)
	public String cambioContrasenia(String JSOCambioCont) {

		String mensaje = "";
		boolean validaContrasenia = false;
		Utilidades util = new Utilidades();
		UsuarioLogica usuarioObj = new UsuarioLogica();
		Gson gSon = new GsonBuilder().create();
		CambioContrasenia cambioContrasenia = new CambioContrasenia();

		json = util.isJSONValid(JSOCambioCont);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		cambioContrasenia = (CambioContrasenia) gSon.fromJson(JSOCambioCont, CambioContrasenia.class);
		try {

			// Validar que la contraseña anterior sea correcta tomando usuario
			List<Usuario> usuario = usuarioObj.obtener("id_usuario", String.valueOf(cambioContrasenia.getIdUsuario()),0,false);
			// Validando que exista el usuario
			if (usuario.isEmpty()) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			} else {
				// Validando que la contraseña del usuario sea igual a la
				// contraseña compartida
				if (!usuario.get(0).getContrasenia().equals(cambioContrasenia.getContraseniaAnterior())) {
					return MensajeLogica.obtenerMensajeCompleto("US-CONT-INCORRECTA", "ES");
				}
			}

			Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("TAM-CONTRASENIA");
			/*int tam = 0;
			if (conf != null) {
				tam = Integer.valueOf(conf.getValorArriba());
			} else {
				tam = 11;
			}*/
			//log.info("Tamaño cont: " + cambioContrasenia.getContraseniaNueva().length());
			//log.info("Validando tamanio de contrasenia no sea mayor a " + tam);


			//log.info("Validando nomenclatura contrasenia minimo " + conf.getValorAbajo());
			// Validando nomenclatura de contrasenia
			validaContrasenia = Utilidades.validaNomenclaturaContrasenia(cambioContrasenia.getContraseniaNueva(),
					conf.getValorAbajo(),conf.getValorArriba());
			if (validaContrasenia == false) {
				return MensajeLogica.obtenerMensajeCompletoConParametros("US-NOM-CONTRASENIA", "ES",
						conf.getValorAbajo(), "", "", "");
			}

			mensaje = usuarioObj.cambioContrasenia(cambioContrasenia.getIdUsuario(),
					cambioContrasenia.getContraseniaNueva());
			LogLogica.insertarLogInterno(new LogInterno("I", "CAMBIO-CONTRASENIA",
					String.valueOf(cambioContrasenia.getIdUsuario()), "", ""));
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", mensaje);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@PUT
	@Path("/{usuario}/{estatus}")
	@Produces(MediaType.APPLICATION_JSON)
	public String cambiarEstatus(@PathParam("usuario") int usuario, @PathParam("estatus") String estatus) {

		String mensaje = "";
		UsuarioLogica usuarioObj = new UsuarioLogica();

		try {
			mensaje = usuarioObj.cambiarEstatusUsuario(usuario, estatus, 0);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", mensaje);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@PUT
	@Path("falta-pago/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String cambioEstatusFaltaPago(@PathParam("idUsuario") int idUsuario) {

		String fecha = "";

		UsuarioLogica usuarioObj = new UsuarioLogica();

		try {
			//log.info("Cambiar estatus a usuario:" + idUsuario);
			usuarioObj.cambiarEstatusUsuario(idUsuario, "3", 0);
			fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			//log.info("Limpiando clases posteriores de usuario:" + idUsuario + " en la fecha " + fecha);
			usuarioObj.limpiarClasesPosteriorAFecha(idUsuario, fecha);
			LogLogica.insertarLogInterno(new LogInterno("I", "US-FALTA-PAGO", String.valueOf(idUsuario), "", ""));
		} catch (ParseException e) {
			return MensajeLogica.errorBD(e);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompleto("US-ACTUALIZADO-FALTA-PAGO", "ES");
	}
	
	@PUT
	@Path("borrar-usuario/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String borrarUsuario(@PathParam("idUsuario") int idUsuario) {

		UsuarioLogica usuarioObj = new UsuarioLogica();
		InscripcionLogica inscripcionObj = new InscripcionLogica();

		try {
			Usuario us = usuarioObj.obtenerUsuarioById(idUsuario);
			//log.info("Borrar usuario:" + idUsuario);
			inscripcionObj.eliminarInscripcionesUsuario(idUsuario);
			//log.info("Se borraron inscripciones");
			usuarioObj.limpiarToddasLasClasesUs(idUsuario);
			//log.info("Se borraron asistencias");
			usuarioObj.eliminarUsuario(idUsuario);
			//log.info("Se borro usuario");
			LogLogica.insertarLogInterno(new LogInterno("I", "US-ELIMINADO", String.valueOf(idUsuario), "Eliminado usuario "+us.getNombre(), ""));
		}  catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompleto("US-BORRADO", "ES");
	}

	@GET
	@Path("/{correo}")
	@Produces(MediaType.APPLICATION_JSON)
	public String recuperarContrasenia(@PathParam("correo") String correo,@QueryParam("sociedad")int sociedad) {

		//log.info("Correo:" + correo);
		UsuarioLogica usuarioObj = new UsuarioLogica();
		List<Usuario> lUsuarios = null;
		try {
			lUsuarios = usuarioObj.obtener("correo_electronico", correo,sociedad,false);
			if (!lUsuarios.isEmpty()) {
				//log.info("Se encontro usuario");
				CorreosAEnviar.correoRecuperarContrasenia(lUsuarios.get(0).getCorreoElectronico(),
						lUsuarios.get(0).getUsuario(), lUsuarios.get(0).getContrasenia());
				LogLogica.insertarLogInterno(new LogInterno("I", "REC-CONTRASENIA",
						String.valueOf(lUsuarios.get(0).getIdUsuario()), "", ""));
			} else {
				return MensajeLogica.obtenerMensajeCompleto("US-USU-NOENCONTRADO-CORREO", "ES");
			}

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompleto("US-REC-CONT", "ES");
	}

	@GET
	@Path("bloqueo-por-pago/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String bloqueoPorPago(@PathParam("idUsuario") String idUsuario) {

		
		//log.info("idUsuario:" + idUsuario);
		UsuarioLogica usuarioObj = new UsuarioLogica();
		AsistenciaAClaseLogica asc = null;
		List<Usuario> lUsuarios = null;
		try {
			lUsuarios = usuarioObj.obtener("id_usuario", idUsuario,0,false);
			if (!lUsuarios.isEmpty()) {
				//log.info("Se encontro usuario");
				if (lUsuarios.get(0).getEstatus().equals("3") || lUsuarios.get(0).getEstatus().equals("0")) {
					return MensajeLogica.obtenerMensajeCompleto("US-BLOQ-PAGO", "ES");
				} else {
					if (lUsuarios.get(0).getIdRol() == 2){
						asc = new AsistenciaAClaseLogica();
						InscripcionUs insus = asc.validoInscripcionUsuario(Integer.valueOf(idUsuario),false);
						System.out.println("Es por clases:"+insus.isEsPorClases());
						if (!mensaje.equals("")) {
							return MensajeLogica.obtenerMensajeCompleto("US-SIN-MEMBRESIA", "ES");
						}else{
							return MensajeLogica.obtenerMensajeCompleto("US-SIN-BLOQUEO", "ES");
						}
					}else{
						return MensajeLogica.obtenerMensajeCompleto("US-SIN-BLOQUEO", "ES");
					}
				}

			} else {
				return MensajeLogica.obtenerMensajeCompleto("US-USU-NOENCONTRADO-CORREO", "ES");
			}

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@PUT
	@Path("multas/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String agregarMulta(@PathParam("idUsuario") int idUsuario,
			@QueryParam("idUsuarioModificante") int idUsuarioModificante, @QueryParam("funcion") String funcion,@QueryParam("lugar") int lugar,@QueryParam("idClase") int idClase,@QueryParam("fecha") String fecha) {
		UsuarioLogica usuarioObj = new UsuarioLogica();
		AsistenciaAClaseLogica al = new AsistenciaAClaseLogica();
		String salida = "";
		Usuario u = null;
		int f = 0;
		try {

			//log.info("Agregar una multa a usuario:" + idUsuario);
			List<Usuario> lu = usuarioObj.obtener("id_usuario", "" + idUsuario,0,false);
			if (!lu.isEmpty()) {
				u = lu.get(0);
				if (funcion.equals("I")) {//AGREGAR FALTA NO ACUDIR A CLASE POR CLASE
					f = lu.get(0).getContadorFaltas() + 1;
					usuarioObj.actualizarCampo("contador_faltas", String.valueOf(f), idUsuario);
					al.actualizarAsistencia("asistio", "F", idUsuario, idClase, fecha);
					LogLogica.insertarLogInterno(new LogInterno("I", "US-AGREGARFALTA", String.valueOf(idUsuario), "Se agrego falta a usuario "+ lu.get(0).getNombre() + ", de usuario "+idUsuarioModificante, ""));
					u.setContadorFaltas(f);
				}
				if (funcion.equals("CFC")) {//CANCELAR FALTA EN CLASE
					f = lu.get(0).getContadorFaltas()- 1;
					usuarioObj.actualizarCampo("contador_faltas", String.valueOf(f), idUsuario);
					al.actualizarAsistencia("asistio", "A", idUsuario, idClase, fecha);
					LogLogica.insertarLogInterno(new LogInterno("I", "US-AGREGARFALTA", String.valueOf(idUsuario), "Se quito falta a usuario "+ lu.get(0).getNombre() + ", de usuario "+idUsuarioModificante, ""));
					u.setContadorFaltas(f);
				}
				if (funcion.equals("E")) {//LIMPIAR TODAS LAS MULTAS
					usuarioObj.actualizarCampo("total_multas", "0", idUsuario);
					LogLogica.insertarLogInterno(new LogInterno("I", "US-ELIMINARMULTAS", String.valueOf(idUsuario), "Se limpiaron faltas a usuario "+ lu.get(0).getNombre() + ", de usuario "+idUsuarioModificante, ""));
					u.setTotalMultas(0);
				}
			}
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}
		if (funcion.equals("I")) {
			salida = MensajeLogica.obtenerMensajeCompletoConRespuesta("US-ACTUALIZADO-FALTAS", "ES", u);
		}if (funcion.equals("CFC")) {
			salida = MensajeLogica.obtenerMensajeCompletoConRespuesta("US-ACTUALIZADO-FALTAS", "ES", u);
		}if (funcion.equals("E")) {
			salida = MensajeLogica.obtenerMensajeCompletoConRespuesta("US-ACTUALIZADO-LIMPFALTAS", "ES", u);
		}
		
		return salida;
	}
	
	@PUT
	@Path("faltas/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String restarFalta(@PathParam("idUsuario") int idUsuario,@QueryParam("funcion") String funcion,@QueryParam("idUsuarioModificante") int idUsuarioModificante) {
		UsuarioLogica usuarioObj = new UsuarioLogica();
		Usuario u = null;
		String m = "";
		int f = 0;
		try {
			
			//log.info("Agregar una multa a usuario:" + idUsuario);
			List<Usuario> lu = usuarioObj.obtener("id_usuario", "" + idUsuario,0,false);
				if (!lu.isEmpty()) {
					u = lu.get(0);
				if(funcion.equals("E")){
					f = lu.get(0).getContadorFaltas() - 1;
					m = "elimino";
				}else{
					f = lu.get(0).getContadorFaltas() + 1;
					m = "agrego";
				}
				usuarioObj.actualizarCampo("contador_faltas", String.valueOf(f), idUsuario);
				LogLogica.insertarLogInterno(new LogInterno("I", "US-QUITARAGFALTA", String.valueOf(idUsuario), "Se "+m+" falta a usuario "+ lu.get(0).getNombre() + ", de usuario "+idUsuarioModificante, ""));
				u.setContadorFaltas(f);
			}
			
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}
		if(funcion.equals("E")){
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("US-QUITARFALTA", "ES", u);
		} else {
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("US-AGREGARFALTA", "ES", u);
		}
	}
	
	

}