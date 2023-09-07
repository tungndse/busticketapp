package springmockproject.busticketapp.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import springmockproject.busticketapp.entity.Location;

@Repository
public interface LocationRepository extends CrudRepository<Location, String> {


    Location getById(String id);

}
