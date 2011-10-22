package orco.domain;

import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.entity.RooEntity;

@RooJavaBean
//@RooToString
@RooEntity
public class Proveedor {
	
	@Size(max=256)
	private String nombre;
	
	@Size(max=6)
	private String codigo;
	
	@Size(max=256)
	private String direccion;
	
	@Size(max=128)
	private String cuidad;
	
	@Size(max=20)
	private String cuit;
	
	@Size(max=128)
	private String telefono;
	
	@Size(max=128)
	private String provincia;
	
	@Size(max=128)
	private String email;
	
	@Size(max=128)
	private String web;
	
    public int compareTo(Proveedor o) {
        if (this.getNombre() != null) {
            return this.getNombre().compareToIgnoreCase(o.getNombre());
        } else {
            return 1;
        }
    }
    
    public String toString() {
        return nombre;
    }
	
	
	
	
	
}
