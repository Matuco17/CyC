package orco.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;


public class Corrector {

	private static Object[] NO_ARGUMENTS_ARRAY = new Object[] {
    };
	
	/**
	 * Corrijo el elemento en algun que otro campo general para que no produzca errores antes de la grabacion
	 * @param elemento
	 */
	public static void corregirElemento(Object elemento){
		BeanInfo bInfo;
		try {
			bInfo = Introspector.getBeanInfo(elemento.getClass());
		
		
			PropertyDescriptor[] propDescs = bInfo.getPropertyDescriptors();
			for (PropertyDescriptor propDesc : propDescs) {
			
				if (propDesc.getPropertyType().equals(Date.class)){
					//Si es fecha, por un error de horas, parece que toma un dia menos, por eso le sumo unas horas
					corregirFecha(elemento, propDesc);
				}
				/* Comente esto ya que en teoria solucione el problema del encoding issue reordenando filtros en el web.xml
				  else if (propDesc.getPropertyType().equals(String.class)){
					corregirString(elemento, propDesc);
				}*/
				
			}
			
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}		
	}

	
	/**
	 * Si es fecha, por un error de horas, parece que toma un dia menos, por eso le sumo unas horas
	 * @param elemento
	 * @param propDesc
	 */
	private static void corregirFecha(Object elemento,	PropertyDescriptor propDesc) {
		try {
			Method readMethod = propDesc.getReadMethod();
	        if (readMethod != null) {
	        	Date fecha = (Date) readMethod.invoke(elemento, NO_ARGUMENTS_ARRAY);
	        	
				if (fecha != null){
					fecha.setTime(fecha.getTime() + 7200000); //Le sumo 2 horas en milisegundos
					
					Method writeMethod = propDesc.getWriteMethod();
					writeMethod.invoke(elemento, new Object[] { fecha });
				}
	        }
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Si es string, corrige todos los caracteres con encoding equivocado.
	 * Este parche no sirve como definitivo ya que no funciona con las mayusculas.
	 * @param elemento
	 * @param propDesc
	 
	private static void corregirString(Object elemento,	PropertyDescriptor propDesc) {
		try {
			Method readMethod = propDesc.getReadMethod();
	        if (readMethod != null) {
	        	String st = (String) readMethod.invoke(elemento, NO_ARGUMENTS_ARRAY);
	        	
				if (st != null && !st.isEmpty()){
					//Realizo la correccion propia
					st = st.replaceAll("Ã¡", "á");
					st = st.replaceAll("Ã©", "é");
					st = st.replaceAll("Ã­", "í");
					st = st.replaceAll("Ã³", "ó");
					st = st.replaceAll("Ãº", "ú");
					st = st.replaceAll("Ã±", "ñ");
					
					Method writeMethod = propDesc.getWriteMethod();
					writeMethod.invoke(elemento, new Object[] { st });
				}
	        }
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			//No hago nada con esta excepcion, ya que no veo porque falla si cuando quiero que ande bien anda bien
			//e.printStackTrace();
		}
	}
	*/
	
		
}
