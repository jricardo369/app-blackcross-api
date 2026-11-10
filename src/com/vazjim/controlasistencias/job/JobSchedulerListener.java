package com.vazjim.controlasistencias.job;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.utilidades.Propiedades;

public class JobSchedulerListener implements ServletContextListener {

	private static final Logger log = Logger.getLogger(JobSchedulerListener.class);

	private ScheduledThreadPoolExecutor scheduler;
	private final List<ScheduledFuture<?>> tareas = new ArrayList<>();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Properties prop = new Propiedades().getProperties();

		List<TrabajoProgramado> trabajos = new ArrayList<>();
		trabajos.add(new JobMembresias());
		trabajos.add(new JobReportes());
		trabajos.add(new JobLimpieza());

		List<TrabajoProgramado> habilitados = new ArrayList<>();
		for (TrabajoProgramado trabajo : trabajos) {
			String valor = prop.getProperty("job." + trabajo.getNombre() + ".enabled", "false");
			if (Boolean.parseBoolean(valor.trim())) {
				habilitados.add(trabajo);
			}
		}

		if (habilitados.isEmpty()) {
			log.info("No hay jobs habilitados");
			return;
		}

		scheduler = new ScheduledThreadPoolExecutor(habilitados.size(), crearThreadFactory());
		scheduler.setRemoveOnCancelPolicy(true);

		for (TrabajoProgramado trabajo : habilitados) {
			try {
				long retrasoInicial = calcularRetrasoInicial(prop, trabajo.getNombre());
				long periodo = TimeUnit.DAYS.toMillis(7);
				ScheduledFuture<?> tarea = scheduler.scheduleAtFixedRate(trabajo, retrasoInicial, periodo,
						TimeUnit.MILLISECONDS);
				tareas.add(tarea);
				log.info("Job " + trabajo.getNombre() + " programado con un retraso inicial de " + retrasoInicial + " ms");
			} catch (Exception e) {
				log.error("No se pudo programar el job " + trabajo.getNombre(), e);
			}
		}

		if (tareas.isEmpty()) {
			scheduler.shutdownNow();
			scheduler = null;
		}
	}

	private ThreadFactory crearThreadFactory() {
		return new ThreadFactory() {
			private final AtomicInteger contador = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "job-" + contador.getAndIncrement());
				thread.setDaemon(false);
				return thread;
			}
		};
	}

	private long calcularRetrasoInicial(Properties prop, String nombre) {
		String prefijo = "job." + nombre;
		ZoneId zona = ZoneId.of(prop.getProperty("job.zona", "America/Mexico_City").trim());
		DayOfWeek dia = DayOfWeek.valueOf(prop.getProperty(prefijo + ".dia").trim().toUpperCase());
		int hora = Integer.parseInt(prop.getProperty(prefijo + ".hora").trim());
		int minuto = Integer.parseInt(prop.getProperty(prefijo + ".minuto", "0").trim());

		ZonedDateTime ahora = ZonedDateTime.now(zona);
		ZonedDateTime proxima = ahora
				.with(TemporalAdjusters.nextOrSame(dia))
				.with(LocalTime.of(hora, minuto));

		if (!proxima.isAfter(ahora)) {
			proxima = proxima.plusWeeks(1);
		}

		return Duration.between(ahora, proxima).toMillis();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		for (ScheduledFuture<?> tarea : tareas) {
			tarea.cancel(true);
		}
		tareas.clear();

		if (scheduler != null) {
			scheduler.shutdownNow();
			try {
				scheduler.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			scheduler = null;
		}
	}
}
