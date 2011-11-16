package orco.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jxls.transformer.XLSTransformer;
import orco.domain.Cliente;
import orco.domain.FacturaLinea;
import orco.domain.OrdenCompra;
import orco.domain.OrdenCompraLinea;
import orco.domain.OrdenTrabajo;
import orco.domain.OrdenTrabajoLinea;
import orco.domain.Presupuesto;
import orco.domain.PresupuestoLinea;
import orco.domain.Prioridad;
import orco.domain.Proveedor;
import orco.domain.Remito;
import orco.domain.RemitoLinea;
import orco.domain.Responsable;
import orco.utils.ArchivoBean;
import orco.utils.ExtraJdbcConnection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;



@Controller
public class BasicController {

	/*
	 * Populates 
	 */
	
	 	@ModelAttribute("prioridads")
	    public final Collection<Prioridad> populatePrioridads() {
	        List<Prioridad> prioridades = new ArrayList<Prioridad>(Prioridad.findAllPrioridads());
	    	Collections.sort(prioridades);
	    	return prioridades;
	    }
	    
	    @ModelAttribute("clientes")
	    public final Collection<Cliente> populateClientes() {
	    	List<Cliente> lista = Cliente.findAllClientes();
	    	lista.add(0, new Cliente());
	    	return lista;
	    }
	    
	    @ModelAttribute("proveedors")
	    public final Collection<Proveedor> populateProveedors() {
	    	List<Proveedor> lista = Proveedor.findAllProveedors();
	    	lista.add(0, new Proveedor());
	    	return lista;
	    }
	    
	    @ModelAttribute("presupuestolineas")
	    public final Collection<PresupuestoLinea> populatePresupuestoLineas() {
	        return PresupuestoLinea.findAllPresupuestoLineas();
	    }
	    
	    @ModelAttribute("tiempos")
	    public final Collection<Tiempo> populateTiempos() {
	        return this.findAllTiempos();
	    }
	   
	    @ModelAttribute("responsables")
	    public final Collection<Responsable> populateResponsables() {
	        List<Responsable> lista = Responsable.findAllResponsables();
	        lista.add(0, new Responsable());
	    	return lista;
	    }
	    
	    @ModelAttribute("presupuestoes")
	    public final Collection<Presupuesto> populatePresupuestoes() {
	        List<Presupuesto> lista = Presupuesto.findAllPresupuestoes();
	        lista.add(0, new Presupuesto());
	    	return lista;
	    }
	    
	    @ModelAttribute("ordentrabajoes")
	    public final Collection<OrdenTrabajo> populateOrdenTrabajoes() {
	        List<OrdenTrabajo> ordenes = OrdenTrabajo.findAllOrdenTrabajoes();
	        ordenes.add(0, new OrdenTrabajo());
			return ordenes;
	    }
	    
	    @ModelAttribute("ordentrabajolineas")
	    public final Collection<OrdenTrabajoLinea> populateOrdenTrabajoLineas() {
	        return OrdenTrabajoLinea.findAllOrdenTrabajoLineas();
	    }
	    
	    @ModelAttribute("remitolineas")
	    public final Collection<RemitoLinea> populateRemitoLineas() {
	        return RemitoLinea.findAllRemitoLineas();
	    }
	    
	    @ModelAttribute("facturalineas")
	    public final Collection<FacturaLinea> populateFacturaLineas() {
	        return FacturaLinea.findAllFacturaLineas();
	    }

    /*
     * Converters
     */
	    
	    final Converter<Cliente, String> getClienteConverter() {
	        return new Converter<Cliente, String>() {
	            public String convert(Cliente cliente) {
	            	if (cliente.getNombre() != null){
	            		return cliente.getNombre();
	            	} else {
	            		return "Sin Cliente";
	            	}
	            }
	        };
	    }
	    
	    final Converter<Proveedor, String> getProveedorConverter() {
	        return new Converter<Proveedor, String>() {
	            public String convert(Proveedor proveedor) {
	            	if (proveedor.getNombre() != null){
	            		return proveedor.getNombre();
	            	} else {
	            		return "Sin Proveedor.";
	            	}
	            }
	        };
	    }
	    
	    final Converter<Presupuesto, String> getPresupuestoConverter() {
	        return new Converter<Presupuesto, String>() {
	            public String convert(Presupuesto presupuesto) {
	                return presupuesto.toString();
	            }
	        };
	    }
	    
