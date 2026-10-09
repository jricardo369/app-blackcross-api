package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vazjim.controlasistencias.logica.AsistenciaAClaseLogica;
import com.vazjim.controlasistencias.logica.AsuetoLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.modelo.Asueto;
import com.vazjim.controlasistencias.utilidades.Propiedades;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("Asuetos")
public class AsuetoServicio {

	//private static Logger log = Logger.getLogger(AsuetoServicio.class);

	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getPropertiesErrores();

	String json = "";
	String respuesta = "";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsuetos(@QueryParam("fecha") String fecha,@QueryParam("sociedad") int sociedad) {

		//log.info("Obtener asuetos:" + fecha);
		AsuetoLogica asuetoLogica = new AsuetoLogica();
		List<Asueto> lista = null;
		String f = "";
		if(fecha == null){
			fecha = "";
		}

		try {
			if(!fecha.equals("")){
				f = "fecha";
			}
			lista = asuetoLogica.obtener(f, fecha,sociedad);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lista);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String insertarAsueto(String JSONasueto) {

		Utilidades util = new Utilidades();

		AsuetoLogica asuetoLogica = new AsuetoLogica();
		AsistenciaAClaseLogica asistenciaCLogica = new AsistenciaAClaseLogica();
		Gson gSon = new GsonBuilder().create();
		Asueto asueto = new Asueto();

		json = util.isJSONValid(JSONasueto);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		asueto = (Asueto) gSon.fromJson(JSONasueto, Asueto.class);
		try {
			if (asueto.getTipo() == 1) {
				if (asuetoLogica.asuetoDuplicado(asueto.getIdClase(), asueto.getFecha(),asueto.getSociedad())) {
					return MensajeLogica.obtenerMensajeCompleto("ASUETO-EXISTE", "ES");
				}

				respuesta = asuetoLogica.insertar(asueto);
			} else {
				List<String> fechas = null;
				if (asueto.getTipo() == 1) {
					//log.info("Solo es un día");
					fechas = new ArrayList<>();
					fechas.add(asueto.getFecha());
				} else if (asueto.getTipo() == 7 || asueto.getTipo() == 14) {
					//log.info("Es cada " + asueto.getTipo() + " días");
					fechas = Utilidades.obtenerTodosLosDiasDelAniooAPartirDeFecha(asueto.getFecha(), asueto.getTipo());
				} else {
					return MensajeLogica.obtenerMensajeCompleto("ASUETO-SOLO-DIAS", "ES");
				}
				if (!fechas.isEmpty()) {
					asistenciaCLogica.eliminarUsuarioAClaseVariasFechas(asueto.getIdClase(), fechas);
					respuesta = asuetoLogica.insertarVariosAsuetos(asueto.getIdClase(), fechas,asueto.getSociedad());
				}
			}

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminarUsuario(@QueryParam("idAsueto") int idAsueto, @QueryParam("fecha") String fecha) {

		AsuetoLogica asuetoObj = new AsuetoLogica();
		//log.info("idAsueto:" + idAsueto);
		//log.info("Fecha:" + fecha);
		boolean conError = false;

		if (fecha == null) {
			fecha = "";
		}

		if ("".equals(fecha)) {
			//log.info("la fecha es vacia");
			if (idAsueto == 0) {
				//log.info("el id asueto es vacio");
				conError = true;
			}
		}

		try {
			if (!conError) {
				respuesta = asuetoObj.eliminarAsueto(idAsueto, fecha);
			} else {
				return MensajeLogica.obtenerMensajeCompleto("ERROR-ELIMINAR-ASUETO", "ES");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", respuesta);
	}

}