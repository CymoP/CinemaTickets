package uk.gov.dwp.uc.pairtest.utils;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

/**
 * Class to define the validation criteria for the TicketService.
 */
public class RequestValidation {

  public static final int MAXIMUM_NUMBER_OF_TICKETS = 20;
  public static final int MINIMUM_NUMBER_OF_TICKETS = 0;

  private RequestValidation() {
  }

  public static void checkAmountOfTicketsIsValid(final int numberOfTickets) {
    if (numberOfTickets <= MINIMUM_NUMBER_OF_TICKETS
        || numberOfTickets > MAXIMUM_NUMBER_OF_TICKETS) {
      throw new InvalidPurchaseException();
    }
  }

  public static void checkAccountIdIsValid(Long accountId) {
    if (accountId == null || accountId <= 0) {
      throw new InvalidPurchaseException();
    }
  }
}
