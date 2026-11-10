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
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.TipoClaseHyroxLogica;
import com.vazjim.controlasistencias.modelo.TipoClaseHyrox;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("TipoClaseHyrox")
public class TipoClaseHyroxServicio {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtener() {

		TipoClaseHyroxLogica logica = new TipoClaseHyroxLogica();
		List<TipoClaseHyrox> tipos = null;
		try {
			tipos = logica.obtener(null, null);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", tipos);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@GET
	@Path("/{idTipoClaseHyrox}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerPorId(@PathParam("idTipoClaseHyrox") int idTipoClaseHyrox) {

		TipoClaseHyroxLogica logica = new TipoClaseHyroxLogica();
		TipoClaseHyrox tipo = null;
		try {
			tipo = logica.obtenerPorId(idTipoClaseHyrox);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", tipo);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String crear(String JSONTipo) {

		Utilidades util = new Utilidades();

		TipoClaseHyroxLogica logica = new TipoClaseHyroxLogica();
		Gson gSon = new GsonBuilder().create();
		TipoClaseHyrox tipo = new TipoClaseHyrox();

		String respuesta = "";
		String json = "";
		try {

			json = util.isJSONValid(JSONTipo);

			if (!json.equals("")) {
				return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
			}

			tipo = (TipoClaseHyrox) gSon.fromJson(JSONTipo, TipoClaseHyrox.class);

			boolean duplicado = logica.validarDuplicidad(tipo.getDescripcion());

			if (duplicado) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-DULICADA", "ES");
			} else {
				respuesta = logica.insertar(tipo);
				return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);
			}

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String actualizar(String JSONTipo) {

		Utilidades util = new Utilidades();

		TipoClaseHyroxLogica logica = new TipoClaseHyroxLogica();
		Gson gSon = new GsonBuilder().create();
		TipoClaseHyrox tipo = new TipoClaseHyrox();

		String respuesta = "";
		String json = "";
		try {

			json = util.isJSONValid(JSONTipo);

			if (!json.equals("")) {
				return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
			}

			tipo = (TipoClaseHyrox) gSon.fromJson(JSONTipo, TipoClaseHyrox.class);
			respuesta = logica.actualizar(tipo);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@DELETE
	@Path("{idTipoClaseHyrox}")
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminar(@PathParam("idTipoClaseHyrox") int idTipoClaseHyrox) {

		TipoClaseHyroxLogica logica = new TipoClaseHyroxLogica();

		String respuesta = "";
		try {

			respuesta = logica.eliminar(idTipoClaseHyrox);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

}
