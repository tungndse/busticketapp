package springmockproject.busticketapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springmockproject.busticketapp.entity.Bus;
import springmockproject.busticketapp.entity.Location;
import springmockproject.busticketapp.repository.BusRepository;
import springmockproject.busticketapp.repository.LocationRepository;
import springmockproject.busticketapp.search.BusSearchInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class BusService {

    private final BusRepository busRepo;

    private final LocationRepository locationRepo;

   /* SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    String dateString = format.format( new Date()   );
    Date   date       = format.parse ( "2009-12-31" );*/

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat preFormat = new SimpleDateFormat("yyyy-MM-dd");


    @Autowired
    public BusService(BusRepository busRepo, LocationRepository locationRepo) {
        this.busRepo = busRepo;
        this.locationRepo = locationRepo;
    }

    public List<Bus> getBusListFromBusSearchInfoStrictly(BusSearchInfo searchInfo) throws ParseException {

        Location locFrom = locationRepo.getById(searchInfo.getFromId());
        Location locTo = locationRepo.getById(searchInfo.getToId());

        log.info("Date for search: " + searchInfo.getDate());

        if (searchInfo.getDate() != null) {

            Date date = searchInfo.getDate();
            String dateString = preFormat.format(date);

            log.info("dateString: " + dateString);

            String dateBeginString = dateString + " 00:00:00";
            String dateEndString = dateString + " 23:59:59";

            log.info("dateBeginString: " + dateBeginString);
            log.info("dateEndString: " + dateEndString);

            Date dateBegin = format.parse(dateBeginString);
            Date dateEnd = format.parse(dateEndString);

            log.info("dateBegin: " + dateBegin);
            log.info("dateEnd: " + dateEnd);

            return busRepo.findByFromLocAndToLocAndDateRunBetweenOrderByDateRun
                    (locFrom, locTo, dateBegin, dateEnd);

        }

        return null;

    }

    public List<Bus> getBusListFromBusSearchInfo(BusSearchInfo searchInfo) throws ParseException {

        Location locFrom = locationRepo.getById(searchInfo.getFromId());
        Location locTo = locationRepo.getById(searchInfo.getToId());

        if (searchInfo.getDate() != null) {

            Date date = searchInfo.getDate();
            String dateString = preFormat.format(date);

            String dateBeginString = dateString + " 00:00:00";
            String dateEndString = dateString + " 23:59:59";

            Date dateBegin = format.parse(dateBeginString);
            Date dateEnd = format.parse(dateEndString);

            return busRepo.findByFromLocAndToLocAndDateRunBetweenOrderByDateRun
                    (locFrom, locTo, dateBegin, dateEnd);
        } else {
            return busRepo.findByFromLocAndToLocOrderByDateRun(locFrom, locTo);
        }

    }

    public Bus getBusById(String busId) {
        return busRepo.getById(busId);
    }


}
