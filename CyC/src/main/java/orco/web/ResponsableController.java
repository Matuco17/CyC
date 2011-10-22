package orco.web;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import orco.domain.Responsable;
import orco.utils.Corrector;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "responsables", formBackingObject = Responsable.class)
@RequestMapping("/responsables")
@Controller
public class ResponsableController extends BasicController {
	
	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Responsable responsable, BindingResult result, Model model, HttpServletRequest request) {
        Corrector.corregirElemento(responsable);
		if (result.hasErrors()) {
            model.addAttribute("responsable", responsable);
            return "responsables/create";
        }
        responsable.persist();
        return "redirect:/responsables/" + encodeUrlPathSeg(responsable.getId().toString(), request);
    }
	
	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Responsable responsable, BindingResult result, Model model, HttpServletRequest request) {
		Corrector.corregirElemento(responsable);
		if (result.hasErrors()) {
            model.addAttribute("responsable", responsable);
            return "responsables/update";
        }
        responsable.merge();
        return "redirect:/responsables/" + encodeUrlPathSeg(responsable.getId().toString(), request);
    }
	 
	 
}
