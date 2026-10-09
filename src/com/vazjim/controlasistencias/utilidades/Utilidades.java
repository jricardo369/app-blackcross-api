package com.vazjim.controlasistencias.utilidades;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vazjim.controlasistencias.logica.ConfiguracionLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.Configuracion;

public class Utilidades {
	
	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getProperties();
	
	final static String hostQAS = prop.getProperty("host-qas");
	final static String hostPRO = prop.getProperty("host-pro");
	final static String host = prop.getProperty("host");
	
	final static String rutaArchivosQAS = prop.getProperty("ruta-archivos-qas");
	final static String rutaArchivosPRO = prop.getProperty("ruta-archivos-pro");
	final static String rutaArchivos = prop.getProperty("ruta-archivos");
	
	final static String ambiente = prop.getProperty("ambiente");

	private static Logger log = Logger.getLogger(Utilidades.class);

	public String isJSONValid(String json) {

		try {

			JsonParser parser = new JsonParser();
			if (!parser.parse(json).isJsonObject())
				return "No es un fromato JSON correcto";

		} catch (JsonSyntaxException jse) {
			return jse.getMessage();
		}

		return "";
	}

	public String isJSONValidArray(String json) {

		try {

			JsonParser parser = new JsonParser();
			if (!parser.parse(json).isJsonArray())
				return "No es un fromato JSON correcto";

		} catch (JsonSyntaxException jse) {
			return jse.getMessage();
		}

		return "";
	}

	public static boolean isNumero(String s) {
		boolean resultado;
		try {
			Integer.parseInt(s);
			resultado = true;
		} catch (NumberFormatException e) {
			resultado = false;
		}
		return resultado;
	}

	public <T> JsonObject generarRespuesta(T respuesta, int codigoStatus, T mensaje, String descripcion) {

		Gson gSon = new GsonBuilder().create();
		JsonElement joRespuesta = gSon.toJsonTree(respuesta);
		JsonElement joCodigo = gSon.toJsonTree(codigoStatus);
		JsonElement joMensaje = gSon.toJsonTree(mensaje);
		JsonElement joDescripcion = gSon.toJsonTree(descripcion);
		JsonObject jo = new JsonObject();

		jo.add("codigo", joCodigo);
		jo.add("mensaje", joMensaje);
		jo.add("descripcion", joDescripcion);
		jo.add("respuesta", joRespuesta);

		return jo;
	}

