package orco.web;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import orco.domain.Cliente;
import orco.filter.ClienteFilter;
import orco.utils.Constantes;
import orco.utils.Corrector;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "clientes", formBackingObject = Cliente.class)
@RequestMapping("/clientes")
@Controller
public class ClienteController extends BasicController {
	
	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Cliente cliente, BindingResult result, Model model, HttpServletRequest request) {
		Corrector.corregirElemento(cliente);
		if (result.hasErrors()) {
            model.addAttribute("cliente", cliente);
            return "clientes/create";
        }
        cliente.persist();
        return "redirect:/clientes/" + super.encodeUrlPathSeg(cliente.getId().toString(), request);
    }
    
	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Cliente cliente, BindingResult result, Model model, HttpServletRequest request) {
        Corrector.corregirElemento(cliente);
		if (result.hasErrors()) {
            model.addAttribute("cliente", cliente);
            return "clientes/update";
        }
        cliente.merge();
        return "redirect:/clientes/" + super.encodeUrlPathSeg(cliente.getId().toString(), request);
    }
	
	
	
	@RequestMapping(method = RequestMethod.GET)
    public String list(ClienteFilter clienteFilter, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,@RequestParam(value = "prevfilter", required = false) Boolean prevfilter, Model model, HttpServletRequest request) {
    	Corrector.corregirElemento(clienteFilter);
    	
    	//Si encuentro un filtro anterior, entonces propongo este en vez del anterior
    	if ((prevfilter != null && prevfilter.booleanValue())
    		&&
    		request.getSession().getAttribute(Constantes.ULTIMO_CLIENTE_FILTER) != null
    		){
    		clienteFilter = (ClienteFilter) request.getSession().getAttribute(Constantes.ULTIMO_CLIENTE_FILTER);
    	} else {
    		request.getSession().setAttribute(Constantes.ULTIMO_CLIENTE_FILTER, clienteFilter);
    	}
    	
    	int sizeNo = (size == null) ? 10 : size.intValue();
    	List<Cliente> clienteList = new ArrayList<Cliente>();
        if (page != null || size != null) {
        	clienteList =  Cliente.findCliente(clienteFilter, page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo);
        } else {
        	clienteList = Cliente.findCliente(clienteFilter, 0 , 10);
        }
        float nrOfPages = (float) Cliente.countFindCliente(clienteFilter) / sizeNo;
        model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        model.addAttribute("clientes", clienteList);
        model.addAttribute("clienteFilter", clienteFilter);
        return "clientes/list";
    }
    
	
	/*
	 @RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            model.addAttribute("clientes", Cliente.findClienteEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Cliente.countClientes() / sizeNo;
            model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            model.addAttribute("clientes", Cliente.findAllClientes());
        }
        return "clientes/list";
    }
	  */  
	
	
}
