package orco.domain;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;

@RooJavaBean
@RooToString
@RooEntity
public class Prioridad implements Comparable<Prioridad> {
	
	  private Short valor;

	  private String texto;

	  public String toString() {
	      	/*  
		  	StringBuilder sb = new StringBuilder();
        	sb.append("Id: ").append(getId()).append(", ");
        	sb.append("Version: ").append(getVersion()).append(", ");
        	sb.append("Valor: ").append(getValor()).append(", ");
        	sb.append("Texto: ").append(getTexto());
        	return sb.toString();
	      	 */
		  	return getTexto();
	    }

	@Override
	public int compareTo(Prioridad arg0) {
		if (this.getValor() != null){
			return this.getValor().compareTo(arg0.getValor());
		} else {
			return 1;
		}
	}
}
