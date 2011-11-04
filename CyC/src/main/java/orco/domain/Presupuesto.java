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

import orco.filter.PresupuestoFilter;
import orco.utils.*;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.transaction.annotation.Transactional;

@RooJavaBean
@RooToString
@RooEntity(finders = { "findPresupuestoesByFecha" })
public class Presupuesto {

    @Size(max = 50)
    private String presupuestoFormulario;

    @ManyToOne(targetEntity = Cliente.class)
    @JoinColumn
    private Cliente cliente;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fecha;

    @Size(max = 255)
    private String condicionesPago;

    @Size(max = 255)
    private String observaciones;

    private BigDecimal impuesto;

    private BigDecimal bonificacion;

    @Lob()
    private String estadoGlobalPresupuesto;

    @Lob()
    private String tipsBusqueda;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fechaInicio;

    private Integer horaInicio;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fechaFin;

    private Integer horaFin;

    @Lob()
    private String comentarios;
    
       

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto")
    private Set<PresupuestoLinea> lineas = new java.util.HashSet<PresupuestoLinea>();

    
    
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_origen")
    private Set<OrdenTrabajo> ordenes = new java.util.HashSet<OrdenTrabajo>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_origen")
    private Set<Remito> remitos = new java.util.HashSet<Remito>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_origen")
    private Set<Factura> facturas = new java.util.HashSet<Factura>();

    
    
    
    