	    final Converter<PresupuestoLinea, String> getPresupuestoLineaConverter() {
	        return new Converter<PresupuestoLinea, String>() {
	            public String convert(PresupuestoLinea presupuestoLinea) {
	                return new StringBuilder().append(presupuestoLinea.getCantidad()).append(" ").append(presupuestoLinea.getDescripcionTrabajo()).append(" ").append(presupuestoLinea.getPrecioUnitario()).toString();
	            }
	        };
	    }
	    
	    final Converter<Prioridad, String> getPrioridadConverter() {
	        return new Converter<Prioridad, String>() {
	            public String convert(Prioridad prioridad) {
	                return prioridad.getTexto();
	            }
	        };
	    }
	    
	    final Converter<Responsable, String> getResponsableConverter() {
	        return new Converter<Responsable, String>() {
	            public String convert(Responsable responsable) {
	            	if (responsable.getApellido() != null && responsable.getNombres() != null){
	            		return new StringBuilder()
	            						.append(responsable.getApellido())
	            						.append(", ")
	            						.append(responsable.getNombres())
	            						.toString();
	            	} else {
	            		return "Sin Responsable";
	            	}
	            }
	        };
	    }
	    
	    final Converter<OrdenTrabajo, String> getOrdenTrabajoConverter() {
	        return new Converter<OrdenTrabajo, String>() {
	            public String convert(OrdenTrabajo ordenTrabajo) {
	            	if (ordenTrabajo.getId() != null){
		                return ordenTrabajo.toString();	
	            	} else {
	            		return "Sin Orden.";
	            	}
	            }
	        };
	    }
	    
	    final Converter<OrdenTrabajoLinea, String> getOrdenTrabajoLineaConverter() {
	        return new Converter<OrdenTrabajoLinea, String>() {
	            public String convert(OrdenTrabajoLinea ordenTrabajoLinea) {
	                return new StringBuilder().append(ordenTrabajoLinea.getCantidad()).append(" ").append(ordenTrabajoLinea.getDescripcion()).toString();
	            }
	        };
	    }
	    
	    final Converter<OrdenCompraLinea, String> getOrdenCompraLineaConverter() {
	        return new Converter<OrdenCompraLinea, String>() {
	            public String convert(OrdenCompraLinea ordenCompraLinea) {
	                return new StringBuilder().append(ordenCompraLinea.getCantidad()).append(" ").append(ordenCompraLinea.getDescripcion()).append(" ").append(ordenCompraLinea.getPrecioUnitario()).toString();
	            }
	        };
	    }
	    
	    final Converter<OrdenCompra, String> getOrdenCompraConverter() {
	        return new Converter<OrdenCompra, String>() {
	            public String convert(OrdenCompra ordenCompra) {
	                return ordenCompra.toString();
	            }
	        };
	    }
	    
	    final Converter<Tiempo, String> getTiempoConverter(){
	    	return new Converter<Tiempo, String>() {
	            public String convert(Tiempo tiempo) {
	                return tiempo.getTexto();
	            }
	        };
	    }
	    
	    final Converter<Remito, String> getRemitoConverter() {
	        return new Converter<Remito, String>() {
	            public String convert(Remito remito) {
	                return remito.getRemitoFormulario();
	            }
	        };
	    }
	    
	    final Converter<RemitoLinea, String> getRemitoLineaConverter() {
	        return new Converter<RemitoLinea, String>() {
	            public String convert(RemitoLinea remitoLinea) {
	                return new StringBuilder().append(remitoLinea.getCantidad()).append(" ").append(remitoLinea.getDescripcion()).toString();
	            }
	        };
	    }
	  
	    
	    public final String encodeUrlPathSeg(String pathSegment, HttpServletRequest request) {
	        String enc = request.getCharacterEncoding();
	        if (enc == null) {
	            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
	        }
	        try {
	            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
	        }
	        catch (UnsupportedEncodingException uee) {}
	        return pathSegment;
	    }
	
	/*
	 * Tiempos
	 */
	    
	   protected List<Tiempo> findAllTiempos(){
		   List<Tiempo> lista = new ArrayList<Tiempo>();
			   
		   for (int i = 0; i < 24; i++){
			   lista.add(new Tiempo(i));
		   }		
		   
		   return lista;
	   }
	    