	/**
	 * Convierte un objeto de tipo <b>Element</b> a un objeto de tipo T
	 * 
	 * @param o
	 *            objeto al que sera parseado <b>Element</b>
	 * @param el
	 *            objeto que contiene los datos del XML
	 * @param tagName
	 *            nombre de etiqueta de XML (opcional)
	 * @param primeraMayuscula
	 *            indica si las etiquetas del XML inician con mayúscula
	 * @param prefijo
	 *            si los elementos del XML contienen un prefijo se agrega aquí
	 * @throws Exception
	 */
	public static void xmlToObject(Object o, Element el, String tagName, boolean primeraMayuscula, String prefijo)
			throws Exception {

		Class<?> clazz = o.getClass();
		NodeList n;
		Element linea;
		String context;
		boolean tagInicial = false;

		if (!"".equals(tagName))
			tagInicial = true;
		for (Field field : clazz.getDeclaredFields()) {
			context = "";
			tagName = tagInicial ? tagName : field.getName();
			try {

				if (primeraMayuscula) {
					tagName = Character.toUpperCase(tagName.charAt(0)) + tagName.substring(1);
				}

				tagName = prefijo + tagName;

				n = el.getElementsByTagName(tagName);
				context = el.getAttribute(field.getName());
				linea = (Element) n.item(0);
				if (linea != null) {
					if (linea.hasChildNodes()) {
						if (!linea.getFirstChild().hasChildNodes())
							context = linea.getFirstChild().getTextContent();
					}
					if (linea.hasAttributes()) {
						context = linea.getAttribute(field.getName());
					}
					field.setAccessible(true);

					if (field.getType().toString().equals(BigDecimal.class.toString())) {
						field.set(o, new BigDecimal(context));
					}
					if (field.getType().toString().equals(int.class.toString())) {
						if (context.equals("")) {
							field.set(o, 0);
						} else {
							field.set(o, new Integer(context));
						}

					}
					if (field.getType().toString().equals(String.class.toString())) {

						field.set(o, context);
						// if (field.getName() == "clave")
						// field.set(o, "FW04740GTN");
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new Exception(e);
			}
		}
	}

	public static String formatearNumero(String numero) {
		DecimalFormat formateador = new DecimalFormat("###,###.00");
		return formateador.format(new Double(numero));
	}

	public static String limpiarCadena(String cadena, boolean limpiarAmperson) {

		String salida = "";
		salida = cadena.trim().replaceAll("\\p{C}", "");
		if (limpiarAmperson) {
			salida = salida.replace("&", "&amp;");
		}

		return salida;

	}
	
	public static String quitarCaracteresEsp(String c){
		return c.replaceAll("[^\\dA-Za-z]", "");
	}

	public static String generarFecha(boolean soloFecha, boolean soloHora, boolean dateTime, String replace,
			int diasASumar, String fechaEntrada) throws ParseException {
		Date d = new Date();
		String fecha = "";
		String hora = "";
		String salida = "";
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("es", "MX"));
		String horapatt = "HH:mm:ss.SSS";
		SimpleDateFormat simpleDateFormatS = new SimpleDateFormat(horapatt, new Locale("es", "MX"));

		if (fechaEntrada != null) {

			Date fechaS = simpleDateFormat.parse(fechaEntrada);

			if (diasASumar == 0) {
				fecha = simpleDateFormat.format(fechaS);
			} else {
				fecha = simpleDateFormat.format(fechaS);
				Calendar c = Calendar.getInstance();
				c.setTime(simpleDateFormat.parse(fecha));
				c.add(Calendar.DATE, diasASumar);
				fecha = simpleDateFormat.format(c.getTime());
			}

			hora = simpleDateFormatS.format(d);

		} else {
			if (diasASumar == 0) {
				fecha = simpleDateFormat.format(d);
			} else {
				fecha = simpleDateFormat.format(d);
				Calendar c = Calendar.getInstance();
				c.setTime(simpleDateFormat.parse(fecha));
				c.add(Calendar.DATE, diasASumar);
				fecha = simpleDateFormat.format(c.getTime());
			}

			hora = simpleDateFormatS.format(d);
		}
		//System.out.println("Hora obt:" + hora);

		if (soloFecha) {
			salida = fecha;
		}
		if (soloHora) {
			salida = hora;
		}
		if (dateTime) {
			salida = fecha + "T" + hora + "Z";
		}
		if (!replace.equals("")) {
			salida = fecha.toString().substring(0, 10).replace(replace, "");
		}

		return salida;
	}

	public static boolean esFinDeSemana(String fecha) {
		// log.info("Fecha:" + fecha);
		boolean salida = false;
		try {
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("es", "MX"));
			Calendar c = Calendar.getInstance();
			c.setTime(simpleDateFormat.parse(fecha));

			int diaFecha = c.get(Calendar.DAY_OF_WEEK);

			if (diaFecha == Calendar.SATURDAY || diaFecha == Calendar.SUNDAY) {
				salida = true;
			} else {
				salida = false;
			}

			fecha = simpleDateFormat.format(c.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return salida;
	}

	public static String diaSemana(String fecha) {
		log.debug("Fecha:" + fecha);
		String salida = "";
		try {
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("es", "MX"));
			Calendar c = Calendar.getInstance();
			c.setTime(simpleDateFormat.parse(fecha));

			int diaFecha = c.get(Calendar.DAY_OF_WEEK);
			log.debug("Día:" + diaFecha);

			if (diaFecha == 2) {
				salida = "LUNES";
			}
			if (diaFecha == 3) {
				salida = "MARTES";
			}
			if (diaFecha == 4) {
				salida = "MIERCOLES";
			}
			if (diaFecha == 5) {
				salida = "JUEVES";
			}
			if (diaFecha == 6) {
				salida = "VIERNES";
			}
			if (diaFecha == 7) {
				salida = "SABADO";
			}
			if (diaFecha == 1) {
				salida = "DOMINGO";
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return salida;
	}

	public static String fechaEnLetra(String fecha) {
		log.debug("Fecha:" + fecha);
		String dia = "";
		String salida = "";
		try {
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("es", "MX"));
			Calendar c = Calendar.getInstance();
			c.setTime(simpleDateFormat.parse(fecha));

			int diaFecha = c.get(Calendar.DAY_OF_WEEK);
			log.debug("Día:" + diaFecha);

			if (diaFecha == 2) {
				dia = "Lunes";
			}
			if (diaFecha == 3) {
				dia = "Martes";
			}
			if (diaFecha == 4) {
				dia = "Miercoles";
			}
			if (diaFecha == 5) {
				dia = "Jueves";
			}
			if (diaFecha == 6) {
				dia = "Viernes";
			}
			if (diaFecha == 7) {
				dia = "Sabado";
			}
			if (diaFecha == 1) {
				dia = "Domingo";
			}

			String mes = fecha.substring(5, 7);
			log.debug("Mes:" + mes);
			switch (mes) {
			case "01":
				mes = "Enero";
				break;
			case "02":
				mes = "Febrero";
				break;
			case "03":
				mes = "Marzo";
				break;
			case "04":
				mes = "Abril";
				break;
			case "05":
				mes = "Mayo";
				break;
			case "06":
				mes = "Junio";
				break;
			case "07":
				mes = "Julio";
				break;
			case "08":
				mes = "Agosto";
				break;
			case "09":
				mes = "Septiembre";
				break;
			case "10":
				mes = "Octubre";
				break;
			case "11":
				mes = "Noviembre";
				break;
			case "12":
				mes = "Diciembre";
				break;

			default:
				break;
			}

			salida = dia + " " + fecha.substring(8, 10) + " de " + mes + " del " + fecha.substring(0, 4);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return salida;
	}

	public static Date cadenaToDate(String f) {
		Date date = null;
		String pattern = "yyyy-MM-dd";
		try {
			date = new SimpleDateFormat(pattern, new Locale("es", "MX")).parse(f);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static int compararFechaActualVsFecha(String fecha2) {
		String fechaActual = "";
		Date fechaAct = null;
		Date fechaComp = null;
		int salida = 0;
		try {
			fechaActual = generarFecha(true, false, false, "", 0, null);
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("es", "MX"));
			fechaAct = simpleDateFormat.parse(fechaActual);
			fechaComp = simpleDateFormat.parse(fecha2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		salida = fechaComp.compareTo(fechaAct);
		log.debug("fecha recibida:" + fecha2);
		log.debug("fecha actual:" + fechaActual);
		if (salida == 0) {
			log.debug("Fechas iguales");
		}
		if (salida > 0) {
			log.debug("La fecha recibida es mayor que la fecha actual");
		}
		if (salida < 0) {
			log.debug("La fecha recibida es menor que la fecha actual");
		}
		return salida;
	}

	public static String completaCeros(String valor, int total) {
		String ceros = "";
		total = total - valor.length();

		for (int i = 0; i < total; i++) {
			ceros += "0";
		}
		valor = ceros + valor;
		return valor;
	}

	public static String convertirHoraA24Hrs(String hora) {
		String salida = "";
		switch (hora) {
		case "1":
			salida = "13";
			break;
		case "2":
			salida = "14";
			break;
		case "3":
			salida = "15";
			break;
		case "4":
			salida = "16";
			break;
		case "5":
			salida = "17";
			break;
		case "6":
			salida = "18";
			break;
		case "7":
			salida = "19";
			break;
		case "8":
			salida = "20";
			break;
		case "9":
			salida = "21";
			break;
		case "10":
			salida = "22";
			break;
		case "11":
			salida = "23";
			break;
		case "12":
			salida = "24";
			break;
		default:
			salida = hora;
			break;
		}
		return salida;
	}
	
	public static String convertirHoraA12Hrs(String hora) {
		String salida = "";
		switch (hora) {
		case "13":
			salida = "1";
			break;
		case "14":
			salida = "2";
			break;
		case "15":
			salida = "3";
			break;
		case "16":
			salida = "4";
			break;
		case "17":
			salida = "5";
			break;
		case "18":
			salida = "6";
			break;
		case "19":
			salida = "7";
			break;
		case "20":
			salida = "8";
			break;
		case "21":
			salida = "9";
			break;
		case "22":
			salida = "10";
			break;
		case "23":
			salida = "11";
			break;
		case "24":
			salida = "12";
			break;
		default:
			salida = hora;
			break;
		}
		return salida;
	}

	public static boolean validaNomenclaturaContrasenia(String contrasenia, String tamanioAbajo,String tamanioArriba) {
		boolean salida = true;
		Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("EXPRESION-CONT");
		String exp = conf.getValorAbajo().replace("P1", tamanioAbajo).replace("P2", tamanioAbajo);
		System.out.println("exp:"+exp);
		String regex = exp;

		if (!Pattern.matches(regex, contrasenia)) {
			salida = false;
		}

		return salida;
	}

	public static String sumarDiasAFechas(String fecha, int dias) {
		String salida = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",new Locale("es", "MX"));
			Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(fecha));
			c.add(Calendar.DATE, dias);
			salida = sdf.format(c.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return salida;
	}
	
	public static String sumarDiasAFechasSinDomingos(String fecha, int dias) {
	    String salida = "";
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "MX"));
	        Calendar c = Calendar.getInstance();
	        c.setTime(sdf.parse(fecha));

	        int diasSumados = 0;

	        while (diasSumados < dias) {
	            c.add(Calendar.DATE, 1); // avanzamos 1 día

	            // Si NO es domingo, lo contamos
	            if (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
	                diasSumados++;
	            }
	        }

	        salida = sdf.format(c.getTime());
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    return salida;
	}
	
	public static String sumarMesesAFecha(String fecha, int meses) {
	    String salida = "";
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "MX"));
	        Calendar c = Calendar.getInstance();
	        c.setTime(sdf.parse(fecha));
	        c.add(Calendar.MONTH, meses); 
	        salida = sdf.format(c.getTime());
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    return salida;
	}
	
	public static String restarDiasAFechas(String fecha, int dias) {
		String salida = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",new Locale("es", "MX"));
			Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(fecha));
			c.add(Calendar.DATE, -dias);
			salida = sdf.format(c.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return salida;
	}
	
	public static String ultimoDiaDeMes(String fecha) {
		String salida = "";
		LocalDate givenDate = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		LocalDate lastDayOfMonthDateGivenDate  = givenDate.withDayOfMonth(
		                                         givenDate.getMonth().length(givenDate.isLeapYear()));
		salida = lastDayOfMonthDateGivenDate+"";
		return salida;
	}

	public static List<String> obtenerTodosLosDiasDelAniooAPartirDeFecha(String fecha, int dias) {
		List<String> list = new ArrayList<>();
		String anio = fecha.substring(0, 4);
		int anioS = Integer.valueOf(anio) + 1;
		list.add(fecha);
		for (;;) {

			cadenaToDate(fecha);
			fecha = sumarDiasAFechas(fecha, dias);
			if (fecha.substring(0, 4).equals(String.valueOf(anioS))) {
				break;
			}
			list.add(fecha);
		}
		return list;
	}
	
	public static String primerDiaDeFecha(String fecha) {
	    int anio = Integer.valueOf(fecha.substring(0, 4));
	    int mes = Integer.valueOf(fecha.substring(5, 7));
	    int dia = Integer.valueOf(fecha.substring(8, 10));

	    LocalDate anyDate = LocalDate.of(anio, mes, dia);
	    LocalDate firstDayOfMonth = anyDate.with(TemporalAdjusters.firstDayOfMonth());

	    return firstDayOfMonth.toString();
	}

	public static String ultimoDiaDeFecha(String fecha) {
		int anio = Integer.valueOf(fecha.substring(0, 4));
		int mes = Integer.valueOf(fecha.substring(5, 7));
		System.out.println("mes:" + mes);
		int dia = Integer.valueOf(fecha.substring(8, 10));
		LocalDate anyDate = LocalDate.of(anio, mes, dia);
		LocalDate lastDayOfMonth = anyDate.with(TemporalAdjusters.lastDayOfMonth());
		System.out.println("Ultimo dia mes:"+lastDayOfMonth);
		return lastDayOfMonth.toString();
	}

	public static String ultimoDiaDeSiguienteMes(String fecha) {
		int anio = Integer.valueOf(fecha.substring(0, 4));
		int mes = Integer.valueOf(fecha.substring(5, 7));
		System.out.println("mes actual:" + mes);
		mes = mes + 1;
		System.out.println("mes:" + mes);
		int dia = Integer.valueOf(fecha.substring(8, 10));
		LocalDate anyDate = LocalDate.of(anio, mes, dia);
		LocalDate lastDayOfMonth = anyDate.with(TemporalAdjusters.lastDayOfMonth());
		System.out.println("Ultimo dia mes:"+lastDayOfMonth);
		return lastDayOfMonth.toString();
	}
	
	public static String ultimoDiaDeMesSumar(String fecha,int sumar) {
		int anio = Integer.valueOf(fecha.substring(0, 4));
		int mes = Integer.valueOf(fecha.substring(5, 7));
		if(sumar == 12){
			anio = anio+1;
		}else{
			mes = mes + sumar;
		}
		System.out.println("mes:" + mes);
		int dia = Integer.valueOf(fecha.substring(8, 10));
		LocalDate anyDate = LocalDate.of(anio, mes, dia);
		LocalDate lastDayOfMonth = anyDate.with(TemporalAdjusters.lastDayOfMonth());
		//System.out.println(lastDayOfMonth);
		return lastDayOfMonth.toString();
	}
	

	public byte[] obtenerImagen() {

		String url = "/Users/joser.vazquez/Downloads/Lugares.png";
		File file = new File(url);
		try {
			byte[] fileContent = FileUtils.readFileToByteArray(file);
			return fileContent;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean guardarArchivo(byte[] archivo) {
		String ruta = "/Users/joser.vazquez/Documents/apache-tomcat-8.0.46/webapps/imgs/lug2.png";
		File f = new File(ruta);
		OutputStream os;
		boolean respuesta = false;
		try {
			os = new FileOutputStream(f);
			os.write(archivo);
			os.flush();
			os.close();
			f.createNewFile();
			respuesta = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return respuesta;
	}
	
	public static String inicialesNombre(String nombre){
		String sTexto = nombre;
		String sPalabra = "";
		String salida = "";
		StringBuilder sb = new StringBuilder();
		
		StringTokenizer stPalabras = new StringTokenizer(sTexto);
		while (stPalabras.hasMoreTokens()) {
			  sPalabra = stPalabras.nextToken();
			  //System.out.println(sPalabra.substring(0,1));
			  sb.append(sPalabra.substring(0,1));
			}
		if(sb.length() > 2){
			salida =  sb.toString().substring(0,2).toUpperCase();
		}else{
			salida = sb.toString().toUpperCase();
		}
		//System.out.println("Salida:"+salida);
		return salida;
	}

	
	
	@SuppressWarnings("deprecation")
	public static void leerXlsx(String ruta) throws IOException {
		
		UsuarioLogica u = new UsuarioLogica();
		
		System.out.println("ruta:"+ruta);
		// obtaining input bytes from a file
		FileInputStream fis = new FileInputStream(new File(ruta));
		// creating workbook instance that refers to .xls file
		@SuppressWarnings("resource")
		XSSFWorkbook wb = new XSSFWorkbook(fis);
		// creating a Sheet object to retrieve the object
		XSSFSheet sheet = wb.getSheetAt(0);
		// evaluating cell type
		FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
		int intUs = 0;
		int intCon = 0;
		int inTi = 0;
		String ins = "";
		int cr = 0;
		String inss = "";
		
		List<String> l1 = new ArrayList<>();
		List<String> l2 = new ArrayList<>();
		
		
		for (Row row : sheet) // iteration over row using for each loop
			
		{
			System.out.println("r:"+row.getRowNum());
			if (row.getRowNum() > 0) {
				
				int i=1;
			
			for (Cell cell : row) // iteration over cell using for each loop
			{
				
				switch (formulaEvaluator.evaluateInCell(cell).getCellType()) {
				case Cell.CELL_TYPE_NUMERIC: // field that represents numeric
												// cell type
					// getting the value of the cell as a number
					if (i == 1) {
						intUs= (int)cell.getNumericCellValue();
					}
					if (i == 4) {
						intCon= (int)cell.getNumericCellValue();
					}
					if (i == 5) {
						inTi= (int)cell.getNumericCellValue();
					}
					if (i == 6) {
						System.out.println("date_"+cell.getDateCellValue());
						Date date = cell.getDateCellValue();
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
						String strDate = dateFormat.format(date); 
						System.out.println("dates_"+strDate);
						ins = "'"+strDate+"'";
					}
					if (i == 7) {
						cr= (int)cell.getNumericCellValue();
					}
					if (i == 8) {
						System.out.println("date_"+cell.getDateCellValue());
						Date date = cell.getDateCellValue();
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
						String strDate = dateFormat.format(date); 
						System.out.println("dates_"+strDate);
						inss ="'"+strDate+"'";
					}
					
					break;
				case Cell.CELL_TYPE_STRING: 
					if (i == 6) {
						System.out.println("date_"+cell.getDateCellValue());
					}
					break;
				}
				
				i++;
			}
			String a = "UPDATE inscripcion set id_tipo_inscripcion ="+inTi+",fecha_inicio="+ins+",clases_restantes="+cr+",fecha_corte="+inss+" WHERE id_usuario = "+intUs+" ";
			l1.add(a);
			String b = "contador_faltas,"+intCon+","+intUs+"";
			l2.add(b);
			}
			
		}
		
		for (String string : l1) {
			System.out.println(string);
			try {
				u.actualizarDeExcel(string);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("------");
		for (String string : l2) {
			System.out.println(string);
			String[] textElements = string.split(",");
			System.out.println(textElements[0]);
			System.out.println(textElements[1]);
			System.out.println(textElements[2]);
			try {
				u.actualizarCampo(textElements[0], textElements[1], Integer.valueOf(textElements[2]));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	public static int diferenciaDias(String fecha1, String fecha2){
		System.out.println("f1:"+fecha1+"/f2:"+fecha2);
		int salida = 0;
		LocalDate dateBefore = LocalDate.parse(fecha1);
		LocalDate dateAfter = LocalDate.parse(fecha2);
		long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
		//System.out.println(noOfDaysBetween);
		salida = (int) noOfDaysBetween;
		return salida;
	}
	
	public static String generarRutaArchivo(int idProducto) {
		//return "/" + Calendar.getInstance().get(Calendar.YEAR) + "/" + idProducto;
		return "/" + idProducto;
	}
	
	public static String obtenerHost(){
		String hostf = "";
		
		//System.out.println("///////////////////ambiente:-" + ambiente + "-");
		//System.out.println("hostQAS:" + hostQAS);
		//System.out.println("hostPRO:" + hostPRO);
		//System.out.println("hostTest:" + host);

		switch (ambiente) {
		case "qas":
			System.out.println("Es QAS");
			hostf = hostQAS;
			break;
		case "pro":
			System.out.println("Es PRO");
			hostf = hostPRO;
			break;
		case "test":
			System.out.println("Es local");
			hostf = host;
			break;
		default:
			System.out.println("No encontro match");
		}

		//System.out.println("Host final:" + rutaArchivosFinal);
		return hostf;
	}
	
	public static String obtenerRuta() {

		//System.out.println("///////////////////ambiente:-" + ambiente + "-");
		//System.out.println("rutaArchivosQAS:" + rutaArchivosQAS);
		//System.out.println("rutaArchivosPRO:" + rutaArchivosPRO);
		//System.out.println("rutaArchivosTest:" + rutaArchivos);
		String rutaArchivosFinal = "";

		switch (ambiente) {
		case "qas":
			System.out.println("Es QAS");
			rutaArchivosFinal = rutaArchivosQAS;
			break;
		case "pro":
			System.out.println("Es PRO");
			rutaArchivosFinal = rutaArchivosPRO;
			break;
		case "test":
			System.out.println("Es local");
			rutaArchivosFinal = rutaArchivos;
			break;
		default:
			System.out.println("No encontro match");
		}

		//System.out.println("Ruta archivos:" + rutaArchivosFinal);
		return rutaArchivosFinal;
	}
	
	public static String obtenerRutaFinal(String rutaArchivo) {

		//System.out.println("///////////////////ambiente:-" + ambiente + "-");
		//System.out.println("rutaArchivosQAS:" + rutaArchivosQAS);
		//System.out.println("rutaArchivosPRO:" + rutaArchivosPRO);
		//System.out.println("rutaArchivosTest:" + rutaArchivos);
		String rutaArchivosFinal = "";

		switch (ambiente) {
		case "qas":
			System.out.println("Es QAS");
			rutaArchivosFinal = rutaArchivosQAS + rutaArchivo;
			break;
		case "pro":
			System.out.println("Es PRO");
			rutaArchivosFinal = rutaArchivosPRO + rutaArchivo;
			break;
		case "test":
			System.out.println("Es local");
			rutaArchivosFinal = rutaArchivos + rutaArchivo;
			break;
		default:
			System.out.println("No encontro match");
		}
		//System.out.println("Rutafinal:" + rutaArchivosFinal);
		return rutaArchivosFinal;
	}
	
	
	public static String convertir12a24(String hora12) {
       
		try {
            // Eliminar espacios y convertir a minúsculas para normalizar
            String horaNormalizada = hora12.trim().toLowerCase();
            
            // Dividir en horas y minutos
            String[] partes = horaNormalizada.split(":");
            if (partes.length != 2) {
                throw new IllegalArgumentException("Formato inválido. Use h:mm");
            }
            
            // Obtener horas y minutos
            int horas = Integer.parseInt(partes[0].trim());
            int minutos = Integer.parseInt(partes[1].trim());
            
            // Validar rangos
            if (horas < 1 || horas > 12) {
                throw new IllegalArgumentException("Las horas deben estar entre 1 y 12");
            }
            if (minutos < 0 || minutos > 59) {
                throw new IllegalArgumentException("Los minutos deben estar entre 0 y 59");
            }
            
            // Convertir a formato 24h (si es PM, sumar 12 a las horas, excepto para las 12 PM)
            int horas24;
            if (horas == 12) {
                horas24 = 12; // 12 PM se convierte en 12
            } else {
                horas24 = horas + 12; // 1-11 PM se convierten en 13-23
            }
            
            // Formatear con ceros a la izquierda
            return String.format("%02d:%02d", horas24, minutos);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato numérico inválido: " + hora12);
        }
		
    }
	
	public static String convertirAPM(String hora12) {
        // Asume que siempre es PM (tarde/noche)
        return convertir12a24(hora12);
    }
	
	public static boolean validarDiferenciaHoraria(String horaClase, String horaActual,int horasPermitidas) {
		boolean salida = false;
        try {
        	int horasP = horasPermitidas * 60;
        	System.out.println("Horas permitidas en minutos:"+horasP);
        	if (horaClase != null && horaClase.matches("^\\d:\\d{2}$")) {
        	    horaClase = "0" + horaClase;
        	}
            LocalTime tiempoClase = LocalTime.parse(horaClase);
            LocalTime tiempoActual = LocalTime.parse(horaActual);
            System.out.println("tiempoClase:"+tiempoClase+"|tiempoActual:"+tiempoActual);
            long diferencia = Math.abs(java.time.temporal.ChronoUnit.MINUTES.between(tiempoActual,tiempoClase ));
            System.out.println("diferencia:"+diferencia);
            salida = diferencia > horasP;
            System.out.println("diferencia > horasP:"+salida);
            return salida;
        } catch (Exception e) {
        	System.out.println("error:"+e.getMessage());
            return false; // o lanzar excepción
        }
    }
	
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

	/*
	 Regresa:
	 - true: sí puede seguir el proceso de apartar
	 - false: no puede apartar la clase
	*/
	public static boolean horaPermitidaFechaHoraB(
	        String fechaRecibida,
	        String horaRecibida,
	        String horaClase,
	        String tipoClase,
	        int tiempoLimite
	) {
	    LocalDate fechaActual = LocalDate.now();
	    LocalDate fechaParametro = LocalDate.parse(fechaRecibida, FORMATO_FECHA);
	    
	    System.out.println("FechaRecibida:"+fechaRecibida + " |fechaActual:"+fechaActual);
	    
	    // Si la fecha actual es mayor, ya pasó la fecha y ya no puede apartar.
	    if (fechaActual.isAfter(fechaParametro)) {
	    	System.out.println("Fecha actual es mayor, ya paso fecha");
	        return false;
	    }

	    // Si la fecha recibida es futura, sí puede seguir el proceso de apartar.
	    if (fechaParametro.isAfter(fechaActual)) {
	    	System.out.println("FechaRecibida es mayor que la fecha actual");
	        return true;
	    }

	    // 2) Convertir horaClase a HH:mm según tipoClase
	    int horaBase = Integer.parseInt(horaClase.trim());

	    int hora24;
	    if ("V".equalsIgnoreCase(tipoClase)) {
	        // Ejemplo requerido: 6 -> 18:00
	        hora24 = horaBase + 12;
	        if (hora24 >= 24) {
	            hora24 = hora24 - 24;
	        }
	    } else if ("M".equalsIgnoreCase(tipoClase)) {
	        // Ejemplo requerido: 6 -> 06:00
	        hora24 = horaBase;
	    } else {
	        throw new IllegalArgumentException("tipoClase inválido. Usa M o V.");
	    }

	    LocalTime horaClaseConvertida = LocalTime.of(hora24, 0);
	    LocalTime horaActualRecibida = LocalTime.parse(horaRecibida, FORMATO_HORA);

	    // Si ya pasó la hora de clase, ya no se puede apartar.
	    if (horaActualRecibida.isAfter(horaClaseConvertida)) {
	    	return false;
	    }

	    // Ventana de horario no permitido:
	    // desde (horaClase - tiempoLimite minutos) hasta horaClase (inclusive)
	    LocalTime inicioNoPermitido = horaClaseConvertida.minusMinutes(tiempoLimite);

	    boolean dentroNoPermitido =
	            !horaActualRecibida.isBefore(inicioNoPermitido)
	            && !horaActualRecibida.isAfter(horaClaseConvertida);

	    return !dentroNoPermitido;
	}
	
	public static boolean horaPermitidaFechaHora(
	        String fechaRecibida,
	        String horaRecibida,
	        String horaClase,
	        String tipoClase,
	        int tiempoLimite
	) {

	    LocalDate fechaClase = LocalDate.parse(fechaRecibida, FORMATO_FECHA);
	    LocalTime horaActual = LocalTime.parse(horaRecibida, FORMATO_HORA);

	    int horaBase = Integer.parseInt(horaClase.trim());
	    int hora24;

	    if ("M".equalsIgnoreCase(tipoClase)) {
	        hora24 = horaBase;
	    } else if ("V".equalsIgnoreCase(tipoClase)) {
	        hora24 = horaBase + 12;
	        if (hora24 >= 24) {
	            hora24 -= 24;
	        }
	    } else {
	        throw new IllegalArgumentException("tipoClase inválido. Usa M o V.");
	    }

	    LocalTime horaClaseTime = LocalTime.of(hora24, 0);

	    LocalDateTime fechaHoraActual = LocalDateTime.of(LocalDate.now(), horaActual);
	    LocalDateTime fechaHoraClase = LocalDateTime.of(fechaClase, horaClaseTime);

	    // Si la clase ya pasó o es en este momento
	    if (!fechaHoraClase.isAfter(fechaHoraActual)) {
	        return false;
	    }

	    long minutosFaltantes = Duration.between(fechaHoraActual, fechaHoraClase).toMinutes();
	    System.out.println("minutosFaltantes:"+minutosFaltantes);

	    // Convertir horas a minutos
	    long minutosLimite = tiempoLimite * 60L;

	    // Solo permitir si faltan más minutos que el límite
	    return minutosFaltantes > minutosLimite;
	}

	private static void imprimirTituloPrueba(String titulo) {
		System.out.println("\n===== " + titulo + " =====");
	}

	private static void ejecutarPruebaHorario(
			int numero,
			String descripcion,
			String fecha,
			String horaActual,
			String horaClase,
			String tipoClase,
			int tiempoLimite,
			boolean esperado) {
		boolean resultado = horaPermitidaFechaHora(fecha, horaActual, horaClase, tipoClase, tiempoLimite);
		System.out.println(numero + ") " + descripcion);
		System.out.println("   fecha=" + fecha
				+ " | horaActual=" + horaActual
				+ " | horaClase=" + horaClase + tipoClase
				+ " | limite=" + tiempoLimite + " hora");
		System.out.println("   esperado=" + esperado + " | resultado=" + resultado);
		System.out.println(resultado ? "   interpretacion: SI puede seguir el proceso de apartar" : "   interpretacion: NO puede apartar la clase");
		System.out.println("---------------------------------------------------------------------");
	}

	public static void main(String[] args) {
		String fechaActualManual = "";
		String fechaFuturaManual = "2026-06-27";
		String fechaPasadaManual = "";

		String fechaActual = fechaActualManual.isEmpty()
				? LocalDate.now().format(FORMATO_FECHA)
				: fechaActualManual;
		String fechaFutura = fechaFuturaManual.isEmpty()
				? LocalDate.now().plusDays(1).format(FORMATO_FECHA)
				: fechaFuturaManual;
		String fechaPasada = fechaPasadaManual.isEmpty()
				? LocalDate.now().minusDays(1).format(FORMATO_FECHA)
				: fechaPasadaManual;

		System.out.println("Fecha actual usada en pruebas: " + fechaActual);
		System.out.println("Fecha futura usada en pruebas: " + fechaFutura);
		System.out.println("Fecha pasada usada en pruebas: " + fechaPasada);
		System.out.println("Convencion mostrada en consola: true = SI puede seguir el proceso de apartar, false = NO puede apartar la clase");

		imprimirTituloPrueba("Fechas Distintas Al Dia Actual");
		
		
		ejecutarPruebaHorario(1,
				"Fecha futura: el metodo actualmente marca la fecha futura como no permitida",
				fechaFutura, "15:00", "6", "M", 1, true);
		ejecutarPruebaHorario(2,
				"Fecha pasada: ya no deberia bloquear por ventana de tiempo del dia actual",
				fechaPasada, "15:00", "6", "M", 1, false);

		imprimirTituloPrueba("Mismo Dia - Clase Matutina 06:00");
		ejecutarPruebaHorario(3,
				"Dos horas antes de la clase: aun se permite apartar",
				fechaActual, "04:00", "6", "M", 1, true);
		ejecutarPruebaHorario(4,
				"Treinta minutos antes de la clase: ya entra en horario no permitido",
				fechaActual, "05:30", "6", "M", 1, false);
		ejecutarPruebaHorario(5,
				"Despues de la hora de clase: ya no se puede apartar",
				fechaActual, "08:00", "6", "M", 1, false);

		imprimirTituloPrueba("Mismo Dia - Umbral De 1 Hora Antes De Clase 07:00");
		ejecutarPruebaHorario(6,
				"07:00 clase, 05:50 hora actual: faltan 70 minutos, si debe dejar apartar",
				fechaActual, "05:50", "7", "M", 1, true);
		ejecutarPruebaHorario(7,
				"07:00 clase, 05:59 hora actual: aun faltan mas de 60 minutos",
				fechaActual, "05:59", "7", "M", 1, true);
		ejecutarPruebaHorario(8,
				"07:00 clase, 06:00 hora actual: empieza el bloqueo exacto de 1 hora",
				fechaActual, "06:00", "7", "M", 1, false);
		ejecutarPruebaHorario(9,
				"07:00 clase, 06:01 hora actual: sigue dentro de la hora no permitida",
				fechaActual, "06:01", "7", "M", 1, false);
		ejecutarPruebaHorario(10,
				"07:00 clase, 06:59 hora actual: todavia no se debe permitir apartar",
				fechaActual, "06:59", "7", "M", 1, false);
		ejecutarPruebaHorario(11,
				"07:00 clase, 07:00 hora actual: se mantiene no permitido",
				fechaActual, "07:00", "7", "M", 1, false);
		ejecutarPruebaHorario(12,
				"07:00 clase, 07:01 hora actual: la clase ya empezo o ya paso",
				fechaActual, "07:01", "7", "M", 1, false);

		ejecutarPruebaHorario(13,
				"07:00 clase, 05:19 hora actual: sigue dentro de la hora permitida",
				fechaActual, "05:19", "7", "M", 1, true);
		
		ejecutarPruebaHorario(14,
				"06:00 clase, 21:23 fecha futura: sigue dentro de la hora permitida",
				fechaFutura, "21:23", "6", "M", 1, true);

		
		
	}
	

}
