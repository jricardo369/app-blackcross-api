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
import com.vazjim.controlasistencias.logica.TipoPaqueteLogica;
import com.vazjim.controlasistencias.modelo.TipoPaquete;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("TipoPaquete")
public class TipoPaqueteServicio {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtener() {

		TipoPaqueteLogica logica = new TipoPaqueteLogica();
		List<TipoPaquete> tipos = null;
		try {
			tipos = logica.obtener(null, null);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", tipos);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@GET
	@Path("/{idTipoPaquete}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerPorId(@PathParam("idTipoPaquete") int idTipoPaquete) {

		TipoPaqueteLogica logica = new TipoPaqueteLogica();
		TipoPaquete tipo = null;
		try {
			tipo = logica.obtenerPorId(idTipoPaquete);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", tipo);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String crear(String JSONTipo) {

		Utilidades util = new Utilidades();

		TipoPaqueteLogica logica = new TipoPaqueteLogica();
		Gson gSon = new GsonBuilder().create();
		TipoPaquete tipo = new TipoPaquete();

		String respuesta = "";
		String json = "";
		try {

			json = util.isJSONValid(JSONTipo);

			if (!json.equals("")) {
				return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
			}

			tipo = (TipoPaquete) gSon.fromJson(JSONTipo, TipoPaquete.class);

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

		TipoPaqueteLogica logica = new TipoPaqueteLogica();
		Gson gSon = new GsonBuilder().create();
		TipoPaquete tipo = new TipoPaquete();

		String respuesta = "";
		String json = "";
		try {

			json = util.isJSONValid(JSONTipo);

			if (!json.equals("")) {
				return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
			}

			tipo = (TipoPaquete) gSon.fromJson(JSONTipo, TipoPaquete.class);
			respuesta = logica.actualizar(tipo);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@DELETE
	@Path("{idTipoPaquete}")
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminar(@PathParam("idTipoPaquete") int idTipoPaquete) {

		TipoPaqueteLogica logica = new TipoPaqueteLogica();

		String respuesta = "";
		try {

			respuesta = logica.eliminar(idTipoPaquete);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

}
