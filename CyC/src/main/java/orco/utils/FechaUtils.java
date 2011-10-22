package orco.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FechaUtils {

	private static DateFormat formateadorFecha = new SimpleDateFormat("dd/MM/yy");
	
	/**
	 * Obtiene el string formateado de fecha y hora de acuerdo a un datepicker y un time picker utilizado en el sistema
	 * @param fecha
	 * @param hora
	 * @return
	 */
	public static String getFechaHora(Date fecha, Integer hora){
		StringBuilder sb = new StringBuilder();		
		if (fecha != null){
			sb.append(formateadorFecha.format(fecha));
		}
		if (hora != null){
			int h = hora.intValue() / 100;
			sb.append((h < 10)? " 0" : " ").append(String.valueOf(h)).append(":00");
		}		
		return sb.toString();
	}
	
}
