package springmockproject.busticketapp.search;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
public class UserSearchInfo {

    private String firstName;

    private String lastName;

    private int role;

}
