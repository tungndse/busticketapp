package springmockproject.busticketapp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import springmockproject.busticketapp.entity.User;
import springmockproject.busticketapp.error.UserError;
import springmockproject.busticketapp.login.Login;
import springmockproject.busticketapp.repository.TicketRepository;
import springmockproject.busticketapp.repository.UserRepository;
import springmockproject.busticketapp.search.UserSearchInfo;
import springmockproject.busticketapp.validator.RegistrationValidator;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private static final String DEFAULT_PASSWORD = "Default123";
    private final UserRepository userRepo;
    private final TicketRepository ticketRepo;
    private final RegistrationValidator validator;

    @Autowired
    public UserService(UserRepository userRepo, TicketRepository ticketRepo, RegistrationValidator validator) {
        this.userRepo = userRepo;
        this.ticketRepo = ticketRepo;
        this.validator = validator;
    }


    public User getUserByLogin(Login login) {
        return userRepo.getUserByIdAndPassword(login.getId(), login.getPassword());
    }

    public UserError validateUpdatedUser(User user, boolean byAdmin) {

        String oldPasswordError = null;

        UserError errors = validator.validateUpdate(user, byAdmin);

        if (!byAdmin) {

            if (!validator.isBlank(user.getOldPassword())
                    || !validator.isBlank(user.getPassword())
                    || !validator.isBlank(user.getConfirm())) {
                String realOldPwd = userRepo.getUserById(user.getId()).getPassword();
                if (!user.getOldPassword().equals(realOldPwd)) {

                    oldPasswordError = "Wrong old password";

                    errors.setOldPasswordError(oldPasswordError);

                    if (errors.isValid()) {
                        errors.setValid(false);
                    }
                }
            }
        }

        return errors;
    }

    public List<User> searchAll() {
        List<User> allUsers = new ArrayList<>();
        userRepo.findAll().forEach(allUsers::add);
        return allUsers;
    }

    public List<User> searchByInfo(UserSearchInfo searchInfo) {
        return userRepo.findUsers
                (searchInfo.getFirstName(), searchInfo.getLastName(), searchInfo.getRole());
    }

    public User searchById(String userId) {
        return userRepo.getUserById(userId);
    }

    public boolean checkIfUserHasActiveTickets(User updated) {
        return !ticketRepo.findByUserIdAndStatus(updated.getId(), 1).isEmpty();
    }

    public void updateUserRole(int newRole, User updated) {
        userRepo.updateRole(newRole, updated.getId());
    }

    public UserError validateRegistration(User user) {
        return validator.validateRegistration(user);
    }

    public void createUser(User user) {
        userRepo.save(user);
    }

    public void updateUser(User user) {
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            String oldPwd = userRepo.getUserById(user.getId()).getPassword();
            user.setPassword(oldPwd);
        }

        userRepo.save(user);
    }

    public void resetUserPassword(User updated) {

        updated = userRepo.getUserById(updated.getId());

        updated.setPassword(DEFAULT_PASSWORD);

        userRepo.save(updated);
    }
}
