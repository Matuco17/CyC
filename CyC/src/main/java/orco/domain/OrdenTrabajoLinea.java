package orco.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;

@RooJavaBean
@RooToString
@RooEntity
public class OrdenTrabajoLinea {
	
	@ManyToOne(targetEntity = OrdenTrabajo.class)
    @JoinColumn
    private OrdenTrabajo ordenTrabajo;

    private Long cantidad;

    @Size(max = 255)
    private String descripcion;

    @ManyToOne(targetEntity = PresupuestoLinea.class)
    @JoinColumn
    private PresupuestoLinea presupuestoLineaOrigen;
    
    @ManyToOne(targetEntity = Prioridad.class)
    @JoinColumn
    private Prioridad prioridad;
    
    private Boolean finalizado;
    
    private Long item;
    


	/**
     * Campos agregados para la impresion de la orden de trabajo
     */
    
    public Long getOrdenTrajbajoId(){
    	if (ordenTrabajo != null){
    		return ordenTrabajo.getId();
    	} else {
    		return null;
    	}
    }
    
    public String getOrdenTrajbajoClienteNombre(){
    	if (ordenTrabajo != null && ordenTrabajo.getCliente() != null){
    		return ordenTrabajo.getCliente().getNombre();
    	} else {
    		return null;
    	}
    }
    
    public String getOrdenTrajbajoClienteTelefono(){
    	if (ordenTrabajo != null && ordenTrabajo.getCliente() != null){
    		return ordenTrabajo.getCliente().getTelefono();
    	} else {
    		return null;
    	}
    }
    
    public String getOrdenTrajbajoResponsable(){
    	if (ordenTrabajo != null && ordenTrabajo.getResponsable() != null){
    		return ordenTrabajo.getResponsable().toString();
    	} else {
    		return null;
    	}
    }
    
 
    
    
    
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    
    @Version
    @Column(name = "version")
    private transient Integer version;
    
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    
    public Integer getVersion() {
        return this.version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
}
