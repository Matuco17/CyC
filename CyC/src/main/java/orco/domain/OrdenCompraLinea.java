package orco.domain;

import java.math.BigDecimal;

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
public class OrdenCompraLinea {
	
	@ManyToOne(targetEntity = OrdenCompra.class)
    @JoinColumn
    private OrdenCompra ordenCompra;

    private Long cantidad;

    @Size(min = 2, max = 255)
    private String descripcion;
   
    private BigDecimal precioUnitario;
	
	
    
    public BigDecimal getPrecioTotal()
    {
    	if (this.precioUnitario != null && this.cantidad != null){
    		return this.precioUnitario.multiply(new BigDecimal(this.cantidad.longValue()));
    	} else {
    		return new BigDecimal(0);
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
