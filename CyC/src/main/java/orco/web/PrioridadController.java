package orco.web;

import org.springframework.core.convert.converter.Converter;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import orco.domain.Prioridad;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "prioridads", formBackingObject = Prioridad.class)
@RequestMapping("/prioridads")
@Controller
public class PrioridadController {
	
	  Converter<Prioridad, String> getPrioridadConverter() {
	        return new Converter<Prioridad, String>() {
	            public String convert(Prioridad prioridad) {
	                return prioridad.getTexto();
	            }
	        };
	    }
	
}
