package uk.gov.dwp.uc.pairtest;

import static uk.gov.dwp.uc.pairtest.utils.RequestValidation.checkAccountIdIsValid;
import static uk.gov.dwp.uc.pairtest.utils.RequestValidation.checkAmountOfTicketsIsValid;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.enums.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

  private boolean adultRequired = false;
  private int numberOfSeatsRequired;
  private int numberOfTickets;
  private boolean adultIsPresent;
  private int totalAmountToPay;
  private TicketPaymentServiceImpl ticketPaymentService = new TicketPaymentServiceImpl();
  private SeatReservationServiceImpl seatReservationService = new SeatReservationServiceImpl();

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

    checkAmountOfTicketsIsValid(numberOfTickets);
    checkAdultIsPresent();

    ticketPaymentService.makePayment(accountId, totalAmountToPay);
    seatReservationService.reserveSeat(accountId, numberOfSeatsRequired);
  }

  private void checkAdultIsPresent() {
    if (adultRequired && !adultIsPresent) {
      throw new InvalidPurchaseException();
    }
  }
}
