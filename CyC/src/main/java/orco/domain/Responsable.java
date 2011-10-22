package orco.domain;

import java.io.Serializable;

import javax.validation.constraints.Size;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;

@RooJavaBean
@RooToString
@RooEntity(finders = { "findResponsablesByUsername" })
public class Responsable implements Serializable {

    private transient static final long serialVersionUID = 2464916768155916420L;

	private String apellido;

    private String nombres;

    @Size(max = 30)
    private String username;

    @Size(max = 30)
    private String password;

    private Boolean esOperario;

    private Boolean esAdministrativo;
    
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
        sb.append(getApellido());
        sb.append(", ");
        sb.append(getNombres());
        return sb.toString();
    }
}
