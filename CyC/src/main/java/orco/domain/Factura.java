package orco.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
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


import orco.filter.FacturaFilter;
import orco.utils.Constantes;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.transaction.annotation.Transactional;

@RooJavaBean
//@RooToString
@RooEntity
public class Factura {
	
    @Size(max = 20)
    private String facturaFormulario;

    @ManyToOne(targetEntity = Cliente.class)
    @JoinColumn
    private Cliente cliente;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fecha;

    @Size(max = 255)
    private String condicionesVenta;

    private BigDecimal impuesto;

    private BigDecimal bonificacion;

    @Lob()
    private String tipsBusqueda;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "factura")
    private Set<FacturaLinea> lineas = new java.util.HashSet<FacturaLinea>();
	
    
    @ManyToOne(targetEntity = Presupuesto.class)
    @JoinColumn
    private Presupuesto presupuestoOrigen;
    
    @ManyToOne(targetEntity = OrdenTrabajo.class)
    @JoinColumn
    private OrdenTrabajo ordenTrabajoOrigen;
    
    /**
     * Metodo que devuelve la lineas del Presupuesto ademas con lineas vacias para terminar de completarlo de manera ordenada
     * @return
     */
    public List<FacturaLinea> getLineasPresentacion() {
        List<FacturaLinea> retValue = new ArrayList<FacturaLinea>(this.lineas);
        while (retValue.size() < Constantes.CANTIDAD_FILAS_COMPROBANTE) {
            retValue.add(new FacturaLinea());
        }
        Collections.sort(retValue, new ComparadorLinea());
        return retValue;
    }

    private double getMultiplicadorBonificacion() {
        if (this.bonificacion == null) {
            return 1d;
        } else {
            return 1 - (this.bonificacion.doubleValue() / 100);
        }
    }

    public BigDecimal getSubtotal() {
        if (this.lineas != null && this.lineas.size() > 0) {
            double total = 0d;
            for (FacturaLinea linea : this.lineas) {
                total += linea.getPrecioTotal().doubleValue();
            }
            total *= getMultiplicadorBonificacion();
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
    public static Factura crear(){
    	Factura f = new Factura();
    	
    	//Seteo por ahora un valor default para algunos campos se deberia hacer por configuracion, pero dado el caso de que muy dificil cambien no se tiene en cuenta
    	f.setImpuesto(new BigDecimal(Constantes.PORCENTAJE_IMPUESTO));
    	f.setFecha(new Date());
    	f.setBonificacion(new BigDecimal(0));
    	
    	return f;
    }
    
    
    /**
     * Obtiene el remito si existe relacionado con la factura y devuelve el nro del mismo.
     * Warning: Tiene el potencial de realizar 4 consultas a la base de datos, por lo que no es conveniente poner este campo en listados
     * @return
     */
    public String getRemitoFormulario(){
    	/**
    	 * Exiten 4 casos: Se esplican con la relación encontrable directamente pero realmente se reducen a 2
    	 * 1. F -> OT <- R (1)
    	 * 2. F -> OT -> P <- R (2)
    	 * 3. F -> P <- R (2)
    	 * 4. F -> P <- OT <- R (1)
    	 * Se implementan en orden distinto ya que puede si tienen orden posiblemente tenga presupuesto cosa que de la otra forma puede que no
    	 */
    	Set<Remito> resultado = new HashSet<Remito>();
    	
    	//Caso 2 y 3 (F -> OT -> P <- R (2)) (F -> P <- R (2))
    	if (this.presupuestoOrigen != null)
    	{
	    	String strCaso3 = "select r from Remito r where r.presupuestoOrigen = :p";
	    	TypedQuery<Remito> query3 = entityManager().createQuery(strCaso3, Remito.class);
	    	query3.setParameter("p", this.presupuestoOrigen);
	    	resultado.addAll(query3.getResultList());	    	
	    }
    	else if (this.ordenTrabajoOrigen != null) //Caso 1 y 4 (F -> OT <- R (1)) (F -> OT -> P <- R (2))
    	{	
	    	String strCaso1 = "select r from Remito r where r.ordenTrabajoOrigen = :ot";
	    	TypedQuery<Remito> query1 = entityManager().createQuery(strCaso1, Remito.class);
	    	query1.setParameter("ot", this.ordenTrabajoOrigen);
	    	resultado.addAll(query1.getResultList());
	    }
    	
    	if (resultado.isEmpty()){
    		return null;
    	} else {
    		StringBuilder resultadoSB = new StringBuilder();
    		for (Remito remito : resultado) {
				resultadoSB.append(" / ").append(remito.getRemitoFormulario());
			}
    		return resultadoSB.substring(3); //Elimino la primera barra que ocupa 3 caracteres
    	}    	
    }
    
    /**
     * Metodo que actualiza el valor del campo TipsBusqueda de la Factura
     * @param pres
     * @return
     */
    private static void actualizarTipsBusqueda(Factura fact) {
        StringBuilder tips = new StringBuilder();
        tips.append("[").append(fact.facturaFormulario).append("]");
        tips.append("[").append(fact.condicionesVenta).append("]");
        for (FacturaLinea fLinea : fact.getLineas()) {
            tips.append("[").append(fLinea.getDescripcion()).append("]");
        }
        fact.setTipsBusqueda(tips.toString().toUpperCase());
    }

    
    /**
     * Crea una factura desde un presupuesto teniendo en cuenta la líneas ganadas
     * @param pres
     * @return
     */
    public static Factura createFrom(Presupuesto pres) {
        Factura f = Factura.crear();
        f.fecha = new Date(); //La fecha asigno la de hoy ya que no tiene sentido ponerle la del presupuesto
        f.presupuestoOrigen = pres;
        f.ordenTrabajoOrigen = null;
        f.condicionesVenta = pres.getCondicionesPago();
        f.cliente = pres.getCliente();
        f.bonificacion = pres.getBonificacion();
        f.impuesto = pres.getImpuesto();
        for (PresupuestoLinea pLinea : pres.getLineas()) {
            if (pLinea.getGanado() != null && pLinea.getGanado().booleanValue()) {
                FacturaLinea fLinea = new FacturaLinea();
                
                fLinea.setCantidad(pLinea.getCantidad());
                fLinea.setDescripcion(pLinea.getDescripcionTrabajo());
                fLinea.setPrecioUnitario(pLinea.getPrecioUnitario());
                fLinea.setPresupuestoLineaOrigen(pLinea);
                fLinea.setOrdenTrabajoLineaOrigen(null);
                
                f.getLineas().add(fLinea);
            }
        }
        return f;
    }
    
    /**
     * Crea una factura desde una Orden de Trabajo teniendo en cuenta la líneas finalizadas y si tienen un origen desde un presupuesto linea
     * @param pres
     * @return
     */
    public static Factura createFrom(OrdenTrabajo orden) {
        Factura f = Factura.crear();
        f.fecha = new Date(); //La fecha asigno la de hoy ya que no tiene sentido ponerle la del presupuesto
        f.ordenTrabajoOrigen = orden;
        if (orden.getPresupuestoOrigen() != null){
        	f.presupuestoOrigen = orden.getPresupuestoOrigen();
        	f.condicionesVenta = orden.getPresupuestoOrigen().getCondicionesPago();
        	f.bonificacion = orden.getPresupuestoOrigen().getBonificacion();
            f.impuesto = orden.getPresupuestoOrigen().getImpuesto();
                    	
        }
        f.cliente = orden.getCliente();
        for (OrdenTrabajoLinea otLinea : orden.getLineas()) {
            if (otLinea.getFinalizado() != null && otLinea.getFinalizado().booleanValue()) {
                FacturaLinea fLinea = new FacturaLinea();
                
                fLinea.setCantidad(otLinea.getCantidad());
                fLinea.setDescripcion(otLinea.getDescripcion());
                
                if (otLinea.getPresupuestoLineaOrigen() != null){
                	fLinea.setPrecioUnitario(otLinea.getPresupuestoLineaOrigen().getPrecioUnitario());
                    fLinea.setPresupuestoLineaOrigen(otLinea.getPresupuestoLineaOrigen());
                } else {
                	fLinea.setPrecioUnitario(new BigDecimal(0));
                    fLinea.setPresupuestoLineaOrigen(null);
                }
                fLinea.setOrdenTrabajoLineaOrigen(otLinea);
                
                f.getLineas().add(fLinea);
            }
        }
        return f;
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
        EntityManager em = new Factura().entityManager;
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
    public Factura merge() {
    	actualizarTipsBusqueda(this);
        if (this.entityManager == null) this.entityManager = entityManager();
        Factura merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    /**
     * Devuelve la cantidad de Facturas que corresponden con el filtro
     * @param ordenTrabajoFilter
     * @return
     */
    @SuppressWarnings("unchecked")
	public static long countFindFactura(FacturaFilter facturaFilter) {
        TypedQuery<Long> query = (TypedQuery<Long>) queryFindFactura(facturaFilter, true);
        return query.getSingleResult();
    }
    
    
    /**
     * Devuelve todas las Facturas que correspondan con el filtro
     * @return
     */
    @SuppressWarnings("unchecked")
	public static List<Factura> findFactura(FacturaFilter facturaFilter, int firstResult, int maxResults) {
        TypedQuery<Factura> query = (TypedQuery<Factura>) queryFindFactura(facturaFilter, false);
        return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    /**
     * Devuelve una query de acuerdo al filtro y si quere la cantidad o el listado mismo
     * @param facturaFilter
     * @param soloContarCantidad discrimina si la query es de cantidad o de listado de objetos
     * @return
     */
	private static Query queryFindFactura(FacturaFilter facturaFilter, boolean soloContarCantidad) {
		StringBuilder querySB = new StringBuilder();
        StringBuilder whereSB = new StringBuilder();
        
        if (soloContarCantidad){
        	querySB.append("select count(o) from Factura o");
        } else {
        	querySB.append("select o from Factura o");
        }
        String[] tipsBusqueda = null;
        
        if (facturaFilter != null) {
            if (facturaFilter.getTexto() != null && facturaFilter.getTexto().trim().length() > 0) {
                tipsBusqueda = facturaFilter.getTexto().split(" ");
                if (tipsBusqueda != null){
	                for (int i = 0; i < tipsBusqueda.length; i++) {
	                	whereSB.append(" and o.tipsBusqueda").append(" like :tipsBusqueda").append(i);	
					}
                }                
            }
            if (facturaFilter.getCliente() != null && facturaFilter.getCliente().getId() > 0) {
                whereSB.append(" and o.cliente = :cliente");
            }
            if (facturaFilter.getFechaDesde() != null) {
                whereSB.append(" and o.fecha >= :fechaDesde");
            }
            if (facturaFilter.getFechaHasta() != null) {
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
        	query = entityManager().createQuery(querySB.toString(), Factura.class);
        }
        
        if (facturaFilter != null) {
            if (facturaFilter.getTexto() != null && facturaFilter.getTexto().trim().length() > 0) {
                if (tipsBusqueda != null){
	                for (int i = 0; i < tipsBusqueda.length; i++) {
	                	query.setParameter("tipsBusqueda" + String.valueOf(i), "%" + tipsBusqueda[i].toUpperCase() + "%");
	                }
                }                
            }
            if (facturaFilter.getCliente() != null && facturaFilter.getCliente().getId() > 0) {
                query.setParameter("cliente", facturaFilter.getCliente());
            }
            if (facturaFilter.getFechaDesde() != null) {
                query.setParameter("fechaDesde", facturaFilter.getFechaDesde());
            }
            if (facturaFilter.getFechaHasta() != null) {
                query.setParameter("fechaHasta", facturaFilter.getFechaHasta());
            }
        }
		return query;
	}
    
	public String toString(){
		if (facturaFormulario != null && facturaFormulario.length() > 0){
			return facturaFormulario;
		} else if (this.getId() != null){
			return "id: " + this.getId().toString();
		} else {
			return "";
		}
	}
	
    
    /**
     * Clase ordenadora de lineas
     * @author orco
     *
     */
    private class ComparadorLinea implements Comparator<FacturaLinea> {

        @Override
        public int compare(FacturaLinea arg0, FacturaLinea arg1) {
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
}
