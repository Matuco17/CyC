package orco.web;


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

import orco.domain.Cliente;
import orco.domain.OrdenTrabajo;
import orco.domain.OrdenTrabajoLinea;
import orco.domain.Presupuesto;
import orco.domain.PresupuestoLinea;
import orco.domain.Prioridad;
import orco.filter.OrdenTrabajoFilter;
import orco.utils.Constantes;
import orco.utils.Corrector;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "ordentrabajoes", formBackingObject = OrdenTrabajo.class)
@RequestMapping("/ordentrabajoes")
@Controller
public class OrdenTrabajoController extends BasicController {
	
	@Autowired
    private GenericConversionService conversionService2;
     
    @RequestMapping(method = RequestMethod.POST)
    public String create(@Valid OrdenTrabajo ordenTrabajo, BindingResult result, Model model, HttpServletRequest request) {
    	
    	completarOrdenTrabajo(ordenTrabajo, request);
    	
    	corregirOrdenTrabajo(ordenTrabajo);
    	
    	if (result.hasErrors()) {
            model.addAttribute("ordenTrabajo", ordenTrabajo);
            addDateTimeFormatPatterns(model);
            return "ordentrabajoes/create";
        } else { 
	        ordenTrabajo.persist();
	        return "redirect:/ordentrabajoes/" + super.encodeUrlPathSeg(ordenTrabajo.getId().toString(), request);
        }
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model model) {
      	OrdenTrabajo ordenTrabajo = OrdenTrabajo.crear();
		model.addAttribute("ordenTrabajo", ordenTrabajo);
        addDateTimeFormatPatterns(model);
        return "ordentrabajoes/create";
    }
    
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model model) {
        showGenerico(id, model);
        return "ordentrabajoes/show";
    }

	private void showGenerico(Long id, Model model) {
		addDateTimeFormatPatterns(model);
        model.addAttribute("ordentrabajo", OrdenTrabajo.findOrdenTrabajo(id));
        model.addAttribute("itemId", id);
	}
        
      
    @RequestMapping(method = RequestMethod.GET)
    public String list(OrdenTrabajoFilter ordenTrabajoFilter, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "prevfilter", required = false) Boolean prevfilter, Model model, HttpServletRequest request) {
    	Corrector.corregirElemento(ordenTrabajoFilter);
		    	
    	int sizeNo = (size == null) ? 10 : size.intValue();
        
    	//Si encuentro un filtro anterior, entonces propongo este en vez del anterior
    	if ((prevfilter != null && prevfilter.booleanValue())
    		&&
    		request.getSession().getAttribute(Constantes.ULTIMO_ORDENTRABAJO_FILTER) != null
    		){
    		ordenTrabajoFilter = (OrdenTrabajoFilter) request.getSession().getAttribute(Constantes.ULTIMO_ORDENTRABAJO_FILTER);
    	} else {
    		request.getSession().setAttribute(Constantes.ULTIMO_ORDENTRABAJO_FILTER, ordenTrabajoFilter);
    	}
    	
        return searchGenerico(ordenTrabajoFilter, page, size, model, sizeNo);
    }
    
    /**
     * Metodo generico de busqueda y devolucion de navegacion para la pantalla principal de Orden de trabajo
     * @param ordenTrabajoFilter
     * @param page
     * @param size
     * @param model
     * @param sizeNo
     * @return
     */
	private String searchGenerico(OrdenTrabajoFilter ordenTrabajoFilter,
			Integer page, Integer size, Model model, int sizeNo) {
		float nrOfPages = (float) OrdenTrabajo.countFindOrdenTrabajo(ordenTrabajoFilter) / sizeNo;
        
        List<OrdenTrabajo> ordenTrabajoList = new ArrayList<OrdenTrabajo>();
		if (page != null || size != null) {
			ordenTrabajoList = OrdenTrabajo.findOrdenTrabajo(ordenTrabajoFilter, page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo);
        } else {
        	ordenTrabajoList =  OrdenTrabajo.findOrdenTrabajo(ordenTrabajoFilter, 0, 10);
        }
        model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        model.addAttribute("ordentrabajoes", ordenTrabajoList);
        model.addAttribute("ordenTrabajoFilter", ordenTrabajoFilter);
        addDateTimeFormatPatterns(model);
        return "ordentrabajoes/list";
	}
    
    @RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid OrdenTrabajo ordenTrabajo, BindingResult result, Model model, HttpServletRequest request) {
    	
    	
    	String urlSeguirEnUpdate = "ordentrabajoes/update";
        String urlUpdateCompletado = "redirect:/ordentrabajoes/";
		
    	return updateGenerico(ordenTrabajo, result, model, request,	urlSeguirEnUpdate, urlUpdateCompletado);
    }
    
    /**
     * Metodo generco para el update
     * @param urlSeguirEnUpdate url que sirve por si no valido o se encontro un error
     * @param urlUpdateCompletado url que sirve una vez que se actualizo correctamente y pasa a show
     * @return
     */
	private String updateGenerico(OrdenTrabajo ordenTrabajo, BindingResult result, Model model, HttpServletRequest request,	String urlSeguirEnUpdate, String urlUpdateCompletado) {
		completarOrdenTrabajo(ordenTrabajo, request);
    	
		corregirOrdenTrabajo(ordenTrabajo);
		
    	if (result.hasErrors()) {
            model.addAttribute("ordenTrabajo", ordenTrabajo);
            addDateTimeFormatPatterns(model);
        	return urlSeguirEnUpdate;
        } else {
            ordenTrabajo.merge();
        	return urlUpdateCompletado + super.encodeUrlPathSeg(ordenTrabajo.getId().toString(), request);
        }
	}
    
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model model) {
        updateFormGenerico(id, model);
        return "ordentrabajoes/update";
    }

	private void updateFormGenerico(Long id, Model model) {
		model.addAttribute("ordenTrabajo", OrdenTrabajo.findOrdenTrabajo(id));
        addDateTimeFormatPatterns(model);
	}
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
        OrdenTrabajo.findOrdenTrabajo(id).remove();
        model.addAttribute("page", (page == null) ? "1" : page.toString());
        model.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/ordentrabajoes?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    }
    
    @RequestMapping(value = "/frompresupuesto/{id}", method = RequestMethod.GET)
    public String createFromPresupuesto(@PathVariable("id") Long id, Model model) {
        addDateTimeFormatPatterns(model);
        Presupuesto presupuestoOrigen = Presupuesto.findPresupuesto(id);
        OrdenTrabajo ordenTrabajo = OrdenTrabajo.createFrom(presupuestoOrigen);
        model.addAttribute("ordenTrabajo", ordenTrabajo);
        return "ordentrabajoes/create";
    }
    
    
    @RequestMapping(value = "/print/{id}", method = RequestMethod.GET)
    public String print(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model, HttpServletRequest request) {
   
      //Creo los parametros
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("OrdenTrabajoId", id);
      
      printGenerico(model, request, params, 
			"OrdenTrabajo.jasper", 
			"ordentrabajo_" + id.toString() + ".pdf");
    
      return VIEW_IMPRIMIR;
    }
    
    @RequestMapping(value = "/xls/pendientes", method = RequestMethod.GET)
    public String xlsPendientes( @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model, HttpServletRequest request) {
   
      //Creo el filtro y realizo la busqueda
      OrdenTrabajoFilter otfilter = new OrdenTrabajoFilter();
      otfilter.setOrdenFinalizada(Boolean.FALSE);
      List<OrdenTrabajo> ordenes = OrdenTrabajo.findOrdenTrabajo(otfilter, 0, Integer.MAX_VALUE);
      
      //Cargo los beans del excel
      Map<String, Object> beans = new HashMap<String, Object>();
      beans.put("ordenes", ordenes);
      beans.put("titulo", "LISTADO DE ORDENES DE TRABAJO PENDIENTES");
      
      genXls(model, request, beans, 
    		  "ListaOrdenesTrabajoFechas.xls", 
    		  "OrdenesPendientes" + (new Double(Math.random() * Integer.MAX_VALUE)).intValue() + ".xls");
    
      return VIEW_IMPRIMIR;
    }
    
    @RequestMapping(value = "/xls/finalizadas", method = RequestMethod.GET)
    public String xlsFinalizadas( @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model, HttpServletRequest request) {
   
      //Creo el filtro y realizo la busqueda
      OrdenTrabajoFilter otfilter = new OrdenTrabajoFilter();
      otfilter.setOrdenFinalizada(Boolean.TRUE);
      List<OrdenTrabajo> ordenes = OrdenTrabajo.findOrdenTrabajo(otfilter, 0, Integer.MAX_VALUE);
      
      //Cargo los beans del excel
      Map<String, Object> beans = new HashMap<String, Object>();
      beans.put("ordenes", ordenes);
      beans.put("titulo", "LISTADO DE ORDENES DE TRABAJO FINALIZADAS");
      
      genXls(model, request, beans, 
    		  "ListaOrdenesTrabajoFechas.xls", 
    		  "OrdenesFinalizadas" + (new Double(Math.random() * Integer.MAX_VALUE)).intValue()  + ".xls");
    
      return VIEW_IMPRIMIR;
    }
    
    
    /**
     * Esta es la llamada que permite la creacion de Ordenes de trabajo desde un presupuesto
     * @return
     */
    @RequestMapping(value = "/tallerlista", method = RequestMethod.GET)
    public String listPantallaTaller(Model model){
    	model.addAttribute("ordentrabajoes", OrdenTrabajo.findOrdenTrabajoPantallaTaller());
    	return "ordentrabajoes/tallerlista";
    }
    
    /**
     * La misma llamada de show pero con la diferencia de que se utiliza para taller
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/taller/{id}", method = RequestMethod.GET)
    public String showPantallaTaller(@PathVariable("id") Long id, Model model) {
        showGenerico(id, model);
        return "ordentrabajoes/tallershow";
    }
    
    /**
     * La misma logica para actualizar una Orden de trabajo pero para el taller
     * @param ordenTrabajo
     * @param result
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "taller", method = RequestMethod.PUT)
    public String updatePantallaTaller(@Valid OrdenTrabajo ordenTrabajo, BindingResult result, Model model, HttpServletRequest request) {
    	Corrector.corregirElemento(ordenTrabajo);
		
    	String urlSeguirEnUpdate = "ordentrabajoes/tallerupdate";
        String urlUpdateCompletado = "redirect:/ordentrabajoes/taller/";
		
    	return updateGenerico(ordenTrabajo, result, model, request,	urlSeguirEnUpdate, urlUpdateCompletado);
    }
    
    /**
     * Update form similar al del ABM pero en el taller
     */
    @RequestMapping(value = "/taller/{id}", params = "form", method = RequestMethod.GET)
    public String updateFormPantallaTaller(@PathVariable("id") Long id, Model model) {
        updateFormGenerico(id, model);
        return "ordentrabajoes/tallerupdate";
    }

    @RequestMapping(params = { "find=ByCliente", "form" }, method = RequestMethod.GET)
    public String findOrdenTrabajoesByClienteForm(Model model) {
        model.addAttribute("clientes", Cliente.findAllClientes());
        return "ordentrabajoes/findOrdenTrabajoesByCliente";
    }
    
    @RequestMapping(params = "find=ByCliente", method = RequestMethod.GET)
    public String findOrdenTrabajoesByCliente(@RequestParam("cliente") Cliente cliente, Model model) {
        model.addAttribute("ordentrabajoes", OrdenTrabajo.findOrdenTrabajoesByCliente(cliente).getResultList());
        return "ordentrabajoes/list";
    }
    
    
    /**
     * Corrige todos los campos de la orden de trabajo que puedan ser incorrectos
     * @param ot
     */
    public static void corregirOrdenTrabajo(OrdenTrabajo ot){
    	Corrector.corregirElemento(ot);
		
    	//Ahora tambien corrijo los detalles
    	for (OrdenTrabajoLinea otl : ot.getLineas()) {
			Corrector.corregirElemento(otl);
		}
    }
    
 
    /**
     * Completa el contenido de la Orden de Trabajo (lineas y otros datos no mapeados)
     * @param ot Orden de trabajo a la que se le van a completar los datos
     * @param request HTTPRequest obtenido desde la llamada al controlador
     */
    public static void completarOrdenTrabajo(OrdenTrabajo ot, HttpServletRequest request) {
    	//Completo la parte de lineas para que las inserte ya que no lo esta agregando
    	String[] ids = request.getParameterValues("linea_id");
    	String[] cantidades = request.getParameterValues("cantidad");
    	String[] descripciones = request.getParameterValues("descripcionTrabajo");
    	String[] prioridades = request.getParameterValues("prioridad");
    	String[] finalizados = request.getParameterValues("finalizado");
    	String[] presupuestoLineasOrigen = request.getParameterValues("presupuestoLineaOrigen");
    	String[] items = request.getParameterValues("item");
    	
    	for (int i = 0; i < cantidades.length; i++){
    		OrdenTrabajoLinea otL = new OrdenTrabajoLinea();
    		if (cantidades[i] != null && cantidades[i].trim().length() > 0){
    			long cantidad = 0;
    			try {
					cantidad = Long.parseLong(cantidades[i]);
				} catch (NumberFormatException e) {
					cantidad = 0;
				}
    			if (cantidad > 0){
    				otL.setItem(Long.parseLong(items[i]));
    				
    				otL.setCantidad(new Long(cantidad));
	        		
    				otL.setDescripcion(descripciones[i]);
	        		
	        		if (prioridades[i] != null && prioridades[i].trim().length() > 0)
	        			otL.setPrioridad(Prioridad.findPrioridad(new Long(prioridades[i])));
	        		
	        		if (presupuestoLineasOrigen[i] != null && presupuestoLineasOrigen[i].trim().length() > 0){
	        			otL.setPresupuestoLineaOrigen(PresupuestoLinea.findPresupuestoLinea(new Long(presupuestoLineasOrigen[i])));
	        		}
	        		
	        		//Agrego los ids siempre que existan (ya que en el formulario de Crear no existe
	        		if (ids != null && ids.length > 0 && ids[i] != null){
	        			try {
	        				otL.setId(Long.valueOf(ids[i]));
						} catch (NumberFormatException e) {
							otL.setId(null);
						}     				        		
	        		}
	        		
	        		//Agrego los ganados siempre que existan
	        		otL.setFinalizado(false);
	        		if (finalizados != null && finalizados.length > 0 && Arrays.binarySearch(finalizados, String.valueOf(i)) >= 0){
	        			try {
	        				otL.setFinalizado(true);
						} catch (NumberFormatException e) {
							otL.setFinalizado(false);
						}     				        		
	        		}
	        		
	        		otL.setOrdenTrabajo(ot);
	        		
	        		ot.getLineas().add(otL);
    			}	        			
    		}
    	}
    	
    	
    }
    	
    
     
    @PostConstruct
    void registerConverters() {
        conversionService2.addConverter(super.getOrdenTrabajoConverter());
        conversionService2.addConverter(super.getOrdenTrabajoLineaConverter());
        conversionService2.addConverter(super.getPresupuestoConverter());
        conversionService2.addConverter(super.getResponsableConverter());
        conversionService2.addConverter(super.getPrioridadConverter());
        conversionService2.addConverter(super.getTiempoConverter());
        conversionService2.addConverter(super.getClienteConverter());
    }
    
    void addDateTimeFormatPatterns(Model model) {
        model.addAttribute("ordenTrabajo_fechafin_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        model.addAttribute("ordenTrabajo_fechainicio_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
    }
    
   
	    
	
}
