package orco.domain;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.transaction.annotation.Transactional;

@RooJavaBean
@RooToString
@RooEntity
public class PresupuestoLinea {
	
	@ManyToOne(targetEntity = Presupuesto.class)
    @JoinColumn
    private Presupuesto presupuesto;

    private Long cantidad;

    @Size(min = 2, max = 255)
    private String descripcionTrabajo;

    @ManyToOne(targetEntity = Prioridad.class)
    @JoinColumn
    private Prioridad prioridad;

    private BigDecimal precioUnitario;
    
    private Boolean ganado;
    
    private Integer nroLinea;
    



	public BigDecimal getPrecioTotal()
    {
    	if (this.precioUnitario != null && this.cantidad != null){
    		return this.precioUnitario.multiply(new BigDecimal(this.cantidad.longValue()));
    	} else {
    		return new BigDecimal(0);
    	}    	
    }
    
    @PersistenceContext
    transient EntityManager entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    
    

    
    @Version
    @Column(name = "version")
    transient private Integer version;
    
    
    
    
    public static final EntityManager entityManager() {
        EntityManager em = new PresupuestoLinea().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    
    @Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
        this.entityManager.flush();
    }
    
    @Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            PresupuestoLinea attached = this.entityManager.find(this.getClass(), this.id);
            this.entityManager.remove(attached);
        }
        this.entityManager.flush();
    }
    
    @Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public PresupuestoLinea merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PresupuestoLinea merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static long countPresupuestoLineas() {
        TypedQuery<Long> query = entityManager().createQuery("select count(o) from PresupuestoLinea o", Long.class);
        //query.setLockMode(LockModeType.NONE);
		return query.getSingleResult();
    }
    
    public static List<PresupuestoLinea> findAllPresupuestoLineas() {
        TypedQuery<PresupuestoLinea> query = entityManager().createQuery("select o from PresupuestoLinea o", PresupuestoLinea.class);
        //query.setLockMode(LockModeType.NONE);
		return query.getResultList();
    }
    
    
    public static PresupuestoLinea findPresupuestoLinea(Long id) {
        if (id == null) return null;
        PresupuestoLinea pres = entityManager().find(PresupuestoLinea.class, id);
        //entityManager().lock(pres, LockModeType.NONE);
        return pres;
    }
    
    public static List<PresupuestoLinea> findPresupuestoLineaEntries(int firstResult, int maxResults) {
        TypedQuery<PresupuestoLinea> query = entityManager().createQuery("select o from PresupuestoLinea o", PresupuestoLinea.class);
        //query.setLockMode(LockModeType.NONE);
		return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
  
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
   
    public Integer getVersion(){
    	return this.version;
    }
    
    public void setVersion(Integer version){
    	this.version = version;
    }
    
    public Integer getNroLinea() {
		return nroLinea;
	}


	public void setNroLinea(Integer nroLinea) {
		this.nroLinea = nroLinea;
	}
    
}
