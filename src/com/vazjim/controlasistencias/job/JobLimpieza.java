package com.vazjim.controlasistencias.job;

public class JobLimpieza extends TrabajoProgramado {

	public boolean borrarClasesAtrasParaBorrado() {
		return new com.vazjim.controlasistencias.logica.AsistenciaAClaseLogica().borrarClasesAtrasParaBorrado();
	}

	public boolean borrarClasesAtrasParaBorradoHyrox() {
		return new com.vazjim.controlasistencias.logica.AsistenciaAClaseHyroxLogica().borrarClasesAtrasParaBorrado();
	}

	public boolean borrarInscripcionesAtrasParaBorrado() {
		return new com.vazjim.controlasistencias.logica.InscripcionLogica().borrarInscripcionesAtrasParaBorrado();
	}

	public JobLimpieza() {
		super("limpieza");
	}

	@Override
	protected void ejecutar() {
		JobLimpieza job = new JobLimpieza();
		job.borrarClasesAtrasParaBorrado();
		job.borrarClasesAtrasParaBorradoHyrox();
		job.borrarInscripcionesAtrasParaBorrado();
	}
}
