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

import orco.filter.OrdenCompraFilter;
import orco.utils.Constantes;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.transaction.annotation.Transactional;

@RooJavaBean
//@RooToString
@RooEntity
public class OrdenCompra {
	
	private Long ordenCompraNro;
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fecha;
	
	@ManyToOne(targetEntity = Proveedor.class)
	@JoinColumn
	private Proveedor proveedor;
	
	@ManyToOne(targetEntity = OrdenTrabajo.class)
	@JoinColumn
	private OrdenTrabajo ordenTrabajo;
	
	@Size(max=64)
	private String remitoProveedorNro;
	
	@ManyToOne(targetEntity = Responsable.class)
	@JoinColumn
	private Responsable aprobo;
	
	@Size(max=256)
	private String condicionesPago;
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fechaEntrega;
	
	private BigDecimal impuesto;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra")
    private Set<OrdenCompraLinea> lineas = new java.util.HashSet<OrdenCompraLinea>();
    
    @Lob()
    private String tipsBusqueda;
	
    
    /**
     * Metodo que devuelve la lineas del Presupuesto ademas con lineas vacias para terminar de completarlo de manera ordenada
     * @return
     */
    public List<OrdenCompraLinea> getLineasPresentacion() {
        List<OrdenCompraLinea> retValue = new ArrayList<OrdenCompraLinea>(this.lineas);
        while (retValue.size() < Constantes.CANTIDAD_FILAS_COMPROBANTE) {
            retValue.add(new OrdenCompraLinea());
        }
        Collections.sort(retValue, new ComparadorLinea());
        return retValue;
    }
    /*
    private double getMultiplicadorBonificacion() {
        if (this.bonificacion == null) {
            return 1d;
        } else {
            return 1 - (this.bonificacion.doubleValue() / 100);
        }
    }
    */

    public BigDecimal getSubtotal() {
        if (this.lineas != null && this.lineas.size() > 0) {
            double total = 0d;
            for (OrdenCompraLinea linea : this.lineas) {
                total += linea.getPrecioTotal().doubleValue();
            }
            //total *= getMultiplicadorBonificacion();
            return new BigDecimal(Math.round(total * 100)).divide(new BigDecimal(100));
        } else {
            return new BigDecimal(0);
        }
    }

    public BigDecimal getTotalImpuesto() {
        if (this.impuesto != null && this.lineas != null && this.lineas.size() > 0) {
            double imp = this.getSubtotal().doubleValue() * this.impuesto.doubleValue();
        	return new BigDecimal(Math.round(imp)).divide(new BigDecimal(100));
        } else {
            return new BigDecimal(0);
        }
    }

    public BigDecimal getTotal() {
        if (this.lineas != null && this.lineas.size() > 0) {
            if (this.impuesto != null) {
                return this.getSubtotal().add(this.getTotalImpuesto());
            } else {
                return this.getSubtotal();
            }
        } else {
            return new BigDecimal(0);
        }
    }
    
