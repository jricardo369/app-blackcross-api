package com.vazjim.controlasistencias.correo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.utilidades.Propiedades;

public class CorreoElectronico {

	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getProperties();

	private static Logger log = Logger.getLogger(CorreoElectronico.class);

	private String cabecera;
	private String mensaje;
	private String email;
	private byte[] archivo;
	private String tipoArchivo;
	private String nombreArchivo;

	public void enviarCorreoElectronicoAsincronamente() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				envioCorreoElectronico();
			}
		}).start();
	}
	
	public void enviarCorreoElectronicoAsincronamenteOffice() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				envioCorreoElectronicoOffice();
			}
		}).start();
	}

	public static byte[] convertirIStoB(InputStream archivo) {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int stream;
		byte[] data = new byte[16384];

		try {
			while ((stream = archivo.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, stream);
			}

			buffer.flush();

		} catch (IOException e) {
		}

		return buffer.toByteArray();
	}

	public void envioCorreoElectronico() {

		final String correoEnvio = prop.getProperty("correo");
		final String correoEnvioPass = prop.getProperty("correo_pass");

		Properties p = new Properties();
		p.put("mail.smtp.host", "smtp.gmail.com");
		p.put("mail.smtp.port", "465");
		p.put("mail.smtp.auth", "true");
		p.put("mail.smtp.socketFactory.port", "465");
		p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		Session session = Session.getInstance(p, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(correoEnvio, correoEnvioPass);
			}
		});

		try {

			String correo = getEmail();
			String cabecera = getCabecera();
			String mensaje = getMensaje();
			
			InternetAddress[] parse = InternetAddress.parse(correo , true);

			Message message = new MimeMessage(session);
			message.setRecipients(javax.mail.Message.RecipientType.TO,  parse);

			message.setSubject(cabecera);
			message.setText("Dear Mail Crawler," + "\n\n Please do not spam my email!");

			if (archivo == null) {

				log.debug("Correo sin archivo adjunto");

				message.setContent(mensaje, "text/html");

			} else {

				log.debug("Correo con archivo adjunto");

				BodyPart messageBodyPart = new MimeBodyPart();
				BodyPart messageBodyPart2 = new MimeBodyPart();
				Multipart multipart = new MimeMultipart();

				// Agregar los datos adjuntos
				DataSource source = null;

				source = new ByteArrayDataSource(this.archivo, "application/" + this.tipoArchivo);

				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(nombreArchivo);

				messageBodyPart2.setContent(mensaje, "text/html");
				multipart.addBodyPart(messageBodyPart);
				multipart.addBodyPart(messageBodyPart2);

				message.setContent(multipart);

			}

			Transport.send(message);

			log.debug("Enviado!");

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public void envioCorreoElectronicoOffice() {

		final String correoEnvio = "do-not-reply@familiasunidascc.com";
		final String correoEnvioPass = "Romeo.P@$$24";

		Properties p = new Properties();
		p.put("mail.smtp.host", "smtp-mail.outlook.com");
		p.put("mail.smtp.port", "587");
		p.put("mail.smtp.user", correoEnvio);
		p.put("mail.smtp.starttls.enable", "true");
		p.put("mail.smtp.auth", "true");
		p.put("mail.smtp.ssl.trust", "smtp.office365.com");
		p.put("mail.smtp.ssl.trust", "*");
		p.put("mail.smtp.ssl.protocols","TLSv1.2");
		
		
		/*p.put("mail.smtp.starttls.enable", "true");
		p.put("mail.smtp.ssl.trust", "*");
		p.put("mail.smtp.ssl.protocols","TLSv1.2");
		p.put("mail.smtp.socketFactory.fallback", "false");
		p.put("mail.debug", "false");
		p.put("mail.smtp.ssl.enable", "true");*/

		Session session = Session.getInstance(p, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(correoEnvio, correoEnvioPass);
			}
		});

		try {

			String correo = getEmail();
			String cabecera = getCabecera();
			String mensaje = getMensaje();
			
			InternetAddress[] parse = InternetAddress.parse(correo , true);

			Message message = new MimeMessage(session);
			message.setRecipients(javax.mail.Message.RecipientType.TO,  parse);

			message.setSubject(cabecera);
			message.setText("Dear Mail Crawler," + "\n\n Please do not spam my email!");

			if (archivo == null) {

				log.debug("Correo sin archivo adjunto");

				message.setContent(mensaje, "text/html");

			} else {

				log.debug("Correo con archivo adjunto");

				BodyPart messageBodyPart = new MimeBodyPart();
				BodyPart messageBodyPart2 = new MimeBodyPart();
				Multipart multipart = new MimeMultipart();

				// Agregar los datos adjuntos
				DataSource source = null;

				source = new ByteArrayDataSource(this.archivo, "application/" + this.tipoArchivo);

				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(nombreArchivo);

				messageBodyPart2.setContent(mensaje, "text/html");
				multipart.addBodyPart(messageBodyPart);
				multipart.addBodyPart(messageBodyPart2);

				message.setContent(multipart);

			}

			Transport.send(message);

			log.debug("Enviado!");

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	

	public String getCabecera() {
		return cabecera;
	}

	public void setCabecera(String cabecera) {
		this.cabecera = cabecera;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public byte[] getArchivo() {
		return archivo;
	}

	public void setArchivo(byte[] archivo) {
		this.archivo = archivo;
	}

	public String getTipoArchivo() {
		return tipoArchivo;
	}

	public void setTipoArchivo(String tipoArchivo) {
		this.tipoArchivo = tipoArchivo;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public static void main(String[] args) {
		CorreoElectronico e = new CorreoElectronico();
		e.setEmail("jricardo369@gmail.com");
		e.setCabecera("Cambio de contraseña");
		e.setMensaje("Su cambio de contraseña se ha enviado");
		e.setArchivo(null);
		e.enviarCorreoElectronicoAsincronamenteOffice();
	}
}