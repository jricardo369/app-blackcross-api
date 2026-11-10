package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.DELETE;
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
import com.vazjim.controlasistencias.logica.ClaseHyroxLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.ClaseHyrox;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("ClasesHyrox")
public class ClaseHyroxServicio {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtener(@QueryParam("sociedad") int sociedad) {

		ClaseHyroxLogica logica = new ClaseHyroxLogica();
		List<ClaseHyrox> clases = null;
		try {
			clases = logica.obtener(null, null, sociedad, false);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", clases);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@GET
	@Path("/activas")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerActivas(@QueryParam("sociedad") int sociedad) {

		ClaseHyroxLogica logica = new ClaseHyroxLogica();
		List<ClaseHyrox> clases = null;
		try {
			clases = logica.obtener(null, null, sociedad, true);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", clases);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@GET
	@Path("/por-fecha/{fecha}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerPorFecha(@PathParam("fecha") String fecha, @QueryParam("sociedad") int sociedad) {

		ClaseHyroxLogica logica = new ClaseHyroxLogica();
		List<ClaseHyrox> clases = null;
		try {
			clases = logica.obtenerPorFecha(fecha, sociedad);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", clases);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@GET
	@Path("/por-fecha-usuario/{fecha}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerPorFechaDeUsuario(@PathParam("fecha") String fecha, @PathParam("idUsuario") int idUsuario, @QueryParam("sociedad") int sociedad) {

		ClaseHyroxLogica logica = new ClaseHyroxLogica();
		List<ClaseHyrox> clases = null;
		try {
			clases = logica.obtenerPorFechaParaUsuario(fecha, idUsuario, sociedad);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", clases);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@GET
	@Path("/{idClaseHyrox}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerPorId(@PathParam("idClaseHyrox") int idClaseHyrox) {

		ClaseHyroxLogica logica = new ClaseHyroxLogica();
		ClaseHyrox clase = null;
		try {
			clase = logica.obtenerPorId(idClaseHyrox);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", clase);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String crear(String JSONClase) {

		Utilidades util = new Utilidades();

		ClaseHyroxLogica logica = new ClaseHyroxLogica();
		Gson gSon = new GsonBuilder().create();
		ClaseHyrox clase = new ClaseHyrox();

		String respuesta = "";
		String json = "";
		try {

			json = util.isJSONValid(JSONClase);

			if (!json.equals("")) {
				return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
			}

			System.out.println("JSON recibido para crear clase Hyrox: " + JSONClase);

			clase = (ClaseHyrox) gSon.fromJson(JSONClase, ClaseHyrox.class);

			System.out.println("Clase parseada - nombre: '" + clase.getNombre() + "', tipoClase: " + clase.getTipoClase() + ", fecha: " + clase.getFecha());

			if (clase.getNombre() == null || clase.getNombre().trim().isEmpty()) {
				return MensajeLogica.obtenerMensajeCompletoConRespuesta("ERROR", "ES", "El campo 'nombre' es obligatorio");
			}

			boolean duplicado = logica.validarDuplicidad(clase.getFecha(), clase.getHoraInicio(), clase.getHoraFin(), clase.getSociedad());

			if (duplicado) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-DULICADA", "ES");
			} else {

				UsuarioLogica us = new UsuarioLogica();
				List<Usuario> lu = us.obtener("id_usuario", clase.getProfesor(), clase.getSociedad(), false);

				if (lu != null) {
					if (!lu.isEmpty()) {
						respuesta = logica.insertar(clase);
						return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);
					} else {
						return MensajeLogica.obtenerMensajeCompleto("CLASE-PRO-NOEXISTE", "ES");
					}

				} else {
					return MensajeLogica.obtenerMensajeCompleto("CLASE-PRO-NOEXISTE", "ES");
				}
			}

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String actualizar(String JSONClase) {

		Utilidades util = new Utilidades();

		ClaseHyroxLogica logica = new ClaseHyroxLogica();
		Gson gSon = new GsonBuilder().create();
		ClaseHyrox clase = new ClaseHyrox();

		String respuesta = "";
		String json = "";
		try {

			json = util.isJSONValid(JSONClase);

			if (!json.equals("")) {
				return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
			}

			clase = (ClaseHyrox) gSon.fromJson(JSONClase, ClaseHyrox.class);
			respuesta = logica.actualizar(clase);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@DELETE
	@Path("{idClaseHyrox}")
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminar(@PathParam("idClaseHyrox") int idClaseHyrox) {

		ClaseHyroxLogica logica = new ClaseHyroxLogica();

		String respuesta = "";
		try {

			respuesta = logica.eliminar(idClaseHyrox);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

}
