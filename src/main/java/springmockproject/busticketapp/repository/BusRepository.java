package springmockproject.busticketapp.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import springmockproject.busticketapp.entity.Bus;
import springmockproject.busticketapp.entity.Location;

import java.util.Date;
import java.util.List;

@Repository
public interface BusRepository extends CrudRepository<Bus, String> {

    List<Bus> findByFromLocAndToLocOrderByDateRun(Location fromLoc, Location toLoc);

    List<Bus> findByFromLocAndToLocAndDateRunBetweenOrderByDateRun(Location fromLoc, Location toLoc,
                                                                   Date dateRunBegin, Date dateRunEnd);
    Bus getById(String id);
}