    /**
     * Metodo que devuelve la lineas del Presupuesto ademas con lineas vacias para terminar de completarlo de manera ordenada
     * @return
     */
    public List<PresupuestoLinea> getLineasPresentacion() {
        List<PresupuestoLinea> retValue = new ArrayList<PresupuestoLinea>(this.lineas);
        while (retValue.size() < Constantes.CANTIDAD_FILAS_COMPROBANTE) {
            retValue.add(new PresupuestoLinea());
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

    public BigDecimal getSubtotalPresupuesto() {
        if (this.lineas != null && this.lineas.size() > 0) {
            double total = 0d;
            for (PresupuestoLinea linea : this.lineas) {
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
            double imp = this.getSubtotalPresupuesto().doubleValue() * this.impuesto.doubleValue();
        	return new BigDecimal(Math.round(imp)).divide(new BigDecimal(100));
        } else {
            return new BigDecimal(0);
        }
    }

    public BigDecimal getTotalPresupuesto() {
        if (this.lineas != null && this.lineas.size() > 0) {
            if (this.impuesto != null) {
                return this.getSubtotalPresupuesto().add(this.getTotalImpuesto());
            } else {
                return this.getSubtotalPresupuesto();
            }
        } else {
            return new BigDecimal(0);
        }
    }

    public String getFechaHoraInicio(){
    	return FechaUtils.getFechaHora(fechaInicio, horaInicio);
    }
    
    public String getFechaHoraFin(){
    	return FechaUtils.getFechaHora(fechaFin, horaFin);
    }

    /**
     * Metodo factory basico con algun dato completado por defecto
     */
    public static Presupuesto crear(){
    	Presupuesto p = new Presupuesto();
    	
    	//Seteo por ahora un valor default para algunos campos se deberia hacer por configuracion, pero dado el caso de que muy dificil cambien no se tiene en cuenta
    	p.setImpuesto(new BigDecimal(Constantes.PORCENTAJE_IMPUESTO));
    	p.setFecha(new Date());
    	p.setBonificacion(new BigDecimal(0));
    	
    	return p;
    }
    
    
    
    /**
     * Metodo que actualiza el valor del campo TipsBusqueda del presupuesto
     * @param pres
     * @return
     */
    private static void actualizarTipsBusqueda(Presupuesto pres) {
        StringBuilder tips = new StringBuilder();
        tips.append("[").append(pres.presupuestoFormulario).append("]");
        tips.append("[").append(pres.comentarios).append("]");
        tips.append("[").append(pres.condicionesPago).append("]");
        tips.append("[").append(pres.estadoGlobalPresupuesto).append("]");
        tips.append("[").append(pres.observaciones).append("]");
        for (PresupuestoLinea pLinea : pres.getLineas()) {
            tips.append("[").append(pLinea.getDescripcionTrabajo()).append("]");
        }
        pres.setTipsBusqueda(tips.toString().toUpperCase());
    }

    private void setTipsBusqueda(String tipsBusqueda) {
        this.tipsBusqueda = tipsBusqueda;
    }

    /**
     * Clase ordenadora de lineas
     * @author orco
     *
     */
    private class ComparadorLinea implements Comparator<PresupuestoLinea> {

        @Override
        public int compare(PresupuestoLinea arg0, PresupuestoLinea arg1) {
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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @PersistenceContext
    transient EntityManager entityManager;

    public static final EntityManager entityManager() {
        EntityManager em = new Presupuesto().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

    @Transactional
    public void persist() {
        actualizarTipsBusqueda(this);
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
        entityManager.flush();
    }

    @Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
        	this.entityManager.remove(this);
        } else {
            Presupuesto attached = this.entityManager.find(this.getClass(), this.id);
            this.entityManager.remove(attached);
        }
        entityManager.flush();
    }

    @Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

    @Transactional
    public Presupuesto merge() {
        actualizarTipsBusqueda(this);
        if (this.entityManager == null) this.entityManager = entityManager();
        Presupuesto merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

    public static long countPresupuestoes() {
        TypedQuery<Long> query = entityManager().createQuery("select count(o) from Presupuesto o", Long.class);
        return query.getSingleResult();
    }

    public static List<Presupuesto> findAllPresupuestoes() {
        TypedQuery<Presupuesto> query = entityManager().createQuery("select o from Presupuesto o", Presupuesto.class);
        return query.getResultList();
    }

    @Transactional
    public static Presupuesto findPresupuesto(Long id) {
        if (id == null) return null;
        Presupuesto pres = entityManager().find(Presupuesto.class, id);
        return pres;
    }

    public static List<Presupuesto> findPresupuestoEntries(int firstResult, int maxResults) {
        TypedQuery<Presupuesto> query = entityManager().createQuery("select o from Presupuesto o", Presupuesto.class);
        return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    /**
     * Devuelve la cantidad de Presupuestos que corresponden con el filtro
     * @param ordenTrabajoFilter
     * @return
     */
    @SuppressWarnings("unchecked")
	public static long countFindPresupuesto(PresupuestoFilter presupuestoFilter) {
        TypedQuery<Long> query = (TypedQuery<Long>) queryFindPresupuesto(presupuestoFilter, true);
        return query.getSingleResult();
    }
    
    /**
     * Devuelve todos los presupuestos que correspondan con el filtro
     * @return
     */
    @SuppressWarnings("unchecked")
	public static List<Presupuesto> findPresupuesto(PresupuestoFilter presupuestoFilter, int firstResult, int maxResults) {
    	TypedQuery<Presupuesto> query = (TypedQuery<Presupuesto>) queryFindPresupuesto(presupuestoFilter, false);
        return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    /**
     * Devuelve una query de acuerdo al filtro y si quere la cantidad o el listado mismo
     * @param presupuestoFilter
     * @param soloContarCantidad iscrimina si la query es de cantidad o de listado de objetos
     * @return
     */
	private static Query queryFindPresupuesto(PresupuestoFilter presupuestoFilter, boolean soloContarCantidad) {
		StringBuilder querySB = new StringBuilder();
        StringBuilder whereSB = new StringBuilder();
        
        if (soloContarCantidad){
        	querySB.append("select count(o) from Presupuesto o");
        } else {
        	querySB.append("select o from Presupuesto o");
        }
        String[] tipsBusqueda = null;
        
        //Creo el where con los filtros correspondientes
        if (presupuestoFilter != null) {
            if (presupuestoFilter.getTexto() != null && presupuestoFilter.getTexto().trim().length() > 0) {
                tipsBusqueda = presupuestoFilter.getTexto().split(" ");
                if (tipsBusqueda != null){
	                for (int i = 0; i < tipsBusqueda.length; i++) {
	                	whereSB.append(" and o.tipsBusqueda").append(" like :tipsBusqueda").append(i);	
					}
                }                
            }
            if (presupuestoFilter.getCliente() != null && presupuestoFilter.getCliente().getId() > 0) {
                whereSB.append(" and o.cliente = :cliente");
            }
            if (presupuestoFilter.getFechaDesde() != null) {
                whereSB.append(" and o.fecha >= :fechaDesde");
            }
            if (presupuestoFilter.getFechaHasta() != null) {
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
        	query = entityManager().createQuery(querySB.toString(), Presupuesto.class);
        }
        
        //Seteo los parametros
        if (presupuestoFilter != null) {
            if (presupuestoFilter.getTexto() != null && presupuestoFilter.getTexto().trim().length() > 0) {
                if (tipsBusqueda != null){
	                for (int i = 0; i < tipsBusqueda.length; i++) {
	                	query.setParameter("tipsBusqueda" + String.valueOf(i), "%" + tipsBusqueda[i].toUpperCase() + "%");
	                }
                }                
            }
            if (presupuestoFilter.getCliente() != null && presupuestoFilter.getCliente().getId() > 0) {
                query.setParameter("cliente", presupuestoFilter.getCliente());
            }
            if (presupuestoFilter.getFechaDesde() != null) {
                query.setParameter("fechaDesde", presupuestoFilter.getFechaDesde());
            }
            if (presupuestoFilter.getFechaHasta() != null) {
                query.setParameter("fechaHasta", presupuestoFilter.getFechaHasta());
            }
        }
		return query;
	}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "version")
    private transient Integer version;

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
    
    
    
    public String toString() {
        if (presupuestoFormulario != null && presupuestoFormulario.length() > 0){
			return presupuestoFormulario;
		} else if (this.getId() != null){
			return "id: " + this.getId().toString();
		} else {
			return "";
		}
    }


	public Set<OrdenTrabajo> getOrdenes() {
		return ordenes;
	}

	public void setOrdenes(Set<OrdenTrabajo> ordenes) {
		this.ordenes = ordenes;
	}

	public Set<Remito> getRemitos() {
		return remitos;
	}

	public void setRemitos(Set<Remito> remitos) {
		this.remitos = remitos;
	}

	public Set<Factura> getFacturas() {
		return facturas;
	}

	public void setFacturas(Set<Factura> facturas) {
		this.facturas = facturas;
	}
    
    
}
