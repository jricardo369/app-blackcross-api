package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.modelo.MembresiaAVencer;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Propiedades;
import com.vazjim.controlasistencias.utilidades.Utilidades;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class UsuarioLogica {

	private static Logger log = Logger.getLogger(UsuarioLogica.class);
	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getProperties();

	/**
	 * Consulta unica que trae los datos del usuario, su ultima inscripcion (membresia) y la
	 * cantidad de asistencias registradas dentro del periodo de esa membresia.
	 */
	private static final String SELECT_USUARIO_CON_MEMBRESIA = "SELECT u.id_usuario,u.id_rol,u.usuario,u.nombre,u.sexo," + 
				"u.correo_electronico,u.peso,u.altura,u.imc,u.telefono,u.nivel,u.estatus,u.intentos,u.sociedad," + 
				"u.total_multas,u.contador_faltas,u.fecha_tc," + 
				"p.fecha_inicio,p.fecha_corte,p.clases," + 
				"(CASE WHEN p.id_inscripcion IS NULL THEN 0 " + 
				"ELSE GREATEST(p.clases - (" + 
				"  (SELECT COUNT(*) FROM asistencia_a_clase a " + 
				"   WHERE a.id_usuario = u.id_usuario " + 
				" AND a.fecha >= DATE(p.fecha_inicio) " + 
				" AND a.fecha < DATE(p.fecha_corte) + INTERVAL 1 DAY)" + 
				"  + " + 
				"  (SELECT COUNT(*) FROM asistencia_a_clase_hyrox h " + 
				"   WHERE h.id_usuario = u.id_usuario " + 
				" AND h.fecha >= DATE(p.fecha_inicio) " + 
				" AND h.fecha < DATE(p.fecha_corte) + INTERVAL 1 DAY)" + 
				"), 0) END) AS clases_restantes," + 
				"ti.tipo_inscripcion," + 
				"(" + 
				"(SELECT COUNT(*) FROM asistencia_a_clase a " + 
				"WHERE a.id_usuario = u.id_usuario " + 
				"AND a.fecha >= DATE(p.fecha_inicio) " + 
				"AND a.fecha < DATE(p.fecha_corte) + INTERVAL 1 DAY)" + 
				"+ " + 
				"(SELECT COUNT(*) FROM asistencia_a_clase_hyrox h " + 
				"WHERE h.id_usuario = u.id_usuario " + 
				"AND h.fecha >= DATE(p.fecha_inicio) " + 
				"AND h.fecha < DATE(p.fecha_corte) + INTERVAL 1 DAY)" + 
				") AS asistencias, " + 
				"tp.titulo AS tipo_paquete " + 
				"FROM usuario u " + 
				"LEFT JOIN inscripcion p ON p.id_inscripcion = (SELECT MAX(p2.id_inscripcion) FROM inscripcion p2 " + 
				"WHERE p2.id_usuario = u.id_usuario) " + 
				"LEFT JOIN tipo_inscripcion ti ON ti.id_tipo_inscripcion = p.id_tipo_inscripcion " + 
				"LEFT JOIN tipo_paquete tp ON tp.id_tipo_paquete = p.tipo_paquete";

	/**
	 * Obtiene el usuario con su membresia y el conteo de asistencias del periodo de la membresia,
	 * validando usuario y contrasenia.
	 * @param usuario
	 * @param contrasenia
	 * @return el usuario encontrado o null
	 * @throws SQLException
	 */
	public Usuario obtenerUsuarioConMembresiaYAsistencias(String usuario, String contrasenia) throws SQLException {
		return obtenerUsuarioConMembresiaYAsistencias("u.usuario = ? AND u.contrasenia = ?", usuario, contrasenia);
	}

	/**
	 * Socios cuyas membresias estan por vencer o ya se agotaron.
	 *
	 * Se toma la ultima inscripcion de cada socio (MAX(id_inscripcion)) y se arma la columna
	 * `vencimiento` con el estado de la membresia:
	 *
	 * <ul>
	 * <li>Membresia por tiempo (1 Mes, 2 Meses, Mensual, Semanal, 1 Semana...): se informa por
	 * fecha de corte, porque no manejan cupo de clases.</li>
	 * <li>Membresia por clases: se informa por el saldo de clases restantes.</li>
	 * </ul>
	 *
	 * Se excluyen los socios cuya ultima membresia vencio hace mas de tres meses y que no
	 * han contratado una nueva, porque ya no son gestionables desde el panel.
	 *
	 * Se consideran los socios activos (estatus 1) y los que tienen falta de pago (estatus 3),
	 * para que el panel muestre el estado de la membresia de toda la cartera.
	 * Solo se toma en cuenta a los socios (id_rol 2); admins y coaches quedan fuera.
	 */
	private static final String SELECT_USUARIOS_MEMBRESIAS_A_VENCER = "SELECT u.id_usuario,u.id_rol,u.usuario,u.nombre,u.sexo,"
			+ "u.correo_electronico,u.telefono,u.intentos,u.estatus,"
			+ "p.fecha_inicio,p.fecha_corte,p.clases,"
			+ "ti.tipo_inscripcion,"
			+ "CASE "
			+ "WHEN p.id_inscripcion IS NULL THEN 'No cuenta con clases' "
			+ "WHEN (LOWER(ti.tipo_inscripcion) LIKE '%mes%' "
			+ "OR LOWER(ti.tipo_inscripcion) LIKE '%mensual%' "
			+ "OR LOWER(ti.tipo_inscripcion) LIKE '%semana%') "
			+ "AND LOWER(ti.tipo_inscripcion) NOT LIKE '%clase%' "
			+ "THEN (CASE "
			+ "WHEN p.fecha_corte IS NULL THEN 'Sin fecha de corte' "
			+ "WHEN DATE(p.fecha_corte) < CURDATE() THEN 'Esta a punto de acabarse su membresia' "
			+ "ELSE CONCAT('Membresia vigente hasta ', DATE_FORMAT(p.fecha_corte, '%d/%m/%Y')) "
			+ "END) "
			+ "WHEN COALESCE(p.clases_restantes, 0) <= 0 THEN 'No cuenta con clases' "
			+ "WHEN p.fecha_corte IS NOT NULL AND DATE(p.fecha_corte) < CURDATE() "
			+ "THEN 'Esta a punto de acabarse su membresia' "
			+ "ELSE CONCAT('Le quedan ', COALESCE(p.clases_restantes, 0), ' clases disponibles') "
			+ "END AS vencimiento "
			+ "FROM usuario u "
			+ "LEFT JOIN inscripcion p ON p.id_inscripcion = (SELECT MAX(p2.id_inscripcion) FROM inscripcion p2 "
			+ "WHERE p2.id_usuario = u.id_usuario) "
			+ "LEFT JOIN tipo_inscripcion ti ON ti.id_tipo_inscripcion = p.id_tipo_inscripcion "
			+ "WHERE u.estatus IN (1, 3) "
			+ "AND u.id_rol = 2 "
			+ "AND NOT (p.fecha_corte IS NOT NULL AND DATE(p.fecha_corte) < DATE_ADD(CURDATE(), INTERVAL -3 MONTH))";

	/**
	 * Obtiene el usuario con su membresia y el conteo de asistencias del periodo de la membresia,
	 * buscandolo unicamente por su identificador.
	 * @param idUsuario
	 * @return el usuario encontrado o null
	 * @throws SQLException
	 */
	public Usuario obtenerUsuarioConMembresiaYAsistencias(int idUsuario) throws SQLException {
		return obtenerUsuarioConMembresiaYAsistencias("u.id_usuario = ?", idUsuario);
	}

	/**
	 * Ejecuta la consulta unica de usuario con membresia y asistencias.
	 * @param where clausula <code>WHERE</code> con los marcadores de posicion
	 * @param parametros valores de los marcadores de posicion
	 * @return el usuario encontrado o null
	 * @throws SQLException
	 */
	private Usuario obtenerUsuarioConMembresiaYAsistencias(String where, Object... parametros) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		Usuario u = null;

		String querySql = SELECT_USUARIO_CON_MEMBRESIA + " WHERE " + where;

		System.out.println("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			for (int i = 0; i < parametros.length; i++) {
				st.setObject(i + 1, parametros[i]);
			}

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				u = mapearUsuario(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}

			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		return u;
	}

	/**
	 * Mapea el resultset de la consulta unica de usuario con membresia y asistencias.
	 * @param rs
	 * @return el usuario mapeado
	 * @throws SQLException
	 */
	private Usuario mapearUsuario(ResultSet rs) throws SQLException {

		Usuario u = new Usuario();
		u.setIdUsuario(rs.getInt(1));
		u.setIdRol(rs.getInt(2));
		u.setUsuario(rs.getString(3));
		u.setNombre(rs.getString(4));
		u.setSexo(rs.getString(5));
		u.setCorreoElectronico(rs.getString(6));
		u.setPeso(rs.getDouble(7));
		u.setAltura(rs.getDouble(8));
		u.setImc(rs.getString(9));
		u.setTelefono(rs.getString(10));
		u.setNivel(rs.getString(11));
		u.setEstatus(rs.getString(12));
		u.setIntentos(rs.getInt(13));
		u.setSociedad(rs.getInt(14));
		u.setTotalMultas(rs.getInt(15));
		u.setContadorFaltas(rs.getInt(16));
		u.setFechaTC(rs.getString(17));

		String fechaCorte = normalizarFecha(rs.getString(19));
		String tipoInscripcion = rs.getString(22);
		int asistencias = rs.getInt(23);
		String tipoPaquete = rs.getString(24);

		u.setAsistencias(asistencias);
		if (u.getNombre() != null && !"".equals(u.getNombre())) {
			u.setIniciales(Utilidades.inicialesNombre(u.getNombre()));
		}

		if (u.getIdRol() == 2) {
			if (tipoInscripcion != null && !"".equals(tipoInscripcion)) {
				if ("Mensual".equals(tipoInscripcion)) {
					u.setMembresia("Membresia " + tipoInscripcion + ", vence el día " + fechaCorte);
					u.setMembresiaDesc("Membresia " + tipoInscripcion + ", vence el día " + fechaCorte);
					u.setVencimiento("Vence " + fechaCorte);
					u.setTipoInscripcion(tipoInscripcion);
					u.setTipoPaquete(tipoPaquete);
				} else {
					u.setMembresia("Membresia de " + tipoInscripcion);
					u.setMembresiaDesc("Membresia de " + tipoInscripcion + " le quedan " + rs.getInt(21));
					u.setVencimiento(fechaCorte);
					u.setNumClases(rs.getInt(20));
					u.setClasesRestantes(rs.getInt(21));
					u.setClasesTomadas(asistencias);
					u.setTipoInscripcion(tipoInscripcion);
					u.setTipoPaquete(tipoPaquete);
				}
			} else {
				u.setMembresia("Sin membresia");
				u.setMembresiaDesc("No cuentas con membresia");
				u.setVencimiento("N/A");
			}
		}
		u.setMensaje("");

		return u;
	}

	/**
	 * Recorta la fecha a formato <code>yyyy-MM-dd</code> manejando valores nulos o vacios.
	 * @param fecha
	 * @return la fecha normalizada o cadena vacia
	 */
	private String normalizarFecha(String fecha) {
		if (fecha == null) {
			return "";
		}
		if (fecha.length() > 10) {
			return fecha.substring(0, 10);
		}
		return fecha;
	}

	/**
	 * Arma el mensaje de estado de la membresia con la acentuacion correcta.
	 *
	 * Los textos se arman en el SQL sin acentos para no depender del charset de la
	 * conexion a MySQL; aqui se sustituyen por su version acentuada, que ya viaja
	 * en UTF-8 dentro del JSON de la respuesta.
	 *
	 * @param mensaje mensaje-armado por el SQL
	 * @return el mensaje acentuado
	 */
	private String acentuarMensaje(String mensaje) {
		if (mensaje == null) {
			return "";
		}
		return mensaje
				.replace("Esta a punto de acabarse su membresia", "Está a punto de acabarse su membresía")
				.replace("Membresia vigente hasta", "Membresía vigente hasta")
				.replace("No cuenta con clases", "No cuenta con clases")
				.replace("Sin fecha de corte", "Sin fecha de corte");
	}

	/**
	 * Lista los socios cuyas membresias estan por vencer o ya se agotaron.
	 *
	 * @param sociedad sociedad del gym; si es 0 no se filtra por sociedad
	 * @return lista de socios con el estado de su membresia en el campo <code>vencimiento</code>
	 * @throws SQLException
	 */
	public List<MembresiaAVencer> usuariosMembresiasAVencer(int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		List<MembresiaAVencer> membresias = new ArrayList<>();

		String querySql = SELECT_USUARIOS_MEMBRESIAS_A_VENCER;
		boolean filtraSociedad = sociedad > 0;
		if (filtraSociedad) {
			querySql += " AND u.sociedad = ? ";
		}

		System.out.println("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			if (filtraSociedad) {
				st.setInt(1, sociedad);
			}

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				MembresiaAVencer m = new MembresiaAVencer();
				m.setIdUsuario(rs.getInt(1));
				m.setIdRol(rs.getInt(2));
				m.setUsuario(rs.getString(3));
				m.setNombre(rs.getString(4));
				m.setSexo(rs.getString(5));
				m.setCorreoElectronico(rs.getString(6));
				m.setTelefono(rs.getString(7));
				m.setIntentos(rs.getInt(8));
				m.setEstatus(rs.getString(9));
				m.setFechaInicio(normalizarFecha(rs.getString(10)));
				m.setFechaCorte(normalizarFecha(rs.getString(11)));
				m.setClases(rs.getInt(12));
				m.setTipoInscripcion(rs.getString(13));
				m.setVencimiento(acentuarMensaje(rs.getString(14)));

				membresias.add(m);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		return membresias;
	}


	/**
	 * Consulta unica que trae los datos del usuario, su ultima inscripcion (membresia) y la
	 * cantidad de asistencias registradas dentro del periodo de esa membresia.
	 */
	private static final String SELECT_TODOS_USUARIOS= "SELECT u.id_usuario,u.id_rol,u.usuario,u.nombre,u.sexo," + 
				"u.correo_electronico,u.peso,u.altura,u.imc,u.telefono,estatus,intentos,total_multas,contador_faltas,fecha_tc," + 
				"DATE(p.fecha_inicio) AS fecha_inicio,DATE(p.fecha_corte) AS fecha_corte,p.clases," + 
				"CONCAT('Membresia ', ti.tipo_inscripcion) as membresia," + 
				"CONCAT('Membresia ', ti.tipo_inscripcion, ' le restan ', p.clases_restantes) AS membresiaDesc," + 
				"CASE " + 
				"WHEN p.id_inscripcion IS NULL THEN 'No cuenta con clases' " + 
				"WHEN (LOWER(ti.tipo_inscripcion) LIKE '%mes%' " + 
				"OR LOWER(ti.tipo_inscripcion) LIKE '%mensual%' " + 
				"OR LOWER(ti.tipo_inscripcion) LIKE '%semana%') " + 
				"AND LOWER(ti.tipo_inscripcion) NOT LIKE '%clase%' " + 
				"THEN (CASE " + 
				"WHEN p.fecha_corte IS NULL THEN 'Sin fecha de corte' " + 
				"WHEN DATE(p.fecha_corte) < CURDATE() THEN 'Esta a punto de acabarse su membresia' " + 
				"ELSE CONCAT('Membresia vigente hasta ', DATE_FORMAT(p.fecha_corte, '%d/%m/%Y')) " + 
				"END) " + 
				"WHEN COALESCE(p.clases_restantes, 0) <= 0 THEN 'No cuenta con clases' " + 
				"WHEN p.fecha_corte IS NOT NULL AND DATE(p.fecha_corte) < CURDATE() " + 
				"THEN 'Esta a punto de acabarse su membresia' " + 
				"ELSE CONCAT('Le quedan ', COALESCE(p.clases_restantes, 0), ' clases disponibles') " + 
				"END AS vencimiento," + 
				"tp.titulo AS tipo_paquete,UPPER(LEFT(u.nombre, 2)) AS iniciales " + 
				"FROM usuario u " + 
				"LEFT JOIN inscripcion p ON p.id_inscripcion = (SELECT MAX(p2.id_inscripcion) FROM inscripcion p2 " + 
				"WHERE p2.id_usuario = u.id_usuario) " + 
				"LEFT JOIN tipo_inscripcion ti ON ti.id_tipo_inscripcion = p.id_tipo_inscripcion " + 
				"LEFT JOIN tipo_paquete tp ON tp.id_tipo_paquete = p.tipo_paquete " + 
				"WHERE u.estatus IN (1, 3) " + 
				"AND u.id_rol IN (2,3) " + 
				"";

	/**
	 * Lista los socios cuyas membresias estan por vencer o ya se agotaron.
	 *
	 * @param sociedad sociedad del gym; si es 0 no se filtra por sociedad
	 * @return lista de socios con el estado de su membresia en el campo <code>vencimiento</code>
	 * @throws SQLException
	 */
	public List<Usuario> obtenerTodosLosUsuarios(int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		List<Usuario> usuarios = new ArrayList<>();

		String querySql = SELECT_TODOS_USUARIOS;
		boolean filtraSociedad = sociedad > 0;
		if (filtraSociedad) {
			querySql += " AND u.sociedad = ? ";
		}

		System.out.println("querySql" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			if (filtraSociedad) {
				st.setInt(1, sociedad);
			}

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				Usuario m = new Usuario();
				m.setIdUsuario(rs.getInt(1));
				m.setIdRol(rs.getInt(2));
				m.setUsuario(rs.getString(3));
				m.setNombre(rs.getString(4));
				m.setSexo(rs.getString(5));
				m.setCorreoElectronico(rs.getString(6));
				m.setPeso(rs.getDouble(7));
				m.setAltura(rs.getDouble(8));
				m.setImc(rs.getString(9));
				m.setTelefono(rs.getString(10));
				m.setEstatus(rs.getString(11));
				m.setIntentos(rs.getInt(12));
				m.setTotalMultas(rs.getInt(13));
				m.setContadorFaltas(rs.getInt(14));
				m.setFechaInicio(normalizarFecha(rs.getString(15)));
				m.setFechaCorte(normalizarFecha(rs.getString(16)));
				m.setNumClases(rs.getInt(17));
				m.setMembresia(rs.getString(18));
				m.setMembresiaDesc(acentuarMensaje(rs.getString(19)));
				m.setVencimiento(acentuarMensaje(rs.getString(20)));
				m.setTipoPaquete(rs.getString(21));
				m.setIniciales(rs.getString(22));

				usuarios.add(m);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		return usuarios;
	}

	public Usuario iniciarSesion(String usuario, String contrasenia) {

		log.info("--------------------------------------- INICIANDO SESION...");

		Usuario usuarioObj = null;

		try {
			List<Usuario> usuarios = obtener("usuario", usuario, 0, false);

			if (usuarios != null && !usuarios.isEmpty()) {
				usuarioObj = usuarios.get(0);
			}

			if(usuarioObj != null) {
				if(!usuarioObj.getContrasenia().equals(contrasenia)){
					usuarioObj = null;
				}else{
					usuarioObj = obtenerUsuarioConMembresiaYAsistencias(usuarioObj.getIdUsuario());
				}
			}


		} catch (SQLException e) {
			e.printStackTrace();
		}



		return usuarioObj;

	}

	public List<Usuario> obtener(String columna, String valor,int sociedad,boolean conMensaje) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		
		InscripcionLogica ins = new InscripcionLogica();
		AsistenciaAClaseLogica al = null;

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<Usuario> usuarios = new ArrayList<>();
		Usuario usuario;

		querySql = "SELECT id_usuario,id_rol,usuario,contrasenia,nombre,sexo,correo_electronico,peso,altura,imc,telefono,nivel,estatus,intentos,total_multas,contador_faltas,sociedad,fecha_tc FROM usuario ";

		String where = "";
		
		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " sociedad = " + sociedad + " ";
		}
		
		if (columna != null && !"".equals(columna)) {
			if (!where.equals(""))
				where += " AND ";
			where += " " + columna + " = '" + valor + "'";
		}

		if (!where.equals("")) {
			if (!where.equals(""))
				where = " WHERE " + where + " ORDER BY id_usuario";
			querySql += where;
		}

		System.out.println("query:" + querySql);

		try {

			if (conMensaje) {
				al = new AsistenciaAClaseLogica();
			}
			
			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				usuario = new Usuario();
				usuario.setIdUsuario(rs.getInt(1));
				usuario.setIdRol(rs.getInt(2));
				usuario.setUsuario(rs.getString(3));
				usuario.setContrasenia(rs.getString(4));
				usuario.setNombre(rs.getString(5));
				usuario.setSexo(rs.getString(6));
				usuario.setCorreoElectronico(rs.getString(7));
				usuario.setPeso(rs.getDouble(8));
				usuario.setAltura(rs.getDouble(9));
				usuario.setImc(rs.getString(10));
				usuario.setTelefono(rs.getString(11));
				usuario.setNivel(rs.getString(12));
				usuario.setEstatus(rs.getString(13));
				usuario.setIntentos(rs.getInt(14));
				usuario.setTotalMultas(rs.getInt(15));
				usuario.setContadorFaltas(rs.getInt(16));
				usuario.setSociedad(rs.getInt(17));
				if (rs.getInt(2) == 2) { 
					Inscripcion insc = ins.obtenerUlimaInscripcionDeUsuario(usuario.getIdUsuario());
					if (insc != null) {
						if(insc.getTipoInscripcionDesc().equals("Mensual")){
							usuario.setMembresia("Membresia " + insc.getTipoInscripcionDesc());
							usuario.setMembresiaDesc("Membresia " + insc.getTipoInscripcionDesc() + ", vence el día " + insc.getFechaCorte());
							usuario.setTipoPaquete(insc.getTipoInscripcionDesc());
						}else{
							usuario.setMembresia("Membresia por " + insc.getTipoInscripcionDesc());
							//usuario.setMembresiaDesc("Membresia por " + insc.getTipoInscripcionDesc() + " le quedan " + insc.getClases_restantes() + ", vencen el día " + insc.getFechaCorte());
							usuario.setMembresiaDesc("Membresia por " + insc.getTipoInscripcionDesc() + " le quedan " + insc.getClases_restantes());
							usuario.setClasesRestantes(insc.getClases_restantes());
							usuario.setTipoPaquete(insc.getTipoInscripcionDesc());
						}
						usuario.setVencimiento(insc.getVencimiento());
					} else {
						usuario.setMembresia("Sin membresia");
						usuario.setMembresiaDesc("No cuentas con membresia");
						usuario.setVencimiento("N/A");
					}
				}
				usuario.setIniciales(Utilidades.inicialesNombre(usuario.getNombre()));
				
				if (conMensaje) {
					String m = al.obtenerMensajeUsuario(usuario.getIdUsuario());
					usuario.setMensaje(m);
				}
				
				usuario.setFechaTC(rs.getString(18));

				usuarios.add(usuario);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		return usuarios;
	}
	
	public Usuario obtenerUsuarioById(int idUsuario) throws SQLException {
		Usuario us = null;
		List<Usuario> usuarios = obtener("id_usuario", String.valueOf(idUsuario), 0, false);
		if (!usuarios.isEmpty()) {
			us = usuarios.get(0);
		}
		return us;
	}

	public String insertar(Usuario usuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();
		String cee = "";

		querySql = "INSERT INTO usuario(id_rol,usuario,contrasenia,nombre,sexo,correo_electronico,peso,altura,imc,telefono,nivel,estatus,intentos,sociedad) " + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, usuario.getIdRol());
			st.setString(2, usuario.getUsuario());
			st.setString(3, usuario.getContrasenia());
			st.setString(4, usuario.getNombre());
			st.setString(5, usuario.getSexo());
			String ce = Utilidades.quitarCaracteresEsp(usuario.getCorreoElectronico());
			cee = ce.equals("") ? "" : ce;
			st.setString(6, cee);
			st.setDouble(7, usuario.getPeso());
			st.setDouble(8, usuario.getAltura());
			st.setString(9, usuario.getImc());
			st.setString(10, usuario.getTelefono());
			st.setString(11, usuario.getNivel());
			st.setString(12, "1");
			st.setInt(13, 0);
			st.setInt(14, usuario.getSociedad());
			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("US-CREADO", "ES");
		return m.getMensaje();
	}

	public String actualizar(Usuario usuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE usuario " + "SET id_rol = ?, usuario = ?, contrasenia = ?, nombre = ?, sexo = ?, correo_electronico = ?, " + "peso = ?,altura = ?,imc = ?,telefono = ?,nivel = ?,estatus = ?,intentos = ? " + "WHERE id_usuario = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, usuario.getIdRol());
			st.setString(2, usuario.getUsuario());
			st.setString(3, usuario.getContrasenia());
			st.setString(4, usuario.getNombre());
			st.setString(5, usuario.getSexo());
			String ce = Utilidades.quitarCaracteresEsp(usuario.getCorreoElectronico());
			st.setString(6, ce);
			st.setDouble(7, usuario.getPeso());
			st.setDouble(8, usuario.getAltura());
			st.setString(9, usuario.getImc());
			st.setString(10, usuario.getTelefono());
			st.setString(11, usuario.getNivel());
			st.setString(12, "1");
			st.setInt(13, 0);
			st.setInt(14, usuario.getIdUsuario());

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}
	
	public String actualizarDatosPerfilUsuario(Usuario usuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE usuario SET correo_electronico = ?,telefono = ? WHERE id_usuario = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, usuario.getCorreoElectronico());
			st.setString(2, usuario.getTelefono());
			st.setInt(3, usuario.getIdUsuario());

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}

	public String actualizarFechaTC(int idUsuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE usuario SET fecha_tc = ? WHERE id_usuario = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			String fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			st.setString(1, fecha);
			st.setInt(2, idUsuario);

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} catch (ParseException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}

	public String actualizarCampo(String campo, String valor, int idUsuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE usuario SET " + campo + " = ? WHERE id_usuario = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setString(1, valor);
			st.setInt(2, idUsuario);

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}

	public String cambiarEstatusUsuario(int idUsuario, String estatus, int intentos) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		if (intentos == 0) {
			querySql = "UPDATE usuario " + "SET estatus = ?, intentos = 0 " + "WHERE id_usuario = ?";
		} else {
			querySql = "UPDATE usuario " + "SET estatus = ? " + "WHERE id_usuario = ?";
		}

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, estatus);
			st.setInt(2, idUsuario);

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}

	public String cambioContrasenia(int idUsuario, String contrasenia) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE usuario " + "SET contrasenia = ? " + "WHERE id_usuario = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, contrasenia);
			st.setInt(2, idUsuario);

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("US-CONT-CAM", "ES");
		return m.getMensaje();
	}

	public String obtenerContrasenia(int idUsuario) throws SQLException {

		PreparedStatement st = null;
		ResultSet rs = null;
		Connection conn = Conexion.getConnectionDbPool();

		String querySql = "";
		String pass = "";

		querySql = "SELECT password FROM usuario  WHERE id_usuario = ?";

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				pass = rs.getString(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		// Haciendo decoding del pass
		byte[] decoded = java.util.Base64.getDecoder().decode(pass);
		pass = new String(decoded);

		return pass;
	}

	/**
	 * Genera un token para el usuario solicitado.
	 * 
	 * @param solicitador
	 * @return un token de tipo <code>String</code>
	 */
	public String generarToken(String solicitador) {
		// Generando el token
		String token = Jwts.builder().setSubject(solicitador).signWith(SignatureAlgorithm.HS256, prop.getProperty("id_sesion").getBytes()).compact();
		return token;
	}

	public boolean usuarioExiste(String usuario) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		PreparedStatement st = null;
		ResultSet rs = null;
		int count = 0;
		String querySql = "";
		boolean salida = false;

		querySql = "SELECT id_usuario FROM usuario WHERE usuario = ?;";
		// log.info("querySql"+querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, usuario);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				count = 1;
			}

			if (count == 1) {
				salida = true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		log.info("Usuario existe?" + salida);
		return salida;
	}
	
	public String limpiarClasesPosteriorAFecha(int idUsuario,String fecha) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM asistencia_a_clase where id_usuario = ? and fecha >= ?;";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);
			st.setString(2, fecha);

			st.execute();
			conn.commit();

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}

		Mensaje m = MensajeLogica.obtenerMensaje("CLASES-ELIMINAR", "ES");
		return m.getMensaje();
	}
	
	public String limpiarToddasLasClasesUs(int idUsuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM asistencia_a_clase where id_usuario = ?;";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);

			st.execute();
			conn.commit();

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}

		Mensaje m = MensajeLogica.obtenerMensaje("CLASES-ELIMINAR", "ES");
		return m.getMensaje();
	}
	
	public String eliminarUsuario(int idUsuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM usuario WHERE id_usuario = ?;";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);

			st.execute();
			conn.commit();

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}

		Mensaje m = MensajeLogica.obtenerMensaje("US-BORRADO", "ES");
		return m.getMensaje();
	}
	
	public String actualizarDeExcel(String query) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = query;
		System.out.println(querySql);
		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}
	

}
