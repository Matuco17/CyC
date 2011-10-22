package orco.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.Size;
import orco.filter.OrdenTrabajoFilter;
import orco.utils.Constantes;
import orco.utils.FechaUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.transaction.annotation.Transactional;
import orco.domain.Responsable;
import java.util.HashSet;

@RooJavaBean
@RooEntity(finders = { "findOrdenTrabajoesByCliente" })
public class OrdenTrabajo {

    @ManyToOne(targetEntity = Presupuesto.class)
    @JoinColumn
    private Presupuesto presupuestoOrigen;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fechaInicio;

    private Integer horaInicio;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fechaFin;

    private Integer horaFin;

    private Float porcentajeAvance;

    @Lob
    private String materialesNecesarios;

    @ManyToOne(targetEntity = Responsable.class)
    @JoinColumn
    private Responsable responsable;

    @ManyToOne(targetEntity = Cliente.class)
    @JoinColumn
    private Cliente cliente;

    private String ordenCompra;

    @ManyToOne(targetEntity = Responsable.class)
    @JoinColumn
    private Responsable atendio;

    @Size(max = 255)
    private String material;

    private BigDecimal precioEstimado;

    @ManyToOne(targetEntity = Responsable.class)
    @JoinColumn
    private Responsable entregadoPor;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "S-")
    private Date fechaEntrega;

    @Size(max = 64)
    private String materialProvistoPor;

    @Size(max = 64)
    private String certificadoDeCalidad;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo")
    private Set<orco.domain.OrdenTrabajoLinea> lineas = new java.util.HashSet<orco.domain.OrdenTrabajoLinea>();

    @Lob()
    private String tipsBusqueda;

    @Lob()
    private String observaciones;

    private Boolean controlCalidad;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo_origen")
    private Set<Remito> remitos = new java.util.HashSet<Remito>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo_origen")
    private Set<Factura> facturas = new java.util.HashSet<Factura>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Version
    @Column(name = "version")
    private transient Integer version;

    /**
     * Metodo que devuelve la lineas de la Orden de Trabajo ademas con lineas vacias para terminar de completarlo de manera ordenada
     * @return
     */
    public List<OrdenTrabajoLinea> getLineasPresentacion() {
        List<OrdenTrabajoLinea> retValue = new ArrayList<OrdenTrabajoLinea>(this.lineas);
        while (retValue.size() < Constantes.CANTIDAD_FILAS_COMPROBANTE) {
            retValue.add(new OrdenTrabajoLinea());
        }
        Collections.sort(retValue, new ComparadorLinea());
        return retValue;
    }

    /**
     * Metodo que actualiza el valor del campo TipsBusqueda de la Orden de Trabajo
     * @param pres
     * @return
     */
    private static void actualizarTipsBusqueda(OrdenTrabajo ot) {
        StringBuilder tips = new StringBuilder();
        if (ot.presupuestoOrigen != null) tips.append("[").append(ot.presupuestoOrigen.getPresupuestoFormulario()).append("]");
        tips.append("[").append(ot.materialesNecesarios).append("]");
        tips.append("[").append(ot.ordenCompra).append("]");
        tips.append("[").append(ot.material).append("]");
        tips.append("[").append(ot.certificadoDeCalidad).append("]");
        for (OrdenTrabajoLinea otLinea : ot.getLineas()) {
            tips.append("[").append(otLinea.getDescripcion()).append("]");
        }
        ot.setTipsBusqueda(tips.toString().toUpperCase());
    }

    /**
     * Metodo factory basico con algun dato completado por defecto
     */
    public static OrdenTrabajo crear() {
        OrdenTrabajo ot = new OrdenTrabajo();
        ot.nroOrden = nextNroOrden();
        return ot;
    }

    private void setTipsBusqueda(String tipsBusqueda) {
        this.tipsBusqueda = tipsBusqueda;
    }

    /**
     * Crea una orden de trabajo desde un presupuesto teniendo en cuenta la líneas ganadas
     * @param pres
     * @return
     */
    public static OrdenTrabajo createFrom(Presupuesto pres) {
        OrdenTrabajo ot = OrdenTrabajo.crear();
        ot.fechaFin = pres.getFechaFin();
        ot.fechaInicio = pres.getFechaInicio();
        ot.horaFin = pres.getHoraFin();
        ot.horaInicio = pres.getHoraInicio();
        ot.presupuestoOrigen = pres;
        ot.cliente = pres.getCliente();
        for (PresupuestoLinea pLinea : pres.getLineas()) {
            if (pLinea.getGanado() != null && pLinea.getGanado().booleanValue()) {
                OrdenTrabajoLinea otLinea = new OrdenTrabajoLinea();
                otLinea.setCantidad(pLinea.getCantidad());
                otLinea.setDescripcion(pLinea.getDescripcionTrabajo());
                otLinea.setPrioridad(pLinea.getPrioridad());
                otLinea.setPresupuestoLineaOrigen(pLinea);
                ot.getLineas().add(otLinea);
            }
        }
        return ot;
    }

    /**
     * Devuelve los dias faltantes para la finalización de la orden de trabajo
     * @return devuelve un string con: <nro dias> dias, <nro horas> hs si es día de hoy, 
     *
     */
    public String getVencimiento() {
        if (fechaFin != null) {
            Calendar calHoy = new GregorianCalendar();
            Calendar calFin = new GregorianCalendar();
            calFin.setTimeInMillis(fechaFin.getTime());
            calFin.set(Calendar.SECOND, 0);
            calFin.set(Calendar.HOUR_OF_DAY, 0);
            if (horaFin != null) calFin.set(Calendar.HOUR_OF_DAY, horaFin / 100);
            long diferenciaEnSegundos = (calFin.getTimeInMillis() - calHoy.getTimeInMillis()) / 1000;
            long diasDiferencia = (diferenciaEnSegundos / 86400);
            if (Math.abs(diasDiferencia) == 0) {
                long horasDiferencia = (diferenciaEnSegundos / 3600);
                return String.valueOf(horasDiferencia) + "H";
            } else {
                return String.valueOf(diasDiferencia) + "D";
            }
        } else {
            return " ";
        }
    }

    /**
     * Obtiene el nombre del Cliente si este existe en la orden de trabajo
     * @return
     */
    public String getClienteNombre() {
        if (this.cliente != null) {
            return this.cliente.getNombre();
        } else {
            return "Sin Cliente.";
        }
    }

    public String getFechaHoraInicio() {
        return FechaUtils.getFechaHora(fechaInicio, horaInicio);
    }

    public String getFechaHoraFin() {
        return FechaUtils.getFechaHora(fechaFin, horaFin);
    }

    /**
     * Busqueda que se utiliza sólamente en la pantalla de Taller
     * @return
     */
    public static List<OrdenTrabajo> findOrdenTrabajoPantallaTaller() {
        StringBuilder querySB = new StringBuilder();
        querySB.append("select o from OrdenTrabajo o where (porcentajeAvance is null) or (porcentajeAvance < 100)");
        TypedQuery<OrdenTrabajo> query = entityManager().createQuery(querySB.toString(), OrdenTrabajo.class);
        return query.getResultList();
    }

    /**
     * Devuelve la cantidad de ordenes de trabajo que corresponden con el filtro
     * @param ordenTrabajoFilter
     * @return
     */
    @SuppressWarnings("unchecked")
    public static long countFindOrdenTrabajo(OrdenTrabajoFilter ordenTrabajoFilter) {
        TypedQuery<Long> query = (TypedQuery<Long>) queryFindOrdenTrabajo(ordenTrabajoFilter, true);
        return query.getSingleResult();
    }

    /**
     * Devuelve el proximo nro de Orden disponible
     * @return
     */
    @SuppressWarnings("unchecked")
    public static long nextNroOrden() {
        TypedQuery<Long> query = entityManager().createQuery("select max(o.nroOrden) from OrdenTrabajo o", Long.class);
        Long maxOrden = query.getSingleResult();
        if (maxOrden != null) {
            return maxOrden.longValue() + 1;
        } else {
            return 1;
        }
    }

    /**
     * Devuelve todos los ordenes de trabajo que correspondan con el filtro
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<OrdenTrabajo> findOrdenTrabajo(OrdenTrabajoFilter ordenTrabajoFilter, int firstResult, int maxResults) {
        TypedQuery<OrdenTrabajo> query = (TypedQuery<OrdenTrabajo>) queryFindOrdenTrabajo(ordenTrabajoFilter, false);
        return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    /**
     * Devuelve una query de acuerdo al filtro y si quere la cantidad o el listado mismo
     * @param ordenTrabajoFilter
     * @param soloContarCantidad discrimina si la query es de cantidad o de listado de objetos
     * @return
     */
    private static Query queryFindOrdenTrabajo(OrdenTrabajoFilter ordenTrabajoFilter, boolean soloContarCantidad) {
        StringBuilder querySB = new StringBuilder();
        StringBuilder whereSB = new StringBuilder();
        if (soloContarCantidad) {
            querySB.append("select count(o) from OrdenTrabajo o");
        } else {
            querySB.append("select o from OrdenTrabajo o");
        }
        String[] tipsBusqueda = null;
        if (ordenTrabajoFilter != null) {
            if (ordenTrabajoFilter.getTexto() != null && ordenTrabajoFilter.getTexto().trim().length() > 0) {
                tipsBusqueda = ordenTrabajoFilter.getTexto().split(" ");
                if (tipsBusqueda != null) {
                    for (int i = 0; i < tipsBusqueda.length; i++) {
                        whereSB.append(" and o.tipsBusqueda").append(" like :tipsBusqueda").append(i);
                    }
                }
            }
            if (ordenTrabajoFilter.getCliente() != null && ordenTrabajoFilter.getCliente().getId() > 0) {
                whereSB.append(" and o.cliente = :cliente");
            }
            if (ordenTrabajoFilter.getFechaInicioDesde() != null) {
                whereSB.append(" and o.fechaInicio >= :fechaInicioDesde");
            }
            if (ordenTrabajoFilter.getFechaInicioHasta() != null) {
                whereSB.append(" and o.fechaInicio <= :fechaInicioHasta");
            }
            if (ordenTrabajoFilter.getFechaFinDesde() != null) {
                whereSB.append(" and o.fechaFin >= :fechaFinDesde");
            }
            if (ordenTrabajoFilter.getFechaFinHasta() != null) {
                whereSB.append(" and o.fechaFin <= :fechaFinHasta");
            }
            if (ordenTrabajoFilter.getOrdenFinalizada() != null && ordenTrabajoFilter.getOrdenFinalizada().booleanValue()) {
                whereSB.append(" and o.porcentajeAvance >= 100");
            }
            if (ordenTrabajoFilter.getOrdenTrabajoNro() != null) {
                whereSB.append(" and o.nroOrden = :otNroOrden");
            }
        }
        if (whereSB.toString().trim().length() > 0) {
            querySB.append(" where ").append(whereSB.toString().substring(4));
        }
        Query query = null;
        if (soloContarCantidad) {
            query = entityManager().createQuery(querySB.toString(), Long.class);
        } else {
            query = entityManager().createQuery(querySB.toString(), OrdenTrabajo.class);
        }
        if (ordenTrabajoFilter != null) {
            if (ordenTrabajoFilter.getTexto() != null && ordenTrabajoFilter.getTexto().trim().length() > 0) {
                if (tipsBusqueda != null) {
                    for (int i = 0; i < tipsBusqueda.length; i++) {
                        query.setParameter("tipsBusqueda" + String.valueOf(i), "%" + tipsBusqueda[i].toUpperCase() + "%");
                    }
                }
            }
            if (ordenTrabajoFilter.getCliente() != null && ordenTrabajoFilter.getCliente().getId() > 0) {
                query.setParameter("cliente", ordenTrabajoFilter.getCliente());
            }
            if (ordenTrabajoFilter.getFechaInicioDesde() != null) {
                query.setParameter("fechaInicioDesde", ordenTrabajoFilter.getFechaInicioDesde());
            }
            if (ordenTrabajoFilter.getFechaInicioHasta() != null) {
                query.setParameter("fechaInicioHasta", ordenTrabajoFilter.getFechaInicioHasta());
            }
            if (ordenTrabajoFilter.getFechaFinDesde() != null) {
                query.setParameter("fechaFinDesde", ordenTrabajoFilter.getFechaFinDesde());
            }
            if (ordenTrabajoFilter.getFechaFinHasta() != null) {
                query.setParameter("fechaFinHasta", ordenTrabajoFilter.getFechaFinHasta());
            }
            if (ordenTrabajoFilter.getOrdenTrabajoNro() != null) {
                query.setParameter("otNroOrden", ordenTrabajoFilter.getOrdenTrabajoNro());
            }
        }
        return query;
    }

    public static OrdenTrabajo findOrdenTrabajo(Long id) {
        if (id == null) return null;
        return entityManager().find(OrdenTrabajo.class, id);
    }

    /**
     * Clase ordenadora de lineas
     * @author orco
     *
     */
    private class ComparadorLinea implements Comparator<OrdenTrabajoLinea> {

        @Override
        public int compare(OrdenTrabajoLinea arg0, OrdenTrabajoLinea arg1) {
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

    @PersistenceContext
    transient EntityManager entityManager;

    @Size(max = 128)
    private String responsableCliente;

    private Long nroOrden;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Responsable> responsables = new HashSet<Responsable>();

    @Transactional
    public void persist() {
        actualizarTipsBusqueda(this);
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

    @Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            for (OrdenTrabajoLinea otl : this.lineas) {
                otl.remove();
            }
            this.entityManager.remove(this);
        } else {
            OrdenTrabajo attached = this.entityManager.find(this.getClass(), this.id);
            for (OrdenTrabajoLinea otl : attached.lineas) {
                otl.remove();
            }
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

    @Transactional
    public OrdenTrabajo merge() {
        actualizarTipsBusqueda(this);
        if (this.entityManager == null) this.entityManager = entityManager();
        OrdenTrabajo merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

    public static final EntityManager entityManager() {
        EntityManager em = new OrdenTrabajo().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
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
        if (nroOrden != null) {
            return String.valueOf(nroOrden);
        } else {
            return "Sin Nro";
        }
    }


    
    
}
