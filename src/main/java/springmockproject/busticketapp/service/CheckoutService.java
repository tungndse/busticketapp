package springmockproject.busticketapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springmockproject.busticketapp.entity.*;
import springmockproject.busticketapp.exception.BookingException;
import springmockproject.busticketapp.repository.*;
import springmockproject.busticketapp.validator.BusAppStringValidator;

import java.util.List;

@Slf4j
@Service
public class CheckoutService {


    UserRepository userRepo;

    TicketRepository ticketRepo;

    SeatRepository seatRepo;

    BusRepository busRepo;

    WalletRepository walletRepo;

    CheckoutRepository checkoutRepo;

    CheckoutSeatRepository checkoutSeatRepo;

    BusAppStringValidator stringValidator;


    @Autowired
    public CheckoutService(TicketRepository ticketRepo,
                           SeatRepository seatRepo,
                           UserRepository userRepo,
                           BusRepository busRepo,
                           WalletRepository walletRepo,
                           CheckoutRepository checkoutRepo,
                           CheckoutSeatRepository checkoutSeatRepo,
                           BusAppStringValidator stringValidator) {
        this.ticketRepo = ticketRepo;
        this.seatRepo = seatRepo;
        this.userRepo = userRepo;
        this.busRepo = busRepo;
        this.walletRepo = walletRepo;
        this.checkoutRepo = checkoutRepo;
        this.checkoutSeatRepo = checkoutSeatRepo;
        this.stringValidator = stringValidator;

    }

    public Checkout getById(long id) {
        return checkoutRepo.getById(id);
    }

    public Checkout getById(String id) {
        return checkoutRepo.getById(Long.parseLong(id));
    }

    public Checkout getOngoingCheckoutForBooker(String bookerId, String busId) {
        User booker = userRepo.getUserById(bookerId);
        Bus bus = busRepo.getById(busId);
        List<Checkout> list = checkoutRepo.getByBookerAndBusAndStatus(booker, bus, 1);
        if (list != null && !list.isEmpty())
            return list.get(0);
        else
            return null;
    }


    @Transactional(rollbackFor = Exception.class)
    public long updateDraft(String draftId, String[] seatIds,
                            String[] passengerNames, String[] passengerPIDs, String[] childSeats)
            throws BookingException, NumberFormatException {

        if (seatIds == null || passengerNames == null || passengerPIDs == null)
            throw new BookingException("CustomerException-Error occurred with inputs, please try again");

        int sizeCheck = seatIds.length;
        if (passengerNames.length != sizeCheck || passengerPIDs.length != sizeCheck)
            throw new BookingException("CustomerException-At least one field is not filled");


        Checkout checkout = checkoutRepo.getById(Long.parseLong(draftId));

        List<CheckoutSeat> checkoutSeats = checkout.getCheckoutSeats();

        for (CheckoutSeat checkoutSeat : checkoutSeats) {
            for (int i = 0; i < seatIds.length; i++) {
                if (seatIds[i].equals(checkoutSeat.getSeat().getId())) {
                    checkoutSeat.setPassengerName(passengerNames[i]);
                    checkoutSeat.setPassengerIdentity(passengerPIDs[i]);

                    boolean isChild = false;
                    if (childSeats != null) {
                        for (String childSeat : childSeats) {
                            if (childSeat.equals(seatIds[i])) {
                                isChild = true;
                                break;
                            }
                        }
                    }

                    checkoutSeat.setPassengerIsChild(isChild);

                    checkoutSeatRepo.save(checkoutSeat);

                    break;
                }
            }

        }

        return checkout.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public long makingDraft(String bookerId,
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

        Checkout checkout = Checkout.builder()
                .booker(userRepo.getUserById(bookerId))
                .bus(targetBus)
                .status(1)
                .build();

        checkout = checkoutRepo.save(checkout);

        for (int i = 0; i < seatIds.length; i++) {
            Seat seat = seatRepo.getById(seatIds[i]);

            boolean isChild = false;
            if (childSeats != null) {
                for (String childSeat : childSeats) {
                    if (childSeat.equals(seatIds[i])) {
                        isChild = true;
                        break;
                    }
                }
            }

            CheckoutSeat checkoutSeat = CheckoutSeat.builder()
                    .checkout(checkout)
                    .seat(seat)
                    .passengerName(passengerNames[i])
                    .passengerIdentity(passengerPIDs[i])
                    .passengerIsChild(isChild)
                    .build();

            checkoutSeatRepo.save(checkoutSeat);

        }

        return checkout.getId();

    }


    public void cancelDraft(String draftId) throws Exception {
        Checkout draft = checkoutRepo.getById(Long.parseLong(draftId));
        draft.setStatus(0);
        checkoutRepo.save(draft);
    }
}
