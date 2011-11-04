package orco.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import orco.filter.RemitoFilter;
import orco.utils.Constantes;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.transaction.annotation.Transactional;

@RooJavaBean
//@RooToString
@RooEntity
public class Remito {
	
    @ManyToOne(targetEntity = Cliente.class)
    @JoinColumn
    private Cliente cliente;
    
    @Size(max = 20)
    private String remitoFormulario;
    
    @Size(max = 128)
    private String transporte;
	
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fecha;
    
    private BigDecimal valorDeclarado;
    	
    @Size(max= 256)
    private String observaciones;    
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "remito")
    private Set<RemitoLinea> lineas = new java.util.HashSet<RemitoLinea>();
    
    @Lob()
    private String tipsBusqueda;
    
    
    @ManyToOne(targetEntity = Presupuesto.class)
    @JoinColumn
    private Presupuesto presupuestoOrigen;
    
    @ManyToOne(targetEntity = OrdenTrabajo.class)
    @JoinColumn
    private OrdenTrabajo ordenTrabajoOrigen;
    
    /**
     * Metodo que devuelve la lineas del Remito ademas con lineas vacias para terminar de completarlo de manera ordenada
     * @return
     */
    public List<RemitoLinea> getLineasPresentacion() {
        List<RemitoLinea> retValue = new ArrayList<RemitoLinea>(this.lineas);
        while (retValue.size() < Constantes.CANTIDAD_FILAS_COMPROBANTE) {
            retValue.add(new RemitoLinea());
        }
        Collections.sort(retValue, new ComparadorLinea());
        return retValue;
    }
    
    /**
     * Metodo factory basico con algun dato completado por defecto
     */
    public static Remito crear(){
    	Remito r = new Remito();
    	
    	//Seteo por ahora un valor default para algunos campos se deberia hacer por configuracion, pero dado el caso de que muy dificil cambien no se tiene en cuenta
    	r.setFecha(new Date());
    	
    	return r;
    }
    
    /**
     * Metodo que actualiza el valor del campo TipsBusqueda del Remito
     * @param pres
     * @return
     */
    private static void actualizarTipsBusqueda(Remito rem) {
        StringBuilder tips = new StringBuilder();
        tips.append("[").append(rem.remitoFormulario).append("]");
        tips.append("[").append(rem.transporte).append("]");
        tips.append("[").append(rem.observaciones).append("]");
        for (RemitoLinea rLinea : rem.lineas) {
            tips.append("[").append(rLinea.getDescripcion()).append("]");
        }
        rem.tipsBusqueda = tips.toString().toUpperCase();
    }

    
    /**
     * Crea una factura desde un presupuesto teniendo en cuenta la líneas ganadas
     * @param pres
     * @return
     */
    public static Remito createFrom(Presupuesto pres) {
        Remito r = new Remito();
        r.fecha = new Date(); //La fecha asigno la de hoy ya que no tiene sentido ponerle la del presupuesto
        r.presupuestoOrigen = pres;
        r.ordenTrabajoOrigen = null;
        r.cliente = pres.getCliente();
        for (PresupuestoLinea pLinea : pres.getLineas()) {
            if (pLinea.getGanado() != null && pLinea.getGanado().booleanValue()) {
                RemitoLinea rLinea = new RemitoLinea();
                
                rLinea.setCantidad(pLinea.getCantidad());
                rLinea.setDescripcion(pLinea.getDescripcionTrabajo());
                rLinea.setPresupuestoLineaOrigen(pLinea);
                rLinea.setOrdenTrabajoLineaOrigen(null);
                
                r.lineas.add(rLinea);
            }
        }
        return r;
    }
    
    /**
     * Crea una factura desde una Orden de Trabajo teniendo en cuenta la líneas finalizadas y si tienen un origen desde un presupuesto linea
     * @param pres
     * @return
     */
    public static Remito createFrom(OrdenTrabajo orden) {
        Remito r = new Remito();
        r.fecha = new Date(); //La fecha asigno la de hoy ya que no tiene sentido ponerle la del presupuesto
        r.ordenTrabajoOrigen = orden;
        if (orden.getPresupuestoOrigen() != null){
        	r.presupuestoOrigen = orden.getPresupuestoOrigen();
        }
        r.cliente = orden.getCliente();
        for (OrdenTrabajoLinea otLinea : orden.getLineas()) {
            if (otLinea.getFinalizado() != null && otLinea.getFinalizado().booleanValue()) {
                RemitoLinea rLinea = new RemitoLinea();
                
                rLinea.setCantidad(otLinea.getCantidad());
                rLinea.setDescripcion(otLinea.getDescripcion());
                
                if (otLinea.getPresupuestoLineaOrigen() != null){
                    rLinea.setPresupuestoLineaOrigen(otLinea.getPresupuestoLineaOrigen());
                } else {
                	rLinea.setPresupuestoLineaOrigen(null);
                }
                rLinea.setOrdenTrabajoLineaOrigen(otLinea);
                
                r.lineas.add(rLinea);
            }
        }
        return r;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    
    @Version
    @Column(name = "version")
    private transient Integer version;

    @PersistenceContext
    transient EntityManager entityManager;

    public static final EntityManager entityManager() {
        EntityManager em = new Remito().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }    
    
    @Transactional
    public void persist() {
      	actualizarTipsBusqueda(this);
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    
    @Transactional
    public void flush() {
    	actualizarTipsBusqueda(this);
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public Remito merge() {
    	actualizarTipsBusqueda(this);
        if (this.entityManager == null) this.entityManager = entityManager();
        Remito merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    
    /**
     * Devuelve la cantidad de Remitos que corresponden con el filtro
     * @param remitoFilter
     * @return
     */
    @SuppressWarnings("unchecked")
	public static long countFindRemito(RemitoFilter remitoFilter) {
        TypedQuery<Long> query = (TypedQuery<Long>) queryFindRemito(remitoFilter, true);
        return query.getSingleResult();
    }
    
    
    
    /**
     * Devuelve todos los Remitos que correspondan con el filtro
     * @return
     */
    @SuppressWarnings("unchecked")
	public static List<Remito> findRemito(RemitoFilter remitoFilter, int firstResult, int maxResults) {
        TypedQuery<Remito> query = (TypedQuery<Remito>) queryFindRemito(remitoFilter, false);
        return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
  
    /**
     * Devuelve una query de acuerdo al filtro y si quere la cantidad o el listado mismo
     * @param remitoFilter
     * @param soloContarCantidad discrimina si la query es de cantidad o de listado de objetos
     * @return
     */
	private static Query queryFindRemito(RemitoFilter remitoFilter, boolean soloContarCantidad) {
		StringBuilder querySB = new StringBuilder();
        StringBuilder whereSB = new StringBuilder();
       
        if (soloContarCantidad){
        	querySB.append("select count(o) from Remito o");
        } else {
        	querySB.append("select o from Remito o");
        }
        String[] tipsBusqueda = null;
        
        //Armo el where con el filtro correspondiente
        if (remitoFilter != null) {
            if (remitoFilter.getTexto() != null && remitoFilter.getTexto().trim().length() > 0) {
                tipsBusqueda = remitoFilter.getTexto().split(" ");
                if (tipsBusqueda != null){
	                for (int i = 0; i < tipsBusqueda.length; i++) {
	                	whereSB.append(" and o.tipsBusqueda").append(" like :tipsBusqueda").append(i);	
					}
                }                
            }
            if (remitoFilter.getCliente() != null && remitoFilter.getCliente().getId() > 0) {
                whereSB.append(" and o.cliente = :cliente");
            }
            if (remitoFilter.getFechaDesde() != null) {
                whereSB.append(" and o.fecha >= :fechaDesde");
            }
            if (remitoFilter.getFechaHasta() != null) {
                whereSB.append(" and o.fecha <= :fechaHasta");
            }
        }
        if (whereSB.toString().trim().length() > 0) {
            querySB.append(" where ").append(whereSB.toString().substring(4));
        }
    
        Query query = null;
        if (soloContarCantidad){
          	query = entityManager().createQuery(querySB.toString(), Long.class);
        } else {
        	query = entityManager().createQuery(querySB.toString(), Remito.class);
        }
        
        //Seteo los valores del filtro
        if (remitoFilter != null) {
            if (remitoFilter.getTexto() != null && remitoFilter.getTexto().trim().length() > 0) {
                if (tipsBusqueda != null){
	                for (int i = 0; i < tipsBusqueda.length; i++) {
	                	query.setParameter("tipsBusqueda" + String.valueOf(i), "%" + tipsBusqueda[i].toUpperCase() + "%");
	                }
                }                
            }
            if (remitoFilter.getCliente() != null && remitoFilter.getCliente().getId() > 0) {
                query.setParameter("cliente", remitoFilter.getCliente());
            }
            if (remitoFilter.getFechaDesde() != null) {
                query.setParameter("fechaDesde", remitoFilter.getFechaDesde());
            }
            if (remitoFilter.getFechaHasta() != null) {
                query.setParameter("fechaHasta", remitoFilter.getFechaHasta());
            }
        }
		return query;
	}
    
    
    /**
     * Clase ordenadora de lineas
     * @author orco
     *
     */
    private class ComparadorLinea implements Comparator<RemitoLinea> {

        @Override
        public int compare(RemitoLinea arg0, RemitoLinea arg1) {
            if (arg0.getId() != null && arg1.getId() != null) {
                return arg0.getId().compareTo(arg1.getId());
            } else if (arg0.getId() != null) {
                return -1;
            } else if (arg1.getId() != null) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    
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
    
    public String toString() {
        if (remitoFormulario != null && remitoFormulario.length() > 0){
			return remitoFormulario;
		} else if (this.getId() != null){
			return "id: " + this.getId().toString();
		} else {
			return "";
		}
    }
    
}
