package springmockproject.busticketapp.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import springmockproject.busticketapp.entity.User;
import springmockproject.busticketapp.error.UserError;
import springmockproject.busticketapp.repository.UserRepository;

@Component
public class RegistrationValidator {

    UserRepository userRepo;

    @Autowired
    public RegistrationValidator(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    public UserError validateRegistration(User user) {

        UserError error = new UserError();

        if (isBlank(user.getId())) {
            error.setIdError("Username must not be blank");
        }

        if (isBlank(user.getFirstName())) {
            error.setFirstNameError("First Name must not be blank");
        }

        if (isBlank(user.getLastName())) {
            error.setLastNameError("Last Name must not be blank");
        }

        if (isBlank(user.getPassword())) {
            error.setPasswordError("Password must not be blank");
        } else if (!user.getConfirm().equals(user.getPassword())) {
            error.setConfirmError("Confirm doesn't match");
        }

        if (!isBlank(error.getIdError()) ||
                !isBlank(error.getFirstNameError()) ||
                !isBlank(error.getLastNameError()) ||
                !isBlank(error.getPasswordError()) ||
                !isBlank(error.getConfirmError())) {
            error.setValid(false);
        }

        if (error.isValid()) {
            if (userRepo.findById(user.getId()).isPresent()) {
                error.setIdError("Username not available");
                error.setValid(false);
                return error;
            }


        }

        return error;

    }

    public UserError validateUpdate(User user, boolean byAdmin) {

        UserError error = new UserError();

        if (isBlank(user.getFirstName())) {
            error.setFirstNameError("First Name must not be blank");
        }

        if (isBlank(user.getLastName())) {
            error.setLastNameError("Last Name must not be blank");
        }

        /*if (isBlank(user.getPassword().trim())) {
            error.setPasswordError("Password must not be blank");
        } else if (!user.getConfirm().equals(user.getPassword())) {
            error.setConfirmError("Confirm doesn't match");
        }*/

        if (!byAdmin) {

            if (!isBlank(user.getPassword())) {
                if (!user.getConfirm().equals(user.getPassword())) {
                    error.setConfirmError("Confirm doesn't match");
                }
            }

        }

        if (!isBlank(error.getFirstNameError()) ||
                !isBlank(error.getLastNameError()) ||
                !isBlank(error.getPasswordError()) ||
                !isBlank(error.getConfirmError())) {
            error.setValid(false);
        }

        return error;

    }
}
