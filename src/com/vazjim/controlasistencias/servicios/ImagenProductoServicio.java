package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vazjim.controlasistencias.logica.ImagenProductoLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.modelo.ImagenProducto;
import com.vazjim.controlasistencias.utilidades.FileSystemAdapter;
import com.vazjim.controlasistencias.utilidades.Propiedades;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("Imagenes")
public class ImagenProductoServicio {

	//private static Logger log = Logger.getLogger(ImagenProductoServicio.class);

	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getPropertiesErrores();

	String json = "";
	String respuesta = "";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerImagenes(@QueryParam("idProducto") int idProducto,@QueryParam("sociedad") int sociedad) {
		ImagenProductoLogica impl = new ImagenProductoLogica();
		List<ImagenProducto> lista = null;
		try {
			lista = impl.obtenerImagenesProductos("producto", String.valueOf(idProducto), sociedad,true);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}
		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lista);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String insertarImagenes(String JSONobj,@QueryParam("sociedad") int sociedad) {

		Utilidades util = new Utilidades();

		//System.out.println("json:"+JSONobj);
		ImagenProductoLogica ipl = new ImagenProductoLogica();
		Gson gSon = new GsonBuilder().create();
		ImagenProducto p = new ImagenProducto();
		FileSystemAdapter f = new FileSystemAdapter();

		json = util.isJSONValid(JSONobj);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		p = (ImagenProducto) gSon.fromJson(JSONobj, ImagenProducto.class);
		try {
			
			int prod = 0;
			//log.info("Insertando imagen");
			String a = p.getArchivo();
			System.out.println("a:"+p.getArchivo());
			//byte[] data = DatatypeConverter.parseBase64Binary(a);
			String imgLimiar = a.replaceAll("data:image/png;base64,", a);
			System.out.println("a:"+imgLimiar);
			byte[] data = Base64.getDecoder().decode(imgLimiar);
			prod = p.getProducto();
			System.out.println("producto:"+prod);
			String rutaArchivos = Utilidades.generarRutaArchivo(prod);
			System.out.println("rutaArchivos:"+rutaArchivos);
			String ext =".png";
			String nombre = "/" + UUID.randomUUID() + ext;
			String r = Utilidades.obtenerRutaFinal(rutaArchivos);
			
			f.almacenArchivo(r, data, r+nombre, a);
			p.setSociedad(sociedad);
			p.setUrlImagen(rutaArchivos+nombre);
			int idInserto = ipl.insertar(p);
			
			if(idInserto != 0){
				
				respuesta = MensajeLogica.obtenerMensajeCompleto("IMAGEN-PROD-INS", "ES");
				
			}else{
				
				respuesta = MensajeLogica.obtenerMensajeCompleto("IMAGEN-PROD-INS-ERR", "ES");
			}

		} catch (SQLException e) {
			
			return MensajeLogica.errorBD(e);
			
		}

		return respuesta;
		
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminarImagen(@QueryParam("idImagen") int idImagen) {

		//log.info("idImagen:" + idImagen);
		int salida = 0;

		ImagenProductoLogica ipl = new ImagenProductoLogica();
		try {
			
			FileSystemAdapter f = new FileSystemAdapter();
			ImagenProducto img = ipl.obtenerImagenByIdImagen(idImagen);
			String ruta = Utilidades.obtenerRuta();
			String rutaFinal = ruta + img.getUrlImagen();
			System.out.println("Eliminar:"+rutaFinal);
			f.eliminarArchivo(rutaFinal);
			salida = ipl.eliminar(idImagen);
			if (salida != 0) {
				respuesta = MensajeLogica.obtenerMensajeCompleto("IMAGEN-PROD-ELIM", "ES");
			} else {
				respuesta = MensajeLogica.obtenerMensajeCompleto("IMAGEN-PROD-ELIM-ERR", "ES");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return respuesta;
	}

}