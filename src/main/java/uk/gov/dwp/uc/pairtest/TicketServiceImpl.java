package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

  private boolean adultRequired = false;
  private int numberOfSeatsRequired;
  private int numberOfTickets;
  private boolean adultIsPresent;
  private int totalAmountToPay;
  private int totalSeatsToAllocate;
  private TicketPaymentServiceImpl ticketPaymentService = new TicketPaymentServiceImpl();
  private SeatReservationServiceImpl seatReservationService = new SeatReservationServiceImpl();
  public static final int MAXIMUM_NUMBER_OF_TICKETS = 20;
  public static final int MINIMUM_NUMBER_OF_TICKETS = 0;

  /**
   * Should only have private methods other than the one below.
   */
  @Override
  public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
      throws InvalidPurchaseException {
    checkAccountIdIsValid(accountId);

    if (ticketTypeRequests != null) {
      for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
        if (ticketTypeRequest.getTicketType() == Type.INFANT) {
          adultRequired = true;
          numberOfTickets += ticketTypeRequest.getNoOfTickets();
        } else if (ticketTypeRequest.getTicketType() == Type.CHILD) {
          adultRequired = true;
          numberOfSeatsRequired += ticketTypeRequest.getNoOfTickets();
          numberOfTickets += ticketTypeRequest.getNoOfTickets();
          totalAmountToPay += (numberOfTickets * 10);
        } else if (ticketTypeRequest.getTicketType() == Type.ADULT) {
          adultIsPresent = true;
          numberOfSeatsRequired += ticketTypeRequest.getNoOfTickets();
          numberOfTickets += ticketTypeRequest.getNoOfTickets();
          totalAmountToPay += (numberOfTickets * 20);
        } else {
          throw new InvalidPurchaseException();
        }
      }
    }

    checkAmountOfTicketsIsValid();
    checkAdultIsPresent();

    ticketPaymentService.makePayment(accountId, totalAmountToPay);
    seatReservationService.reserveSeat(accountId, numberOfSeatsRequired);
  }

  private void checkAmountOfTicketsIsValid() {
    if (numberOfTickets <= MINIMUM_NUMBER_OF_TICKETS
        || numberOfTickets > MAXIMUM_NUMBER_OF_TICKETS) {
      throw new InvalidPurchaseException();
    }
  }

  private static void checkAccountIdIsValid(Long accountId) {
    if (accountId == null || accountId <= 0) {
      throw new InvalidPurchaseException();
    }
  }

  private void checkAdultIsPresent() {
    if (adultRequired && !adultIsPresent) {
      throw new InvalidPurchaseException();
    }
  }
}
