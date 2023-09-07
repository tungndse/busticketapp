package springmockproject.busticketapp.login;

import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Builder
public class SessionUser implements Serializable {

    private String id;
    private String firstName;
    private String lastName;
    private int role;

}
