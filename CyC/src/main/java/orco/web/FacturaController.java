package orco.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import orco.domain.Factura;
import orco.domain.FacturaLinea;
import orco.domain.OrdenTrabajo;
import orco.domain.OrdenTrabajoLinea;
import orco.domain.Presupuesto;
import orco.domain.PresupuestoLinea;
import orco.filter.FacturaFilter;
import orco.utils.Constantes;
import orco.utils.Corrector;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "facturas", formBackingObject = Factura.class)
@RequestMapping("/facturas")
@Controller
public class FacturaController extends BasicController {
	
	@Autowired
    private GenericConversionService conversionService2;
    
	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Factura factura, BindingResult result, Model model, HttpServletRequest request) {
    	completarFactura(factura, request);
    
    	corregirFactura(factura);
    	
    	if (result.hasErrors()) {
            model.addAttribute("factura", factura);
            addDateTimeFormatPatterns(model);
            return "facturas/create";
        } else {      
        	factura.persist();
	        return "redirect:/facturas/" + super.encodeUrlPathSeg(factura.getId().toString(), request);
 	    }
    }

    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model model) {
    	Factura fact = Factura.crear();
    	
    	model.addAttribute("factura", fact);
        addDateTimeFormatPatterns(model);
        return "facturas/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model model) {
        addDateTimeFormatPatterns(model);
        model.addAttribute("factura", Factura.findFactura(id));
        model.addAttribute("itemId", id);
        return "facturas/show";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String list(FacturaFilter facturaFilter, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,@RequestParam(value = "prevfilter", required = false) Boolean prevfilter, Model model, HttpServletRequest request) {
    	Corrector.corregirElemento(facturaFilter);
    	
    	//Si encuentro un filtro anterior, entonces propongo este en vez del anterior
    	if ((prevfilter != null && prevfilter.booleanValue())
    		&&
    		request.getSession().getAttribute(Constantes.ULTIMO_FACTURA_FILTER) != null
    		){
    		facturaFilter = (FacturaFilter) request.getSession().getAttribute(Constantes.ULTIMO_FACTURA_FILTER);
    	} else {
    		request.getSession().setAttribute(Constantes.ULTIMO_FACTURA_FILTER, facturaFilter);
    	}
    	
    	int sizeNo = (size == null) ? 10 : size.intValue();
    	List<Factura> facturaList = new ArrayList<Factura>();
        if (page != null || size != null) {
        	facturaList =  Factura.findFactura(facturaFilter, page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo);
        } else {
        	facturaList = Factura.findFactura(facturaFilter, 0 , 10);
        }
        float nrOfPages = (float) Factura.countFindFactura(facturaFilter) / sizeNo;
        model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        model.addAttribute("facturas", facturaList);
        model.addAttribute("facturaFilter", facturaFilter);
        addDateTimeFormatPatterns(model);
        return "facturas/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Factura factura, BindingResult result, Model model, HttpServletRequest request) {
    	
    	completarFactura(factura, request);        
        
    	corregirFactura(factura);
    	
        if (result.hasErrors()) {
             model.addAttribute("factura", factura);
             addDateTimeFormatPatterns(model);
             return "facturas/update";
        } else { //SI no ocurre error, entonces lo grabo
            factura.merge();
            return "redirect:/facturas/" + super.encodeUrlPathSeg(factura.getId().toString(), request);
        }
         
       
     }
    
    
    @RequestMapping(value = "/frompresupuesto/{id}", method = RequestMethod.GET)
    public String createFromPresupuesto(@PathVariable("id") Long id, Model model) {
        addDateTimeFormatPatterns(model);
        Presupuesto presupuestoOrigen = Presupuesto.findPresupuesto(id);
        Factura factura = Factura.createFrom(presupuestoOrigen);
        model.addAttribute("factura", factura);
        return "facturas/create";
    }
    
    @RequestMapping(value = "/fromordentrabajo/{id}", method = RequestMethod.GET)
    public String createFromOrdenTrabajo(@PathVariable("id") Long id, Model model) {
        addDateTimeFormatPatterns(model);
        OrdenTrabajo ordenTrabajoOrigen = OrdenTrabajo.findOrdenTrabajo(id);
        Factura factura = Factura.createFrom(ordenTrabajoOrigen);
        model.addAttribute("factura", factura);
        return "facturas/create";
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model model) {
    	Factura fact = Factura.findFactura(id);
    	
    	model.addAttribute("factura", fact);
        
        addDateTimeFormatPatterns(model);
        return "facturas/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
    	Factura.findFactura(id).remove();
        model.addAttribute("page", (page == null) ? "1" : page.toString());
        model.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/facturas?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    }
    
    @RequestMapping(value = "/print/{id}/{type}", method = RequestMethod.GET)
    public String print(@PathVariable("id") Long id,@PathVariable("type") String type, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model, HttpServletRequest request) {
    	String jasperFile = null;
    	String pdfFile = null;
    	if (type.equalsIgnoreCase(Constantes.PRINT_TYPE_TEMPLATE)){
    		jasperFile = "FacturaTemplate.jasper";
    		pdfFile = "facturaT_" + id.toString() + ".pdf";
    	}	
    	
    	if (jasperFile != null){
	    	//Busco la factura
			Factura factAImprimir = Factura.findFactura(id);
	        
			//Creo los parametros a pasar
			Map<String, Object> params = new HashMap<String, Object>();
	        params.put("FacturaId", id);
	        params.put("remitoFormularioRelacionado", factAImprimir.getRemitoFormulario());
	        
	        //Imprimo el reporte
	        printGenerico(model, request, params, jasperFile, pdfFile);
	       
	        return Constantes.VIEW_IMPRIMIR;
    	}
    	return null;
    }


    
    
    
    /**
	 * Completa el contenido de la Factura de lineas y otros datos no mapeados
	 * @param factura objeto de Tipo Factura al que se le van a completar datos
	 * @param request HTTPRequest obtenido desde la llamada al controlador
	 */
	public static void completarFactura(Factura factura, HttpServletRequest request) {
		//Completo algun que otro campo que no figure correctamente
		String porcImpuesto = request.getParameter("porcImpuesto");
		try {
			factura.setImpuesto(new BigDecimal(porcImpuesto));
	    } catch (Exception e) {
			factura.setImpuesto(new BigDecimal(0));
		}	
	
		String porcBonificacion = request.getParameter("porcBonificacion");
		try {
			factura.setBonificacion(new BigDecimal(porcBonificacion));
		} catch (Exception e) {
			factura.setBonificacion(new BigDecimal(0));
		}
		
		//Completo la parte de lineas para que las inserte ya que no lo esta agregando
    	String[] ids = request.getParameterValues("linea_id");
    	String[] nrosLineas = request.getParameterValues("nro_linea");
    	String[] cantidades = request.getParameterValues("cantidad");
    	String[] descripciones = request.getParameterValues("descripcion");
    	String[] preciosUnitarios = request.getParameterValues("precioUnitario");
    	String[] presupuestoLineasOrigen = request.getParameterValues("presupuestoLineaOrigen");
    	String[] ordenTrabajoLineasOrigen = request.getParameterValues("ordenTrabajoLineaOrigen");
    	
    	
    	for (int i = 0; i < cantidades.length; i++){
    		FacturaLinea fl = new FacturaLinea();
    		if (descripciones[i] != null && descripciones[i].trim().length() > 0){
				fl.setNroLinea(new Integer(nrosLineas[i]));
				
				//Completo la cantidad
				try {
					fl.setCantidad(new Long(cantidades[i]));
				} catch (NumberFormatException e) {
					fl.setCantidad(null);
				}
    			
				fl.setDescripcion(descripciones[i]);
        		
        		if (preciosUnitarios[i] != null){
        			try {
        				fl.setPrecioUnitario(new BigDecimal(preciosUnitarios[i]));
    	        	} catch (Exception e) {
    	        		fl.setPrecioUnitario(null);
	    	        }
        		}
        		
        		if (presupuestoLineasOrigen[i] != null && presupuestoLineasOrigen[i].trim().length() > 0){
        			fl.setPresupuestoLineaOrigen(PresupuestoLinea.findPresupuestoLinea(new Long(presupuestoLineasOrigen[i])));
        		}
        		
        		if (ordenTrabajoLineasOrigen[i] != null && ordenTrabajoLineasOrigen[i].trim().length() > 0){
        			fl.setOrdenTrabajoLineaOrigen(OrdenTrabajoLinea.findOrdenTrabajoLinea(new Long(ordenTrabajoLineasOrigen[i])));
        		}
        			
        		//Agrego los ids siempre que existan (ya que en el formulario de Crear no existe
        		if (ids != null && ids.length > 0 && ids[i] != null){
        			try {
						fl.setId(Long.valueOf(ids[i]));
					} catch (NumberFormatException e) {
						fl.setId(null);
					}     				        		
        		}
        		
        		
        		
        		fl.setFactura(factura);
        		
        		factura.getLineas().add(fl);
			}	        			
    		
    	}
	}
 
	 /**
	 * Corrige todos los campos de la Factura que puedan ser incorrectos
	 * @param ot
	 */
    public static void corregirFactura(Factura f){
    	Corrector.corregirElemento(f);
		
    	//Ahora tambien corrijo los detalles
    	for (FacturaLinea fl : f.getLineas()) {
			Corrector.corregirElemento(fl);
		}
    }
    
    
    void addDateTimeFormatPatterns(Model model) {
        model.addAttribute("factura_fecha_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
    }
    
    @PostConstruct
    void registerConverters() {
        conversionService2.addConverter(super.getClienteConverter());
        conversionService2.addConverter(super.getOrdenTrabajoConverter());
        conversionService2.addConverter(super.getPresupuestoConverter());
    }
	
}
