package orco.filter;

import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import orco.domain.Cliente;

public class OrdenTrabajoFilter extends BasicFilter {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5244156271L;

	private Cliente cliente;
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style="S-")
	private Date fechaInicioDesde;
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style="S-")
	private Date fechaInicioHasta;
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style="S-")
	private Date fechaFinDesde;
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style="S-")
	private Date fechaFinHasta;
	
	private Boolean ordenFinalizada;
		
	private Long ordenTrabajoNro;
	
	
	public Long getOrdenTrabajoNro() {
		return ordenTrabajoNro;
	}
	public void setOrdenTrabajoNro(Long ordenTrabajoNro) {
		this.ordenTrabajoNro = ordenTrabajoNro;
	}
	public Boolean getOrdenFinalizada() {
		return ordenFinalizada;
	}
	public void setOrdenFinalizada(Boolean ordenFinalizada) {
		this.ordenFinalizada = ordenFinalizada;
	}
	public Date getFechaInicioDesde() {
		return fechaInicioDesde;
	}
	public void setFechaInicioDesde(Date fechaInicioDesde) {
		this.fechaInicioDesde = fechaInicioDesde;
	}
	public Date getFechaInicioHasta() {
		return fechaInicioHasta;
	}
	public void setFechaInicioHasta(Date fechaInicioHasta) {
		this.fechaInicioHasta = fechaInicioHasta;
	}
	public Date getFechaFinDesde() {
		return fechaFinDesde;
	}
	public void setFechaFinDesde(Date fechaFinDesde) {
		this.fechaFinDesde = fechaFinDesde;
	}
	public Date getFechaFinHasta() {
		return fechaFinHasta;
	}
	public void setFechaFinHasta(Date fechaFinHasta) {
		this.fechaFinHasta = fechaFinHasta;
	}
	
	public Cliente getCliente() {
		return cliente;
	}
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	
	
	
}
