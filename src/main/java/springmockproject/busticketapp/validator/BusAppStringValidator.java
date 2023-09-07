package springmockproject.busticketapp.validator;

import org.springframework.stereotype.Component;

@Component
public class BusAppStringValidator {

    public boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

}
