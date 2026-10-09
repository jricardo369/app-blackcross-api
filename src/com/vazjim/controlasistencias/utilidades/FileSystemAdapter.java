package com.vazjim.controlasistencias.utilidades;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class FileSystemAdapter {

	private static Logger log = Logger.getLogger(FileSystemAdapter.class);

	public boolean almacenArchivo(String ruta, byte[] bytes, String nombre,String b64) {

		log.info("Se gardará en : " + ruta);
		new File(ruta).mkdirs();

		boolean respuesta = false;
		OutputStream os = null;
		OutputStream osClone = null;

		try {

			log.info("ruta completa:" + nombre);
			File f = new File(nombre);
			f.createNewFile();

			os = new FileOutputStream(f, false);
			os.write(bytes);
			os.flush();
			os.close();

			respuesta = true;
		} catch (IOException e) {
			e.printStackTrace();
			log.error("ERROR:" + e.getLocalizedMessage());
			respuesta = false;
		} finally {

			try {

				if (os != null)
					os.close();
				if (osClone != null)
					osClone.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

		return respuesta;
	}

	
	public boolean eliminarArchivo(String ruta) {
		boolean respuesta = false;
		File archivo = new File(ruta);
		respuesta = eliminarArchivoYSubArchivos(archivo);
		log.info("Archivo " + archivo.getName() + " borrado: " + respuesta);
		return respuesta;
	}

	private boolean eliminarArchivoYSubArchivos(File f) {
		if (f.isDirectory())
			for (File child : f.listFiles())
				eliminarArchivoYSubArchivos(child);
		return f.delete();
	}

	public File obtenerArchivoDesdeRuta(String rutaRelativa) {
		String rutaArchivosFinal = Utilidades.obtenerRuta();
		/*switch (ambiente) {
		case "qas":
			rutaArchivosFinal = rutaArchivosQAS;
			break;
		case "pro":
			rutaArchivosFinal = rutaArchivosPRO;
			break;
		case "test":
			rutaArchivosFinal = rutaArchivos;
			break;
		}*/
		System.out.println("Obtener archivo de:"+rutaArchivosFinal + "/" + rutaRelativa);
		return new File(rutaArchivosFinal + "/" + rutaRelativa);
	}

	public byte[] obtenerArchivo(String rutaRelativa) {

		File file = obtenerArchivoDesdeRuta(rutaRelativa);

		if (!file.exists()) {
			System.out.println(rutaRelativa + " not found");
		}

		FileInputStream fis = null;
		try {

			fis = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			fis.read(bytes);
			return bytes;

		} catch (IOException e) {

			e.printStackTrace();
			return null;

		} finally {

			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public String generaRutaArchivo(String anio, String usuario, String numero) {
		return "/" + anio + "/" + usuario + "/" + numero;
	}

	public ByteArrayOutputStream generarZip(List<String> srcFiles) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ZipOutputStream zipOut = new ZipOutputStream(bos);
			for (String srcFile : srcFiles) {
				File fileToZip = new File(srcFile);
				FileInputStream fis = new FileInputStream(fileToZip);
				ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
				zipOut.putNextEntry(zipEntry);

				byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}
				fis.close();
			}
			zipOut.close();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bos;
	}

	public static void main(String args[]) {
		String ambiente = "qas";
		String rutaArchivosFinal = "";
		switch (ambiente) {
		case "qas":
			System.out.println("Es QAS");
			rutaArchivosFinal = "QAS";
			break;
		case "pro":
			System.out.println("Es PRO");
			rutaArchivosFinal = "PRO";
			break;
		case "test":
			System.out.println("Es local");
			rutaArchivosFinal = "LOCAL";
			break;
		}
		log.info("rutafinal:" + rutaArchivosFinal);
	}

}
