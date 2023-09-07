package springmockproject.busticketapp.search;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Data
@NoArgsConstructor
public class BusSearchInfo {

    private String fromId;
    private String toId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date = new Date();

}
