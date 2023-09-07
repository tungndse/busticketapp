package springmockproject.busticketapp.error;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserError {

    private String idError;

    private String firstNameError;

    private String lastNameError;

    private String passwordError;

    private String confirmError;

    private String oldPasswordError;

    private boolean valid = true;

}
