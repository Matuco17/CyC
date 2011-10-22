package orco.web;

import java.util.ArrayList;
import java.util.List;

import orco.domain.Cliente;
import orco.domain.OrdenTrabajo;
import orco.filter.OrdenTrabajoFilter;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/portalclientes")
@Controller
public class PortalClientesController extends BasicController {

    @RequestMapping(value="ordenestrabajo",  method = RequestMethod.GET)
    public String ordenestrabajo(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
    	 int sizeNo = (size == null) || (size < 1) ? 10 : size.intValue();
         
    	//Busco las ordenes de trabajo del cliente
    	 try {
    		 //Primero busco al cliente 
    		 String userNameCliente = SecurityContextHolder.getContext().getAuthentication().getName();
    		 Cliente cliente = Cliente.findClientesByUserNamePortal(userNameCliente).getSingleResult();
    		 OrdenTrabajoFilter ordenTrabajoFilter = new OrdenTrabajoFilter();
        	 ordenTrabajoFilter.setCliente(cliente); 
        	 
        	 
        	 //Busco las ordenes de trabajo de acuerdo al filtro realizado
        	 List<OrdenTrabajo> ordenTrabajoList = new ArrayList<OrdenTrabajo>();
             float nrOfPages = (float) ordenTrabajoList.size() / sizeNo;
             
             if (page != null) {
     			ordenTrabajoList = ordenTrabajoList.subList(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo);
             } else {
             	ordenTrabajoList =  ordenTrabajoList.subList(0, 10);
             }
             model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
             model.addAttribute("ordentrabajoes", ordenTrabajoList);
             model.addAttribute("ordenTrabajoFilter", ordenTrabajoFilter);
             return "portalclientes/ordenestrabajo";
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
    }
	
	
}
