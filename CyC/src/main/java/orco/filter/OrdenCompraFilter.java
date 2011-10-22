package orco.filter;

import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import orco.domain.Proveedor;

public class OrdenCompraFilter extends BasicFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 892813791872319L;


	private Proveedor proveedor;
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style="S-")
    private Date fechaDesde;
	
	
	@Temporal(TemporalType.DATE)
    @DateTimeFormat(style="S-")
    private Date fechaHasta;
	
	public Proveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
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
