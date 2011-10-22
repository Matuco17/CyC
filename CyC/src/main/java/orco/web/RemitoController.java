package orco.web;

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

import orco.domain.OrdenTrabajo;
import orco.domain.OrdenTrabajoLinea;
import orco.domain.Presupuesto;
import orco.domain.PresupuestoLinea;
import orco.domain.Remito;
import orco.domain.RemitoLinea;
import orco.filter.RemitoFilter;
import orco.utils.Constantes;
import orco.utils.Corrector;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "remitoes", formBackingObject = Remito.class)
@RequestMapping("/remitoes")
@Controller
public class RemitoController extends BasicController {
	
	@Autowired
	private GenericConversionService conversionService2;
	 
	
    @RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Remito remito, BindingResult result, Model model, HttpServletRequest request) {
        completarRemito(remito, request);
        corregirRemito(remito);
    	
    	
    	if (result.hasErrors()) {
            model.addAttribute("remito", remito);
            addDateTimeFormatPatterns(model);
            return "remitoes/create";
        } else {
        	remito.persist();
            return "redirect:/remitoes/" + encodeUrlPathSeg(remito.getId().toString(), request);
        }
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model model) {
    	Remito remito = new Remito();
		
    	//Seteo por ahora un valor default para algunos campos
    	remito.setFecha(new Date());
    	
    	model.addAttribute("remito", remito);
        addDateTimeFormatPatterns(model);
        return "remitoes/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model model) {
        addDateTimeFormatPatterns(model);
        model.addAttribute("remito", Remito.findRemito(id));
        model.addAttribute("itemId", id);
        return "remitoes/show";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String list(RemitoFilter remitoFilter, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "prevfilter", required = false) Boolean prevfilter, Model model, HttpServletRequest request) {
        Corrector.corregirElemento(remitoFilter);
    	
        //Si encuentro un filtro anterior, entonces propongo este en vez del anterior
    	if ((prevfilter != null && prevfilter.booleanValue())
    		&&
    		request.getSession().getAttribute(Constantes.ULTIMO_REMITO_FILTER) != null
    		){
    		remitoFilter = (RemitoFilter) request.getSession().getAttribute(Constantes.ULTIMO_REMITO_FILTER);
    	} else {
    		request.getSession().setAttribute(Constantes.ULTIMO_REMITO_FILTER, remitoFilter);
    	}
        
        
    	int sizeNo = size == null ? 10 : size.intValue();
    	List<Remito> remitoList = new ArrayList<Remito>();
    	if (page != null || size != null) {
        	remitoList =  Remito.findRemito(remitoFilter, page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo);
        } else {
        	remitoList = Remito.findRemito(remitoFilter, 0 , 10);
        }
        float nrOfPages = (float) Remito.countFindRemito(remitoFilter) / sizeNo;
        model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        model.addAttribute("remitoes", remitoList);
        model.addAttribute("remitoFilter", remitoFilter);
        addDateTimeFormatPatterns(model);
        return "remitoes/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Remito remito, BindingResult result, Model model, HttpServletRequest request) {
        completarRemito(remito, request);
        
        corregirRemito(remito);
    	
    	if (result.hasErrors()) {
            model.addAttribute("remito", remito);
            addDateTimeFormatPatterns(model);
            return "remitoes/update";
        } else {  //SI no ocurre error, entonces lo grabo
	        remito.merge();
	        return "redirect:/remitoes/" + encodeUrlPathSeg(remito.getId().toString(), request);
        }
    }
    
    @RequestMapping(value = "/frompresupuesto/{id}", method = RequestMethod.GET)
    public String createFromPresupuesto(@PathVariable("id") Long id, Model model) {
        addDateTimeFormatPatterns(model);
        Presupuesto presupuestoOrigen = Presupuesto.findPresupuesto(id);
        Remito remito = Remito.createFrom(presupuestoOrigen);
        model.addAttribute("remito", remito);
        return "remitoes/create";
    }
    
    @RequestMapping(value = "/fromordentrabajo/{id}", method = RequestMethod.GET)
    public String createFromOrdenTrabajo(@PathVariable("id") Long id, Model model) {
        addDateTimeFormatPatterns(model);
        OrdenTrabajo ordenTrabajoOrigen = OrdenTrabajo.findOrdenTrabajo(id);
        Remito remito = Remito.createFrom(ordenTrabajoOrigen);
        model.addAttribute("remito", remito);
        return "remitoes/create";
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("remito", Remito.findRemito(id));
        addDateTimeFormatPatterns(model);
        return "remitoes/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
        Remito.findRemito(id).remove();
        model.addAttribute("page", (page == null) ? "1" : page.toString());
        model.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/remitoes?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    }
        
    @RequestMapping(value = "/printtemplate/{id}", method = RequestMethod.GET)
    public String printTemplate(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model, HttpServletRequest request) {
    	
    	//Creo el map con los parametros
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("RemitoId", id);
        
        printGenerico(model, request, params, 
				"RemitoTemplate.jasper", 
				"remitoT_" + id.toString() + ".pdf");
       
        return VIEW_IMPRIMIR;
    }
    
    /**
	 * Completa el contenido del Remito de lineas y otros datos no mapeados
	 * @param remito objeto de Tipo Remito al que se le van a completar datos
	 * @param request HTTPRequest obtenido desde la llamada al controlador
	 */
	public static void completarRemito(Remito remito, HttpServletRequest request) {
			
		//Completo la parte de lineas para que las inserte ya que no lo esta agregando
    	String[] ids = request.getParameterValues("linea_id");
    	String[] cantidades = request.getParameterValues("cantidad");
    	String[] descripciones = request.getParameterValues("descripcion");
    	String[] presupuestoLineasOrigen = request.getParameterValues("presupuestoLineaOrigen");
    	String[] ordenTrabajoLineasOrigen = request.getParameterValues("ordenTrabajoLineaOrigen");
    	
    	
    	for (int i = 0; i < cantidades.length; i++){
    		RemitoLinea rl = new RemitoLinea();
    		if (cantidades[i] != null && cantidades[i].trim().length() > 0){
    			long cantidad = 0;
    			try {
					cantidad = Long.parseLong(cantidades[i]);
				} catch (NumberFormatException e) {
					cantidad = 0;
				}
    			if (cantidad > 0){
    				rl.setCantidad(new Long(cantidad));
	        		
    				rl.setDescripcion(descripciones[i]);
	        		
	        		if (presupuestoLineasOrigen[i] != null && presupuestoLineasOrigen[i].trim().length() > 0){
	        			rl.setPresupuestoLineaOrigen(PresupuestoLinea.findPresupuestoLinea(new Long(presupuestoLineasOrigen[i])));
	        		}
	        		
	        		if (ordenTrabajoLineasOrigen[i] != null && ordenTrabajoLineasOrigen[i].trim().length() > 0){
	        			rl.setOrdenTrabajoLineaOrigen(OrdenTrabajoLinea.findOrdenTrabajoLinea(new Long(ordenTrabajoLineasOrigen[i])));
	        		}	        		
	        			
	        		//Agrego los ids siempre que existan (ya que en el formulario de Crear no existe
	        		if (ids != null && ids.length > 0 && ids[i] != null){
	        			try {
							rl.setId(Long.valueOf(ids[i]));
						} catch (NumberFormatException e) {
							rl.setId(null);
						}     				        		
	        		}	        		       		
	        		
	        		rl.setRemito(remito);
	        		
	        		remito.getLineas().add(rl);
    			}	        			
    		}
    	}
	}

    
	 /**
	 * Corrige todos los campos del remito que puedan ser incorrectos
	 * @param ot
	 */
    public static void corregirRemito(Remito r){
    	Corrector.corregirElemento(r);
		
    	//Ahora tambien corrijo los detalles
    	for (RemitoLinea rl : r.getLineas()) {
			Corrector.corregirElemento(rl);
		}
    }
    
    
    void addDateTimeFormatPatterns(Model model) {
        model.addAttribute("remito_fecha_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
    }
	
    @PostConstruct
    void registerConverters() {
        conversionService2.addConverter(super.getClienteConverter());
        conversionService2.addConverter(super.getOrdenTrabajoConverter());
        conversionService2.addConverter(super.getPresupuestoConverter());
        conversionService2.addConverter(super.getRemitoConverter());      
        conversionService2.addConverter(super.getRemitoLineaConverter());
    }
}
