package com.vazjim.controlasistencias.utilidades;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Propiedades {

	public Properties getProperties() {

		Properties prop = new Properties();
		String propFileName = "config.properties";

		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			prop.load(inputStream);
			if (inputStream == null) {
				throw new FileNotFoundException("archivo de propiedades '" + propFileName + "' no se encuentra en classpath");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prop;

	}
	
	public Properties getPropertiesErrores() {

		Properties prop = new Properties();
		String propFileName = "msjerrores.properties";

		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			prop.load(inputStream);
			if (inputStream == null) {
				throw new FileNotFoundException("archivo de propiedades '" + propFileName + "' no se encuentra en classpath");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prop;

	}
	
	public static void main(String args[]){
		Propiedades p = new Propiedades();
		Properties pp = p.getProperties();
		String v = pp.getProperty("version");
		System.out.println(v);
	}
	
}
