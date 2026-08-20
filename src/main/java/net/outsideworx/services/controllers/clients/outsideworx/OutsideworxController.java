package net.outsideworx.services.controllers.clients.outsideworx;

import net.outsideworx.services.controllers.ModelVisitor;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

@Controller
class OutsideworxController implements ModelVisitor {
    @Override
    public ModelAndView getModel() {
        return new ModelAndView("clients/outsideworx");
    }
}
