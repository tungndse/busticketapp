package springmockproject.busticketapp.container;

import lombok.Data;

import java.util.List;

@Data
public class DraftTicketContainer {

    List<DraftTicket> adultDraftList;

    List<DraftTicket> childrenDraftList;

    public DraftTicketContainer(List<DraftTicket> adultDraftList, List<DraftTicket> childrenDraftList) {
        this.adultDraftList = adultDraftList;
        this.childrenDraftList = childrenDraftList;
    }
}