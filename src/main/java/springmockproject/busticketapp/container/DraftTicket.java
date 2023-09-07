package springmockproject.busticketapp.container;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DraftTicket {

    private String bookerId;
    private String seatId;
    private int seatNo;
    private String passengerName;
    private String passengerPID;
    private boolean isChild;
    private String busId;


}
