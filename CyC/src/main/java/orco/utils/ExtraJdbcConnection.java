package orco.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

public class ExtraJdbcConnection {

	public static Connection getConnection(HttpServletRequest request){
		Connection con = null;
		try {
		   	/*** Abrimos flujo de entrada y cargamos el contenido en props ****/
			Properties props = new Properties();
			String pathDBProperties = request.getSession().getServletContext().getRealPath("/WEB-INF/classes/META-INF/spring/database.properties");
			FileInputStream in = new FileInputStream(pathDBProperties);
			props.load(in); // Cargamos el contenido del flujo en props
			in.close();

			/**** Obtenemos las propiedades del objeto props ***/
			/*** Para registrar de manera estática (setProperty) el driver ***/
			String drivers = props.getProperty("database.driverClassName");
			if (drivers != null)
				System.setProperty("database.driverClassName", drivers);  // Cargamos driver
			/*** Para la conexión ***/
			String url = props.getProperty("database.url");
			String username = props.getProperty("database.username");
			String password = props.getProperty("database.password");

			/*** Conexión ****/
			con = DriverManager.getConnection( url, username, password );
			
		} catch (SQLException e) { 
			e.printStackTrace();  
		} catch (SecurityException e) { 
			e.printStackTrace();  
		} catch (IOException e) { 
			e.printStackTrace();  
		}
		return con;
	}
	
}