    /**
     * Metodo factory basico con algun dato completado por defecto
     */
    public static OrdenCompra crear(){
    	OrdenCompra oc = new OrdenCompra();
    	oc.ordenCompraNro = nextNroOrden();
    	
    	//Seteo por ahora un valor default para algunos campos se deberia hacer por configuracion, pero dado el caso de que muy dificil cambien no se tiene en cuenta
    	oc.setImpuesto(new BigDecimal(Constantes.PORCENTAJE_IMPUESTO));
    	oc.setFecha(new Date());
    	
    	return oc;
    }
    
    
    /**
     * Metodo que actualiza el valor del campo TipsBusqueda de la Factura
     * @param pres
     * @return
     */
    private static void actualizarTipsBusqueda(OrdenCompra oc) {
        StringBuilder tips = new StringBuilder();
        tips.append("[").append(oc.ordenCompraNro).append("]");
        tips.append("[").append(oc.condicionesPago).append("]");
        tips.append("[").append(oc.remitoProveedorNro).append("]");
        if (oc.ordenTrabajo !=  null)
        	tips.append("[").append(oc.ordenTrabajo.getNroOrden()).append("]");
        
        for (OrdenCompraLinea ocLinea : oc.getLineas()) {
            tips.append("[").append(ocLinea.getDescripcion()).append("]");
        }
        oc.setTipsBusqueda(tips.toString().toUpperCase());
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
        EntityManager em = new OrdenCompra().entityManager;
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
    public OrdenCompra merge() {
    	actualizarTipsBusqueda(this);
        if (this.entityManager == null) this.entityManager = entityManager();
        OrdenCompra merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    /**
     * Devuelve la cantidad de Ordenes de compra que corresponden con el filtro
     * @param ordenTrabajoFilter
     * @return
     */
    @SuppressWarnings("unchecked")
	public static long countFindOrdenCompra(OrdenCompraFilter ocFilter) {
        TypedQuery<Long> query = (TypedQuery<Long>) queryFindOrdenCompra(ocFilter, true);
        return query.getSingleResult();
    }
    
    
    /**
     * Devuelve todas las Ordenes de compra que correspondan con el filtro
     * @return
     */
    @SuppressWarnings("unchecked")
	public static List<OrdenCompra> findOrdenCompra(OrdenCompraFilter ocFilter, int firstResult, int maxResults) {
        TypedQuery<OrdenCompra> query = (TypedQuery<OrdenCompra>) queryFindOrdenCompra(ocFilter, false);
        return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    /**
     * Devuelve el proximo nro de Orden disponible
     * @return
     */
    @SuppressWarnings("unchecked")
    public static long nextNroOrden() {
        TypedQuery<Long> query = entityManager().createQuery("select max(o.ordenCompraNro) from OrdenCompra o", Long.class);
		Long maxOrden = query.getSingleResult();
		if (maxOrden != null){ //pongo este contro por las pruebas en base limpias que no andan ya que tira null el resultado de la consulta
			return maxOrden.longValue() + 1;
		} else {
			return 1;
		}
			
    }
    
    
    /**
     * Devuelve una query de acuerdo al filtro y si quere la cantidad o el listado mismo
     * @param ordenCompraFilter
     * @param soloContarCantidad discrimina si la query es de cantidad o de listado de objetos
     * @return
     */
	private static Query queryFindOrdenCompra(OrdenCompraFilter ordenCompraFilter, boolean soloContarCantidad) {
		StringBuilder querySB = new StringBuilder();
        StringBuilder whereSB = new StringBuilder();
        
        if (soloContarCantidad){
        	querySB.append("select count(o) from OrdenCompra o");
        } else {
        	querySB.append("select o from OrdenCompra o");
        }
        String[] tipsBusqueda = null;
        
        if (ordenCompraFilter != null) {
            if (ordenCompraFilter.getTexto() != null && ordenCompraFilter.getTexto().trim().length() > 0) {
                tipsBusqueda = ordenCompraFilter.getTexto().split(" ");
                if (tipsBusqueda != null){
	                for (int i = 0; i < tipsBusqueda.length; i++) {
	                	whereSB.append(" and o.tipsBusqueda").append(" like :tipsBusqueda").append(i);	
					}
                }                
            }
            if (ordenCompraFilter.getProveedor() != null && ordenCompraFilter.getProveedor().getId() > 0) {
                whereSB.append(" and o.proveedor = :proveedor");
            }
            if (ordenCompraFilter.getFechaDesde() != null) {
                whereSB.append(" and o.fecha >= :fechaDesde");
            }
            if (ordenCompraFilter.getFechaHasta() != null) {
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
        	query = entityManager().createQuery(querySB.toString(), OrdenCompra.class);
        }
        
        if (ordenCompraFilter != null) {
            if (ordenCompraFilter.getTexto() != null && ordenCompraFilter.getTexto().trim().length() > 0) {
                if (tipsBusqueda != null){
	                for (int i = 0; i < tipsBusqueda.length; i++) {
	                	query.setParameter("tipsBusqueda" + String.valueOf(i), "%" + tipsBusqueda[i].toUpperCase() + "%");
	                }
                }                
            }
            if (ordenCompraFilter.getProveedor() != null && ordenCompraFilter.getProveedor().getId() > 0) {
                query.setParameter("proveedor", ordenCompraFilter.getProveedor());
            }
            if (ordenCompraFilter.getFechaDesde() != null) {
                query.setParameter("fechaDesde", ordenCompraFilter.getFechaDesde());
            }
            if (ordenCompraFilter.getFechaHasta() != null) {
                query.setParameter("fechaHasta", ordenCompraFilter.getFechaHasta());
            }
        }
		return query;
	}
    
    
    /**
     * Clase ordenadora de lineas
     * @author orco
     *
     */
    private class ComparadorLinea implements Comparator<OrdenCompraLinea> {

        @Override
        public int compare(OrdenCompraLinea arg0, OrdenCompraLinea arg1) {
        	if (arg0.getNroLinea() != null && arg1.getNroLinea() != null) {
                return arg0.getNroLinea().compareTo(arg1.getNroLinea());
            } else if (arg0.getNroLinea() != null) {
                return -1;
            } else if (arg1.getNroLinea() != null) {
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
	
}
