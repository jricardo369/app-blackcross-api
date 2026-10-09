package com.vazjim.controlasistencias.servicios;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
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
import com.vazjim.controlasistencias.logica.ConfiguracionLogica;
import com.vazjim.controlasistencias.logica.InscripcionLogica;
import com.vazjim.controlasistencias.logica.LogLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.modelo.TipoInscripcion;
import com.vazjim.controlasistencias.modelo.TipoPago;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("Inscripciones")
public class InscripcionServicio {

	//private static Logger log = Logger.getLogger(InscripcionServicio.class);

	String json = "";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerInscripciones(@QueryParam("fecha") String fecha, @QueryParam("idUsuario") int idUsuario,@QueryParam("sociedad") int sociedad) {

		System.out.println("Obtener pagos de " + fecha + " con id usuario:" + idUsuario);
		InscripcionLogica logica = new InscripcionLogica();
		List<Inscripcion> lista = null;

		String fechaf = "";
		
		if (fecha != null && !"".equals(fecha)) {
			fechaf = fecha + " 23:59:00";

			if (fecha != null) {
				fecha = fecha + " 00:00:00";
			}
		}
		if(fecha == null){
			fecha ="";
		}

		try {
			lista = logica.obtener(fecha, fechaf, idUsuario, 0, 0,sociedad);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("INS-OBTENER", "ES", lista);
	}
	
	@GET
	@Path("/{idInscripcion}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerInscripcionPorId(@PathParam("idInscripcion") int idInscripcion) {

		System.out.println("Obtener inscripcion " + idInscripcion);
		InscripcionLogica logica = new InscripcionLogica();
		Inscripcion s = null;

		

		try {
			s = logica.obtenerInscripcionPorId(idInscripcion);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("INS-OBTENER", "ES", s);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String insertarInscripcion(String JSONEntrada) {

		Utilidades util = new Utilidades();

		InscripcionLogica logica = new InscripcionLogica();
		Gson gSon = new GsonBuilder().create();
		Inscripcion obj = new Inscripcion();
		String mensaje = "";
		Configuracion confDiasCorte = ConfiguracionLogica.obtenerConIdentificador("DIAS-CORTE-CLASES");
		int dc = 0;

		json = util.isJSONValid(JSONEntrada);
		System.out.println("json:"+json);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		obj = (Inscripcion) gSon.fromJson(JSONEntrada, Inscripcion.class);
		try {
			
			Inscripcion ins = logica.obtenerUlimaInscripcionDeUsuario(obj.getIdUsuario());
			
			if (ins != null) {
				
				Date fechaActual = new Date();
				Date fechaCorte = Utilidades.cadenaToDate(ins.getFechaCorte());
				
				if (fechaCorte.after(fechaActual)) {
					
					if (!ins.getTipoInscripcionDesc().contains("Mes")) {
						if (ins.getClases_restantes() != 0) {
							return MensajeLogica.obtenerMensajeCompleto("INSC-INSEXISTEACTUAL", "ES");
						}
					}else{
						
						Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("PERM-INSCRIPCION-MENSUAL");
						int pim = Integer.valueOf(conf.getValorAbajo());
						System.out.println("fechaActual:"+fechaActual);
						dc = Integer.valueOf(confDiasCorte.getValorAbajo());
						System.out.println("dc:"+dc);
						System.out.println("fechaCorte:"+ins.getFechaCorte());
						String fechaCorteMensual = Utilidades.restarDiasAFechas(ins.getFechaCorte(), dc);
						//String fechaCorteMensual = ins.getFechaCorte();
						System.out.println("fechaCorteMensual:"+fechaCorteMensual);
						String fechaAc = Utilidades.generarFecha(true, false, false, "", 0, null);
						System.out.println("fechaAc: "+fechaAc);
						System.out.println("Diferencia de dias: "+Utilidades.diferenciaDias(fechaAc, fechaCorteMensual));
						System.out.println("Permitido inscripcion mensual: "+pim);
						System.out.println("Dif dias: "+Utilidades.diferenciaDias(fechaAc, fechaCorteMensual));
						if (Utilidades.diferenciaDias(fechaAc, fechaCorteMensual) > pim) {
							return MensajeLogica.obtenerMensajeCompleto("INSC-INSEXISTEACTUAL", "ES");
						}
						
						
					}
				}
			}
			
			mensaje = logica.insertar(obj);
			
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", mensaje);
	}
	
	@DELETE
	@Path("/{idInscripcion}")
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminarInscripcion(@PathParam("idInscripcion") int idInscripcion,@QueryParam("idUsuario") int idUsuario) {
		
		InscripcionLogica logica = new InscripcionLogica();
		boolean correcto = false;
		String mensaje = "";
		
		try {
			Inscripcion ins = logica.obtenerInscripcionPorId(idInscripcion);
			correcto = logica.eliminarInscripcion(idInscripcion);
			LogLogica.insertarLogInternoAccion("I", MensajeLogica.obtenerMensajeDesc("INS-ELIMINAR", "ES") + " de usuario " + ins.getIdUsuario(), idUsuario );
			
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		if(correcto){
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("INS-ELIMINAR", "ES", "");
		}else{
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", mensaje);
		}
		
	}
	
	@GET()
	@Path("tipos-inscripcion")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerTiposInscripcion(@QueryParam("sociedad") int sociedad) {

		//log.info("Obtener tipos de inscripcion");
		InscripcionLogica logica = new InscripcionLogica();
		List<TipoInscripcion> lista = null;

		try {
			lista = logica.obtenerTiposInscripcion(sociedad);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "ES", lista);
	}
	
	@GET()
	@Path("tipos-pago")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerTiposPago(@QueryParam("sociedad") int sociedad) {

		//log.info("Obtener tipos de pago");
		InscripcionLogica logica = new InscripcionLogica();
		List<TipoPago> lista = null;

		try {
			lista = logica.obtenerTiposPagos(sociedad);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "ES", lista);
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public void actulizarDeExcel(@QueryParam("ruta") String ruta) {
		try {
			//"/Users/joser.vazquez/Downloads/UsuariosIRodaDTest.xlsx"
			Utilidades.leerXlsx(ruta);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


}