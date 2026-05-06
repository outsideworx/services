package net.outsideworx.services.controllers;

import org.springframework.web.servlet.ModelAndView;

public interface ModelVisitor {
    ModelAndView getModel();
}