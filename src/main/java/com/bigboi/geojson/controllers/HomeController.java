package com.bigboi.geojson.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for handling the landing page of the application.
 */
@Controller
public class HomeController {

    /**
     * Handles requests to the root URL and returns the landing page.
     *
     * @return ModelAndView for the landing page
     */
    @GetMapping("/")
    public ModelAndView home() {
        return new ModelAndView("index");
    }
}