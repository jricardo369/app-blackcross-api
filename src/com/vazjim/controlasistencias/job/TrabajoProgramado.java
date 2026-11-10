package com.vazjim.controlasistencias.job;

import org.apache.log4j.Logger;

public abstract class TrabajoProgramado implements Runnable {

	private static final Logger log = Logger.getLogger(TrabajoProgramado.class);

	private final String nombre;

	protected TrabajoProgramado(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}

	@Override
	public final void run() {
		long inicio = System.currentTimeMillis();
		try {
			log.info("Iniciando job " + nombre);
			ejecutar();
			log.info("Job " + nombre + " finalizado en " + (System.currentTimeMillis() - inicio) + " ms");
		} catch (Throwable t) {
			log.error("Error al ejecutar el job " + nombre, t);
		}
	}

	protected abstract void ejecutar();
}
