package orco.web;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import orco.domain.Proveedor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "proveedors", formBackingObject = Proveedor.class)
@RequestMapping("/proveedors")
@Controller
public class ProveedorController {
}
