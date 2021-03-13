package com.example.coronavirus.controllers;

import com.example.coronavirus.services.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    CoronaVirusDataService coronaVirusDataService;

    @GetMapping("/")
    public String home(Model model){
        int totalReport=coronaVirusDataService.getAllStats().stream().mapToInt(stat -> stat.getLastedTotalCases()).sum();
        model.addAttribute("locationStats",coronaVirusDataService.getAllStats());
        model.addAttribute("totalReportedCases",totalReport);
        return "index";
    }
}
