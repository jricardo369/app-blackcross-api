package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;

import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vazjim.controlasistencias.logica.ImagenProductoLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.ProductoLogica;
import com.vazjim.controlasistencias.modelo.ImagenProducto;
import com.vazjim.controlasistencias.modelo.Producto;
import com.vazjim.controlasistencias.utilidades.FileSystemAdapter;
import com.vazjim.controlasistencias.utilidades.Propiedades;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("Productos")
public class ProductoServicio {

	//private static Logger log = Logger.getLogger(ProductoServicio.class);

	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getPropertiesErrores();

	String json = "";
	String respuesta = "";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerProductos(@QueryParam("sociedad") int sociedad) {
		ProductoLogica asuetoLogica = new ProductoLogica();
		List<Producto> lista = null;
		try {
			lista = asuetoLogica.obtenerProductos(null, null, sociedad, false);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}
		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lista);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String insertarProducto(String JSONobj,@QueryParam("sociedad") int sociedad) {

		Utilidades util = new Utilidades();

		ProductoLogica pl = new ProductoLogica();
		ImagenProductoLogica ipl = new ImagenProductoLogica();
		Gson gSon = new GsonBuilder().create();
		Producto p = new Producto();
		FileSystemAdapter f = new FileSystemAdapter();

		json = util.isJSONValid(JSONobj);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		p = (Producto) gSon.fromJson(JSONobj, Producto.class);
		try {
			
			int idInserto = pl.insertar(p);
			
			if(idInserto != 0){
				
				//log.info("Se inserto producto");
				
				String imgs[] = p.getImagenesArr();
				for (int i = 0; i < imgs.length; i++) {
					  //System.out.println(imgs[i]);
					  
					  //log.info("Insertando imagen");
						String a = imgs[i];
						//System.out.println("a:"+a);
						int ii = a.indexOf(",");
					    a = a.substring(ii + 1);
						//System.out.println("la:"+a);
						byte[] data = DatatypeConverter.parseBase64Binary(a);
						String rutaArchivos = Utilidades.generarRutaArchivo(idInserto);
						System.out.println("rutaArchivos:"+rutaArchivos);
						String ext =".png";
						String nombre = "/" + UUID.randomUUID() + ext;
						String r = Utilidades.obtenerRutaFinal(rutaArchivos);
						
						f.almacenArchivo(r, data, r+nombre, imgs[i]);
						ipl.insertarImagen(rutaArchivos+nombre, sociedad, idInserto);
					}
				
				
				
				respuesta = MensajeLogica.obtenerMensajeCompleto("PROD-INSERTAR", "ES");
				
			}else{
				
				respuesta = MensajeLogica.obtenerMensajeCompleto("PROD-INSERTAR-ERROR", "ES");
			}

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return respuesta;
		
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminarProducto(@QueryParam("idProducto") int idProducto,@QueryParam("sociedad") int sociedad) {

		ProductoLogica prodLogica = new ProductoLogica();
		ImagenProductoLogica imagenProdLogica = new ImagenProductoLogica();
		FileSystemAdapter f = new FileSystemAdapter();
		//log.info("idProducto:" + idProducto);
		int salida = 0;

		try {
				salida = prodLogica.eliminar(idProducto);
				if (salida != 0) {
					
					String r = Utilidades.obtenerRuta();
					List<ImagenProducto> imgs = imagenProdLogica.obtenerImagenesProductos("producto", String.valueOf(idProducto), sociedad,false);
					for (ImagenProducto im : imgs) {
						System.out.println(r+im.getUrlImagen());
						f.eliminarArchivo(r+im.getUrlImagen());
						imagenProdLogica.eliminar(im.getIdImagen());
						
					}
					
					respuesta = MensajeLogica.obtenerMensajeCompleto("PROD-ELIMINAR", "ES");
				}else{
					respuesta = MensajeLogica.obtenerMensajeCompleto("PROD-ELIMINAR-ERR", "ES");
				}
				
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return respuesta;
		
	}

}