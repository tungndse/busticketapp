package springmockproject.busticketapp.repository;

import org.springframework.data.repository.CrudRepository;
import springmockproject.busticketapp.entity.Notification;

public interface NotificationRepository extends CrudRepository<Notification, Long> {

}
