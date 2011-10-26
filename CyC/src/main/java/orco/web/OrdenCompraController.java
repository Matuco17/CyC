package orco.web;

import java.math.BigDecimal;
import java.util.ArrayList;
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

import orco.domain.OrdenCompra;
import orco.domain.OrdenCompraLinea;
import orco.filter.OrdenCompraFilter;
import orco.utils.Constantes;
import orco.utils.Corrector;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "ordencompras", formBackingObject = OrdenCompra.class)
@RequestMapping("/ordencompras")
@Controller
public class OrdenCompraController extends BasicController {
	
	@Autowired
    private GenericConversionService conversionService2;
  	
	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid OrdenCompra ordenCompra, BindingResult result, Model model, HttpServletRequest request) {
    	completarOrdenCompra(ordenCompra, request);
    
    	corregirOrdenCompra(ordenCompra);
    	
    	if (result.hasErrors()) {
            model.addAttribute("ordencompra", ordenCompra);
            addDateTimeFormatPatterns(model);
            return "ordencompras/create";
        } else {      
        	ordenCompra.persist();
	        return "redirect:/ordencompras/" + super.encodeUrlPathSeg(ordenCompra.getId().toString(), request);
 	    }
    }

    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model model) {
    	OrdenCompra oc = OrdenCompra.crear();
    	
    	model.addAttribute("ordencompra", oc);
        addDateTimeFormatPatterns(model);
        return "ordencompras/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model model) {
        addDateTimeFormatPatterns(model);
        model.addAttribute("ordencompra", OrdenCompra.findOrdenCompra(id));
        model.addAttribute("itemId", id);
        return "ordencompras/show";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String list(OrdenCompraFilter ordenCompraFilter, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,@RequestParam(value = "prevfilter", required = false) Boolean prevfilter, Model model, HttpServletRequest request) {
    	Corrector.corregirElemento(ordenCompraFilter);
    	
    	//Si encuentro un filtro anterior, entonces propongo este en vez del anterior
    	if ((prevfilter != null && prevfilter.booleanValue())
    		&&
    		request.getSession().getAttribute(Constantes.ULTIMO_ORDENCOMPRA_FILTER) != null
    		){
    		ordenCompraFilter = (OrdenCompraFilter) request.getSession().getAttribute(Constantes.ULTIMO_ORDENCOMPRA_FILTER);
    	} else {
    		request.getSession().setAttribute(Constantes.ULTIMO_ORDENCOMPRA_FILTER, ordenCompraFilter);
    	}
    	
    	int sizeNo = (size == null) ? 10 : size.intValue();
    	List<OrdenCompra> ordenCompraList = new ArrayList<OrdenCompra>();
        if (page != null || size != null) {
        	ordenCompraList =  OrdenCompra.findOrdenCompra(ordenCompraFilter, page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo);
        } else {
        	ordenCompraList = OrdenCompra.findOrdenCompra(ordenCompraFilter, 0 , 10);
        }
        float nrOfPages = (float) OrdenCompra.countFindOrdenCompra(ordenCompraFilter) / sizeNo;
        model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        model.addAttribute("ordencompras", ordenCompraList);
        model.addAttribute("ordenCompraFilter", ordenCompraFilter);
        addDateTimeFormatPatterns(model);
        return "ordencompras/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid OrdenCompra ordenCompra, BindingResult result, Model model, HttpServletRequest request) {
    	
    	completarOrdenCompra(ordenCompra, request);        
        
    	corregirOrdenCompra(ordenCompra);
    	
        if (result.hasErrors()) {
             model.addAttribute("ordencompra", ordenCompra);
             addDateTimeFormatPatterns(model);
             return "ordenCompras/update";
        } else { //SI no ocurre error, entonces lo grabo
            ordenCompra.merge();
            return "redirect:/ordencompras/" + super.encodeUrlPathSeg(ordenCompra.getId().toString(), request);
        }
         
       
     }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model model) {
    	OrdenCompra oc = OrdenCompra.findOrdenCompra(id);
    	
    	model.addAttribute("ordencompra", oc);
        
        addDateTimeFormatPatterns(model);
        return "ordencompras/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
    	OrdenCompra.findOrdenCompra(id).remove();
        model.addAttribute("page", (page == null) ? "1" : page.toString());
        model.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/ordencompras?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    }
    
    @RequestMapping(value = "/print/{id}/{type}", method = RequestMethod.GET)
    public String print(@PathVariable("id") Long id, @PathVariable("type") String type, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model, HttpServletRequest request) {
    	String jasperFile = null;
    	String pdfFile = null;
    	if (type.equalsIgnoreCase(Constantes.PRINT_TYPE_COMMON)){
    		jasperFile = "OrdenCompra.jasper";
    		pdfFile = "ordenCompra_" + id.toString() + ".pdf";
    	} 
    	
    	if (jasperFile != null){
    	
	    	//Creo los parametros a pasar
			Map<String, Object> params = new HashMap<String, Object>();
	        params.put("OrdenCompraId", id);
	        
	        printGenerico(model, request, params, jasperFile, pdfFile);
	       
	        return Constantes.VIEW_IMPRIMIR;
    	}
    	return null;
    }


    
    
    
    /**
	 * Completa el contenido de la OrdenCompra de lineas y otros datos no mapeados
	 * @param ordenCompra objeto de Tipo OrdenCompra al que se le van a completar datos
	 * @param request HTTPRequest obtenido desde la llamada al controlador
	 */
	public static void completarOrdenCompra(OrdenCompra ordenCompra, HttpServletRequest request) {
		//Completo algun que otro campo que no figure correctamente
		String porcImpuesto = request.getParameter("porcImpuesto");
		try {
			ordenCompra.setImpuesto(new BigDecimal(porcImpuesto));
	    } catch (Exception e) {
			ordenCompra.setImpuesto(new BigDecimal(0));
		}	
	   		
		//Completo la parte de lineas para que las inserte ya que no lo esta agregando
    	String[] ids = request.getParameterValues("linea_id");
    	String[] cantidades = request.getParameterValues("cantidad");
    	String[] descripciones = request.getParameterValues("descripcion");
    	String[] preciosUnitarios = request.getParameterValues("precioUnitario");
    	
    	
    	for (int i = 0; i < cantidades.length; i++){
    		OrdenCompraLinea fl = new OrdenCompraLinea();
    		if (cantidades[i] != null && cantidades[i].trim().length() > 0){
    			long cantidad = 0;
    			try {
					cantidad = Long.parseLong(cantidades[i]);
				} catch (NumberFormatException e) {
					cantidad = 0;
				}
    			if (cantidad > 0){
    				fl.setCantidad(new Long(cantidad));
	        		
    				fl.setDescripcion(descripciones[i]);
	        		
	        		if (preciosUnitarios[i] != null){
	        			try {
	        				fl.setPrecioUnitario(new BigDecimal(preciosUnitarios[i]));
	    	        	} catch (Exception e) {
	    	        		fl.setPrecioUnitario(new BigDecimal(0));
		    	        }
	        		}
	        		
	        			
	        		//Agrego los ids siempre que existan (ya que en el formulario de Crear no existe
	        		if (ids != null && ids.length > 0 && ids[i] != null){
	        			try {
							fl.setId(Long.valueOf(ids[i]));
						} catch (NumberFormatException e) {
							fl.setId(null);
						}     				        		
	        		}
	        		
	        		
	        		
	        		fl.setOrdenCompra(ordenCompra);
	        		
	        		ordenCompra.getLineas().add(fl);
    			}	        			
    		}
    	}
	}
 
	 /**
	 * Corrige todos los campos de la OrdenCompra que puedan ser incorrectos
	 * @param ot
	 */
    public static void corregirOrdenCompra(OrdenCompra oc){
    	Corrector.corregirElemento(oc);
		
    	//Ahora tambien corrijo los detalles
    	for (OrdenCompraLinea ocl : oc.getLineas()) {
			Corrector.corregirElemento(ocl);
		}
    }
    
    @PostConstruct
    void registerConverters() {
    	conversionService2.addConverter(super.getOrdenCompraConverter());
        conversionService2.addConverter(super.getOrdenCompraLineaConverter());
        conversionService2.addConverter(super.getOrdenTrabajoConverter());
        conversionService2.addConverter(super.getProveedorConverter());
        conversionService2.addConverter(super.getResponsableConverter());
    }
    
    void addDateTimeFormatPatterns(Model model) {
        model.addAttribute("ordenCompra_fecha_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        model.addAttribute("ordenCompra_fechaentrega_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
    }
	
}