	   /**
	    * Parte de manejo del componente de tiempo, dado que por ahora no se bien como manejarlo  
	   */
	   public class Tiempo{
		   private  int valor;
		   private  String texto;
		   
		   public Tiempo(){}
		   
		   public Tiempo(int valor){
			   this.valor = valor * 100;
			   this.texto = ((valor >= 10)? "" : "0") + String.valueOf(valor) + ":00";
		   }
		   
		   @Override
			public String toString() {
				return this.texto;
			}
		   
		   public int getValor() {return valor;}
		   public void setValor(int valor) {this.valor = valor;}
		   public String getTexto() {return texto;}
		   public void setTexto(String texto) {this.texto = texto;}
	   }
	
	 /*
	  * Prints
	  */
	   
	    /**
	     * 
	     * @param model Modelo de la llamada http
	     * @param request request de la llamada http
	     * @param params Mapeo con los parametros del reporte
	     * @param archivosJasper nombre del archivo del reporte (sin path)
	     * @param nombreArchivoPDF nombre del archivo generado
	     */
		protected void printGenerico(Model model, HttpServletRequest request, Map<String, Object> params, String archivosJasper, String nombreArchivoPDF) 
		{
			try
	        {
				//Agrego la imagen de Logo Reducido por si el reporte lo necesita
				try {
					params.put("logoreducido_img", new FileInputStream(request.getSession().getServletContext().getRealPath("/images/" + "logoReducido.jpg")));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				//Agrego la imagen de Membrete por si el reporte lo necesita
				try {
					params.put("membrete_img", new FileInputStream(request.getSession().getServletContext().getRealPath("/images/" + "membrete.jpg")));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				//Agrego la imagen del fondo del presupuesto
				try {
					params.put("presupuesto_back", new FileInputStream(request.getSession().getServletContext().getRealPath("/images/" + "presupuestoEnBlanco.jpg")));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
				
				String pathTemplate = request.getSession().getServletContext().getRealPath("/reports/" + archivosJasper);
	            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(pathTemplate); 
	            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, 
	          		  									params,
	          		  									ExtraJdbcConnection.getConnection(request));
	            
	            String pathPdfGenerado = request.getSession().getServletContext().getRealPath("/reports/" + nombreArchivoPDF);
	            String urlPDFGenerado = "/CyC/resources/reports/" + nombreArchivoPDF;
				//Genero el pdf para bajar
	            JasperExportManager.exportReportToPdfFile(jasperPrint, pathPdfGenerado);
	            
				//Preparo el codigo para la pagina de impresion
				ArchivoBean arch = new ArchivoBean();
				arch.setNombre(nombreArchivoPDF);
				arch.setUrl(urlPDFGenerado);
				
				model.addAttribute(ArchivoBean.PARAMETRO_ARCHIVO, arch);
	          }
	          catch (JRException e)
	          {
	            e.printStackTrace();
	          }
		}
		
		 /**
	     * 
	     * @param model Modelo de la llamada http
	     * @param request request de la llamada http
	     * @param beans Mapeo con los parametros del reporte
	     * @param archivosXLS nombre del archivo del reporte (sin path)
	     * @param nombreArchivoXLS nombre del archivo generado
	     */
		protected void genXls(Model model, HttpServletRequest request, Map<String, Object> beans, String archivoXLS, String nombreArchivoXLS) 
		{
			try
	        {
				//
				String pathTemplate = request.getSession().getServletContext().getRealPath("/xlss/" + archivoXLS);
	            String pathXLSGenerado = request.getSession().getServletContext().getRealPath("/xlss/" + nombreArchivoXLS);
	            String urlXLSGenerado = "/CyC/resources/xlss/" + nombreArchivoXLS;
				
	            //Genero el archivo de excel
	            XLSTransformer transformer = new XLSTransformer();
                transformer.transformXLS(pathTemplate, beans, pathXLSGenerado);
	            
	            //Preparo el codigo para la pagina de impresion
				ArchivoBean arch = new ArchivoBean();
				arch.setNombre(nombreArchivoXLS);
				arch.setUrl(urlXLSGenerado);
				
				model.addAttribute(ArchivoBean.PARAMETRO_ARCHIVO, arch);
	          }
	          catch (Exception e)
	          {
	            e.printStackTrace();
	          }
		}
		
		
		
		
		
		
}
