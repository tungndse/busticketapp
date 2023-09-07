package springmockproject.busticketapp.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.JDBCException;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import springmockproject.busticketapp.container.DraftTicket;
import springmockproject.busticketapp.container.DraftTicketContainer;
import springmockproject.busticketapp.entity.*;
import springmockproject.busticketapp.exception.BookingException;
import springmockproject.busticketapp.repository.*;
import springmockproject.busticketapp.validator.BusAppStringValidator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TicketService {

    UserRepository userRepo;

    TicketRepository ticketRepo;

    SeatRepository seatRepo;

    BusRepository busRepo;

    WalletRepository walletRepo;

    CheckoutRepository checkoutRepo;

    WalletService walletService;

    BusAppStringValidator stringValidator;

    @Autowired
    public TicketService(UserRepository userRepo, TicketRepository ticketRepo, SeatRepository seatRepo, BusRepository busRepo,
                         WalletRepository walletRepo,
                         CheckoutRepository checkoutRepo,
                         WalletService walletService, BusAppStringValidator stringValidator) {
        this.userRepo = userRepo;
        this.ticketRepo = ticketRepo;
        this.seatRepo = seatRepo;
        this.busRepo = busRepo;
        this.walletRepo = walletRepo;
        this.checkoutRepo = checkoutRepo;
        this.walletService = walletService;
        this.stringValidator = stringValidator;
    }


    public List<Ticket> getTicketListByUserAndStatus(User user, int status) {
        return ticketRepo.findByUserAndStatus(user, status);
    }

    public List<Ticket> getTicketListByUserAndStatus(User user, String status) {
        int statusInt = Integer.parseInt(status);
        return getTicketListByUserAndStatus(user, statusInt);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTicketByIdInStringType(String ticketIdStr) throws BookingException {
        long ticketId = Long.parseLong(ticketIdStr);

        Ticket ticket = ticketRepo.findById(ticketId);
        String seatId = ticket.getSeat().getId();

        Seat seat = seatRepo.getById(seatId);
        Bus bus = seat.getBus();


        int childrenAttachedCount = ticketRepo.countChildrenAttachedToSpecificPIDOnTheSameBusByTheSameBookerAndIsBooking
                (bus.getId(), ticket.getUser().getId(), ticket.getPassengerIdentity());
        if (!ticket.isPassengerIsChild() && childrenAttachedCount > 0) {
            throw new BookingException("CustomerException-This passenger still has attached children on the same bus");
        }

        long diffInMillies = Math.abs(bus.getDateRun().getTime() - new Date().getTime());
        long diffHour = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        log.info("DIFF HOUR : " + diffHour + " hours");
        if (diffHour >= 72) {
            log.info("Refund 100%");
        } else if (diffHour >= 48) {
            log.info("Refund 80%");
        } else if (diffHour >= 24) {
            log.info("Refund 50%");
        } else {
            log.info("NO REFUND");
        }

        // REFUNDING
        if (diffHour > 24) {
            Wallet wallet = walletRepo.getByUserId(ticket.getUser().getId());

            double ticketRefund = bus.getPrice();

            if (diffHour < 72 && diffHour >= 48) {
                ticketRefund *= 0.8;
            } else if (diffHour < 48) {
                ticketRefund *= 0.5;
            }

            walletService.addBalance(wallet, ticketRefund);
        }

        ticketRepo.setTicketStatus(0, ticketId);

        seatRepo.changeSeatStatus(false, seatId);
    }

    @Transactional(rollbackFor = BookingException.class)
    public void cancelTickets(String[] ticketIds) throws BookingException {

        for (String ticketId : ticketIds) {

            cancelTicketByIdInStringType(ticketId);

        }
    }

    public void setTicketDoneByIdStringType(String ticketIdStr) {
        long ticketId = Long.parseLong(ticketIdStr);
        ticketRepo.setTicketStatus(2, ticketId);
        Ticket ticket = ticketRepo.findById(ticketId);
        String seatId = ticket.getSeat().getId();
        seatRepo.changeSeatStatus(false, seatId);
    }

    public List<Ticket> getTicketListByStatusString(String statusStr) {
        int status = Integer.parseInt(statusStr);
        return ticketRepo.findByStatus(status);
    }

    public List<Ticket> getTicketListByBusIdAndStatus(String busId, int ticketStatusInt) {
        return ticketRepo.findByBusIdAndStatus(busId, ticketStatusInt);
    }

    public List<Ticket> getTicketListByBusIdAndStatus(String busId, String ticketStatusStr) {
        int status = Integer.parseInt(ticketStatusStr);
        return getTicketListByBusIdAndStatus(busId, status);
    }


    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public void bookTickets(String bookerId,
                            String[] seatIds,
                            String[] passengerNames,
                            String[] passengerPIDs,
                            String[] childSeats)
            throws BookingException, NumberFormatException {
        if (seatIds == null || passengerNames == null || passengerPIDs == null)
            throw new BookingException("CustomerException-Error occurred with inputs, please try again");

        int sizeCheck = seatIds.length;
        if (passengerNames.length != sizeCheck || passengerPIDs.length != sizeCheck)
            throw new BookingException("CustomerException-At least one field is not filled");

        Seat seatRepresentative = seatRepo.getById(seatIds[0]);
        Bus targetBus = seatRepresentative.getBus();


        //Drafting TICKETs

        DraftTicketContainer draftTicketContainer = prepareTicketDraftList(bookerId, seatIds, passengerNames, passengerPIDs, childSeats,
                targetBus.getId());

        //Booking
        bookTicketsFromDraftList(draftTicketContainer);

        double amountToPay = seatIds.length * targetBus.getPrice();
        Wallet bookerWallet = walletRepo.getByUserId(bookerId);

        doMoneyTransaction(bookerWallet, amountToPay);


    }

    private void bookTicketsFromDraftList(DraftTicketContainer draftTicketContainer) throws BookingException {

        //for 1
        List<DraftTicket> adultDrafts = draftTicketContainer.getAdultDraftList();

        //for 2
        List<DraftTicket> childrenDrafts = draftTicketContainer.getChildrenDraftList();

        for (DraftTicket draftTicket : adultDrafts) {

            int countPIDOnTheSameBus = ticketRepo
                    .checkDuplicatedPersonalIDCount(draftTicket.getBusId(), draftTicket.getPassengerPID());
            if (countPIDOnTheSameBus > 0)
                throw new BookingException("CustomerException-One passenger has already been booked on this " +
                        "bus! (Duplicated personal IDs are not allowed)");

            ticketRepo.createTicket(draftTicket.getSeatId(), draftTicket.getBookerId(),
                    draftTicket.getPassengerName(), draftTicket.getPassengerPID(), false);

            Seat seatTarget = seatRepo.getById(draftTicket.getSeatId());
            if (seatTarget.isBooked()) {
                throw new BookingException("Unfortunately at least one of your selected seats has been booked, " +
                        "please try again later");
            }

            seatRepo.changeSeatStatus(true, draftTicket.getSeatId());


        } // end for 1

        for (DraftTicket draftTicket : childrenDrafts) {

            int caretakerCheck = ticketRepo.countCaretakerOnTheSameBusByTheSameBookerAndIsBooking
                    (draftTicket.getBusId(), draftTicket.getBookerId(),
                            draftTicket.getPassengerPID());

            if (caretakerCheck < 1)
                throw new BookingException("CustomerException-Caretaker not found on this trip");

            int countChildren = ticketRepo.countChildrenAttachedToSpecificPIDOnTheSameBusByTheSameBookerAndIsBooking
                    (draftTicket.getBusId(), draftTicket.getBookerId(), draftTicket.getPassengerPID());
            if (countChildren > 1) {
                throw new BookingException("CustomerException-Caretaker can only have at most 2 children attached");
            }

            ticketRepo.createTicket(draftTicket.getSeatId(), draftTicket.getBookerId(),
                    draftTicket.getPassengerName(), draftTicket.getPassengerPID(), true);

            Seat seatTarget = seatRepo.getById(draftTicket.getSeatId());
            if (seatTarget.isBooked()) {
                throw new BookingException("Unfortunately at least one of your selected seats has been booked, " +
                        "please try again later");
            }

            seatRepo.changeSeatStatus(true, draftTicket.getSeatId());

        } // end for 2


    }

    private DraftTicketContainer prepareTicketDraftList(String bookerId, String[] seatIds,
                                                        String[] passengerNames, String[] passengerPIDs,
                                                        String[] childSeats, String busId) throws BookingException {
        List<DraftTicket> adultDraftTickets = new ArrayList<>();
        List<DraftTicket> childrenDraftTickets = new ArrayList<>();

        for (int i = 0; i < seatIds.length; i++) {
            if (stringValidator.isBlank(passengerNames[i]) || stringValidator.isBlank(passengerPIDs[i])) {
                throw new BookingException("CustomerException-At least one field is left blank or has whitespace input");
            }

            if (passengerPIDs[i].length() != 12) {
                throw new BookingException("CustomerException-At least one Passenger ID field doesn't satisfy requirement");
            }

            boolean isChild = false;
            if (childSeats != null) {
                for (String childSeat : childSeats) {
                    if (seatIds[i].equals(childSeat)) {
                        isChild = true;
                        break;
                    }
                }
            }


            DraftTicket draftTicket = DraftTicket.builder()
                    .bookerId(bookerId)
                    .seatId(seatIds[i])
                    .passengerName(passengerNames[i])
                    .passengerPID(passengerPIDs[i])
                    .isChild(isChild)
                    .busId(busId)
                    .build();

            if (!isChild) {
                adultDraftTickets.add(draftTicket);
            } else {
                childrenDraftTickets.add(draftTicket);
            }

        }
        return new DraftTicketContainer(adultDraftTickets, childrenDraftTickets);
    }

    private void doMoneyTransaction(Wallet bookerWallet, double amountToPay) throws BookingException {
        try {
            walletService.deduceBalance(bookerWallet.getId(), amountToPay);
        } catch (BookingException be) {
            throw new BookingException(be.getMessage());
        } catch (Exception ex) {
            throw new BookingException("CustomerException-Problem occurred during processing transaction for the checkout, transaction failed");
        }
    }

    public void setDraftDone(Checkout draft) {
        draft.setStatus(2);
        checkoutRepo.save(draft);
    }
}

