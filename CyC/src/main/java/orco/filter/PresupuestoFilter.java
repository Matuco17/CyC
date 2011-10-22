package orco.filter;

import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import orco.domain.Cliente;

public class PresupuestoFilter extends BasicFilter{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4123984173209293L;

	private Cliente cliente;
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style="S-")
    private Date fechaDesde;
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style="S-")
    private Date fechaHasta;
	
	
	public Cliente getCliente() {
		return cliente;
	}
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	public Date getFechaDesde() {
		return fechaDesde;
	}
	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}
	public Date getFechaHasta() {
		return fechaHasta;
	}
	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}
	
	
	
}
