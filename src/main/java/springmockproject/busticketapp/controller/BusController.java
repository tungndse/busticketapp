package springmockproject.busticketapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import springmockproject.busticketapp.entity.Bus;
import springmockproject.busticketapp.entity.Location;
import springmockproject.busticketapp.search.BusSearchInfo;
import springmockproject.busticketapp.service.BusService;
import springmockproject.busticketapp.service.LocationService;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Slf4j
@Controller
public class BusController {


    private final BusService busService;
    private final LocationService locationService;

    @Autowired
    public BusController(BusService busService,
                         LocationService locationService) {
        this.busService = busService;
        this.locationService = locationService;

    }

    @ModelAttribute
    public void fillNecessities(Model model) {
        List<Location> listLoc = locationService.getAllLocations();
        model.addAttribute("listLoc", listLoc);
        model.addAttribute("today", new Date());
        model.addAttribute("searchInfo", new BusSearchInfo());
    }

    @GetMapping(value = "/customer/view_buses")
    public String viewAllBuses(Model model) throws ParseException {

        return "view-buses";
    }


    @PostMapping(value = "/customer/search_bus/commit")
    public String viewBuses(BusSearchInfo searchInfo, Model model) {

        List<Bus> list = null;
        try {
            list = busService.getBusListFromBusSearchInfoStrictly(searchInfo);
        } catch (ParseException e) {
            log.info(e.getMessage());
        }

        model.addAttribute("listBuses", list);

        return "bus-list";
    }


    @GetMapping(value = "/conductor/search_bus")
    public String prepareSearchSeats(Model model) {
        return "conductor-view-bus";
    }

    @PostMapping(value = "/conductor/search_bus/commit")
    public String viewBuses2(BusSearchInfo searchInfo, Model model) {
        List<Bus> list = null;
        try {
            list = busService.getBusListFromBusSearchInfo(searchInfo);
        } catch (ParseException e) {
            log.info("Problem parsing date search");
        }

        if (list != null && !list.isEmpty()) {
            model.addAttribute("listBuses", list);

        } else {
            model.addAttribute("foundNoResult", "Found no result");

        }

        return "conductor-view-bus";
    }


    @PostMapping(value = "/conductor/search_bus_for_tickets/commit")
    public String viewBuses3(BusSearchInfo searchInfo, String ticketStatus, Model model) {
        List<Bus> list = null;
        try {
            list = busService.getBusListFromBusSearchInfo(searchInfo);
        } catch (ParseException e) {
            log.info("Problem parsing date search");
        }
        model.addAttribute("ticketStatus", ticketStatus);

        if (list != null && !list.isEmpty()) {
            model.addAttribute("listBuses", list);
        } else {
            model.addAttribute("foundNoResult", "Found no result");

        }

        return "conductor-search-bus-for-tickets";
    }


}
