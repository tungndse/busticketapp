package springmockproject.busticketapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springmockproject.busticketapp.entity.Location;
import springmockproject.busticketapp.repository.LocationRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepo;


    @Autowired
    public LocationService(LocationRepository locationRepo) {
        this.locationRepo = locationRepo;
    }

    public List<Location> getAllLocations() {
        List<Location> listLoc = new ArrayList<>();
        locationRepo.findAll().forEach(listLoc::add);
        return listLoc;
    }


}
