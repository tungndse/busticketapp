package springmockproject.busticketapp.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import springmockproject.busticketapp.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, String> {


    User getUserById(String id);

    User getUserByIdAndPassword(String id, String password);

    List<User> getUserByFirstNameLikeOrLastNameLikeAndRoleEquals(String firstName, String lastName, int role);

    //@Query("SELECT p FROM Person p WHERE LOWER(p.lastName) = LOWER(:lastName)")
    //"SELECT u.username FROM User u WHERE u.username LIKE CONCAT('%',:username,'%')"
    @Query("SELECT u FROM User u WHERE (u.firstName LIKE CONCAT('%',:firstName,'%') " +
            "AND u.lastName LIKE CONCAT('%',:lastName,'%')) " +
            "AND u.role = :role")
    List<User> findUsers(@Param("firstName") String firstName, @Param("lastName") String lastName,
                           @Param("role") int role);


    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    void updateRole(int role, String userId);

}
