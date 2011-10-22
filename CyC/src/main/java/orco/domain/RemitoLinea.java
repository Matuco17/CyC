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
public class RemitoLinea {
	
	@ManyToOne(targetEntity = Remito.class)
    @JoinColumn
    private Remito remito;

    private Long cantidad;

    @Size(max = 255)
    private String descripcion;
    
    @ManyToOne(targetEntity = PresupuestoLinea.class)
    @JoinColumn
    private PresupuestoLinea presupuestoLineaOrigen;
    
    @ManyToOne(targetEntity = OrdenTrabajoLinea.class)
    @JoinColumn
    private OrdenTrabajoLinea ordenTrabajoLineaOrigen;
 
	
    
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
