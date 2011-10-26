package orco.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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

import orco.domain.*;
import orco.filter.PresupuestoFilter;
import orco.utils.Constantes;
import orco.utils.Corrector;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "presupuestoes", formBackingObject = Presupuesto.class)
@RequestMapping("/presupuestoes")
@Controller
public class PresupuestoController extends BasicController {
	
	@Autowired
	private GenericConversionService conversionService2;//Tiene esta variable con el 2 xq sino no funcionaba lo de roo
	 
	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Presupuesto presupuesto, BindingResult result, Model model, HttpServletRequest request) {
    	completarPresupuesto(presupuesto, request);
    
    	corregirPresupuesto(presupuesto);
    	
    	if (result.hasErrors()) {
            model.addAttribute("presupuesto", presupuesto);
            addDateTimeFormatPatterns(model);
            return "presupuestoes/create";
        } else {      
	        presupuesto.persist();
	        return "redirect:/presupuestoes/" + super.encodeUrlPathSeg(presupuesto.getId().toString(), request);
 	    }
    }

    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model model) {
    	Presupuesto pres = Presupuesto.crear();
    	
    	model.addAttribute("presupuesto", pres);
        addDateTimeFormatPatterns(model);
        return "presupuestoes/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model model) {
        addDateTimeFormatPatterns(model);
        model.addAttribute("presupuesto", Presupuesto.findPresupuesto(id));
        model.addAttribute("itemId", id);
        return "presupuestoes/show";
    }
    /*
    @RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            model.addAttribute("presupuestoes", Presupuesto.findPresupuestoEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Presupuesto.countPresupuestoes() / sizeNo;
            model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            model.addAttribute("presupuestoes", Presupuesto.findAllPresupuestoes());
        }
        model.addAttribute("presupuestoFilter", new PresupuestoFilter());
        addDateTimeFormatPatterns(model);
        return "presupuestoes/list";
    }
    */
    
    @RequestMapping(method = RequestMethod.GET)
    public String list(PresupuestoFilter presupuestoFilter, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,@RequestParam(value = "prevfilter", required = false) Boolean prevfilter, Model model, HttpServletRequest request) {
    	Corrector.corregirElemento(presupuestoFilter);
    	
    	//Si encuentro un filtro anterior, entonces propongo este en vez del anterior
    	if ((prevfilter != null && prevfilter.booleanValue())
    		&&
    		request.getSession().getAttribute(Constantes.ULTIMO_PRESUPUESTO_FILTER) != null
    		){
    		presupuestoFilter = (PresupuestoFilter) request.getSession().getAttribute(Constantes.ULTIMO_PRESUPUESTO_FILTER);
    	} else {
    		request.getSession().setAttribute(Constantes.ULTIMO_PRESUPUESTO_FILTER, presupuestoFilter);
    	}
    	
    	
    	int sizeNo = size == null ? 10 : size.intValue();
    	List<Presupuesto> presupuestoList = new ArrayList<Presupuesto>();
    	if (page != null || size != null) {
        	presupuestoList =  Presupuesto.findPresupuesto(presupuestoFilter, page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo);
        } else {
        	presupuestoList = Presupuesto.findPresupuesto(presupuestoFilter, 0 , 10);
        }
        float nrOfPages = (float) Presupuesto.countFindPresupuesto(presupuestoFilter) / sizeNo;
        model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
      
        model.addAttribute("presupuestoes", presupuestoList);
        model.addAttribute("presupuestoFilter", presupuestoFilter);
        addDateTimeFormatPatterns(model);
        return "presupuestoes/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Presupuesto presupuesto, BindingResult result, Model model, HttpServletRequest request) {
    	
    	completarPresupuesto(presupuesto, request);        
        
    	corregirPresupuesto(presupuesto);
    	
        if (result.hasErrors()) {
             model.addAttribute("presupuesto", presupuesto);
             addDateTimeFormatPatterns(model);
             return "presupuestoes/update";
        } else { //SI no ocurre error, entonces lo grabo
            presupuesto.merge();
            return "redirect:/presupuestoes/" + super.encodeUrlPathSeg(presupuesto.getId().toString(), request);
        }
         
       
     }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model model) {
    	Presupuesto pres = Presupuesto.findPresupuesto(id);
    	
    	model.addAttribute("presupuesto", pres);
        
        addDateTimeFormatPatterns(model);
        return "presupuestoes/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
        Presupuesto.findPresupuesto(id).remove();
        model.addAttribute("page", (page == null) ? "1" : page.toString());
        model.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/presupuestoes?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    }
    
    @RequestMapping(value = "/print/{id}/{type}", method = RequestMethod.GET)
    public String print(@PathVariable("id") Long id, @PathVariable("type") String type, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model, HttpServletRequest request) {
    	String jasperFile = null;
    	String pdfFile = null;
    	if (type.equalsIgnoreCase(Constantes.PRINT_TYPE_COMMON)){
    		jasperFile = "Presupuesto.jasper";
    		pdfFile = "presupuesto_" + id.toString() + ".pdf";
    	} else if (type.equalsIgnoreCase(Constantes.PRINT_TYPE_TEMPLATE)){
     		jasperFile = "PresupuestoTemplate.jasper";
    		pdfFile = "presupuestoT_" + id.toString() + ".pdf";
    	} else if (type.equalsIgnoreCase(Constantes.PRINT_TYPE_SCAN)){
     		jasperFile = "PresupuestoScan.jasper";
    		pdfFile = "presupuestoS_" + id.toString() + ".pdf";
    	}
    	
    	if (jasperFile != null){
	    	//Creo los parametros a pasar
			Map<String, Object> params = new HashMap<String, Object>();
	        params.put("PresupuestoId", id);
	        
	        printGenerico(model, request, params, jasperFile, pdfFile);
	       
	        return Constantes.VIEW_IMPRIMIR;
    	}
    	return null;
    }
    
    /**
	 * Completa el contenido del presupuesto de (lineas y otros datos no mapeados)
	 * @param presupuesto objeto de Tipo Presupuesto al que se le van a completar datos
	 * @param request HTTPRequest obtenido desde la llamada al controlador
	 */
	public static void completarPresupuesto(Presupuesto presupuesto, HttpServletRequest request) {
		//Completo algun que otro campo que no figure correctamente
		String porcImpuesto = request.getParameter("porcImpuesto");
		try {
			presupuesto.setImpuesto(new BigDecimal(porcImpuesto));
	    } catch (Exception e) {
			presupuesto.setImpuesto(new BigDecimal(0));
		}	
	
		String porcBonificacion = request.getParameter("porcBonificacion");
		try {
			presupuesto.setBonificacion(new BigDecimal(porcBonificacion));
		} catch (Exception e) {
			presupuesto.setBonificacion(new BigDecimal(0));
		}
		
		//Completo la parte de lineas para que las inserte ya que no lo esta agregando
    	String[] ids = request.getParameterValues("linea_id");
    	String[] cantidades = request.getParameterValues("cantidad");
    	String[] descripciones = request.getParameterValues("descripcionTrabajo");
    	String[] prioridades = request.getParameterValues("prioridad");
    	String[] preciosUnitarios = request.getParameterValues("precioUnitario");
    	String[] ganados = request.getParameterValues("ganado");
    	
    	for (int i = 0; i < cantidades.length; i++){
    		PresupuestoLinea pl = new PresupuestoLinea();
    		if (cantidades[i] != null && cantidades[i].trim().length() > 0){
    			long cantidad = 0;
    			try {
					cantidad = Long.parseLong(cantidades[i]);
				} catch (NumberFormatException e) {
					cantidad = 0;
				}
    			if (cantidad > 0){
    				pl.setCantidad(new Long(cantidad));
	        		
    				pl.setDescripcionTrabajo(descripciones[i]);
	        		
	        		if (preciosUnitarios[i] != null){
	        			try {
	        				pl.setPrecioUnitario(new BigDecimal(preciosUnitarios[i]));
	    	        	} catch (Exception e) {
	    	        		pl.setPrecioUnitario(new BigDecimal(0));
		    	        }
	        		}
	        		
	        		if (prioridades[i] != null)
	        			pl.setPrioridad(Prioridad.findPrioridad(new Long(prioridades[i])));
	        		
	        		//Agrego los ids siempre que existan (ya que en el formulario de Crear no existe
	        		if (ids != null && ids.length > 0 && ids[i] != null){
	        			try {
							pl.setId(Long.valueOf(ids[i]));
						} catch (NumberFormatException e) {
							pl.setId(null);
						}     				        		
	        		}
	        		
	        		//Agrego los ganados siempre que existan
	        		pl.setGanado(false);
	        		if (ganados != null && ganados.length > 0 && Arrays.binarySearch(ganados, String.valueOf(i)) >= 0){
	        			try {
							pl.setGanado(true);
						} catch (NumberFormatException e) {
							pl.setGanado(false);
						}     				        		
	        		}
	        		
	        		pl.setPresupuesto(presupuesto);
	        		
	        		presupuesto.getLineas().add(pl);
    			}	        			
    		}
    	}
	}
 
	 /**
	 * Corrige todos los campos del Presupuesto que puedan ser incorrectos
	 * @param ot
	 */
    public static void corregirPresupuesto(Presupuesto p){
    	Corrector.corregirElemento(p);
		
    	//Ahora tambien corrijo los detalles
    	for (PresupuestoLinea pl : p.getLineas()) {
			Corrector.corregirElemento(pl);
		}
    }
	
	
    
    @PostConstruct
    void registerConverters() {
        conversionService2.addConverter(getClienteConverter());
        conversionService2.addConverter(getPresupuestoConverter());
        conversionService2.addConverter(getPresupuestoLineaConverter());
        conversionService2.addConverter(getPrioridadConverter());
        conversionService2.addConverter(getTiempoConverter());
    }
    
    
    void addDateTimeFormatPatterns(Model model) {
        model.addAttribute("presupuesto_fecha_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        model.addAttribute("presupuesto_fechafin_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        model.addAttribute("presupuesto_fechainicio_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
     }
    

    
}
