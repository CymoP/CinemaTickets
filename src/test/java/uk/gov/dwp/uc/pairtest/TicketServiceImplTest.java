package uk.gov.dwp.uc.pairtest;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.enums.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest extends TestCase {

  @Captor
  private ArgumentCaptor<Integer> captor;

  @Mock
  private TicketPaymentServiceImpl ticketPaymentService;

  @Mock
  private SeatReservationServiceImpl seatReservationService;

  @InjectMocks
  private TicketServiceImpl ticketService;

  private Long accountId;
  private TicketTypeRequest[] ticketTypeRequestsList;

  @Test
  public void testTicketsAreBookedWhenRequestIsValid() {
    givenValidAccountId(123L);
    givenValidTicketTypeRequest();
    whenPurchaseTicketsIsCalled();
    thenVerifyTicketPaymentServiceImplIsCalled();
    thenVerifySeatReservationServiceImplIsCalled();
    thenVerifyTicketPriceIsCalculatedCorrectly();
  }

  @Test
  public void testTicketsAreBookedWhenRequestIsValid_MaxAccountId() {
    givenValidAccountId(Long.MAX_VALUE);
    givenValidTicketTypeRequest();
    whenPurchaseTicketsIsCalled();
    thenVerifyTicketPaymentServiceImplIsCalled();
    thenVerifySeatReservationServiceImplIsCalled();
    thenVerifyTicketPriceIsCalculatedCorrectly();
  }

  @Test(expected = InvalidPurchaseException.class)
  public void testInvalidPurchaseExceptionIsThrownWhenRequestIsInvalid_AccountIdLessThanZero() {
    givenInvalidAccountId(-1L);
    givenValidTicketTypeRequest();
    whenPurchaseTicketsIsCalled();
  }

  @Test(expected = InvalidPurchaseException.class)
  public void testInvalidPurchaseExceptionIsThrownWhenRequestIsInvalid_AccountIdIsZero() {
    givenInvalidAccountId(0L);
    givenValidTicketTypeRequest();
    whenPurchaseTicketsIsCalled();
  }

  @Test(expected = InvalidPurchaseException.class)
  public void testInvalidPurchaseExceptionIsThrownWhenRequestIsInvalid_AccountIdIsNull() {
    givenInvalidAccountId(null);
    givenValidTicketTypeRequest();
    whenPurchaseTicketsIsCalled();
  }

  @Test(expected = InvalidPurchaseException.class)
  public void testInvalidPurchaseExceptionIsThrownWhenRequestIsInvalid_AccountIdAboveMaximum() {
    givenInvalidAccountId(Long.MAX_VALUE + 1L);
    givenValidTicketTypeRequest();
    whenPurchaseTicketsIsCalled();
  }

  @Test(expected = InvalidPurchaseException.class)
  public void testInvalidPurchaseExceptionIsThrownWhenRequestIsInvalid_NumberOfTicketsGreaterThan20() {
    givenValidAccountId(123L);
    givenInvalidTicketTypeRequest(21);
    whenPurchaseTicketsIsCalled();
  }

  @Test(expected = InvalidPurchaseException.class)
  public void testInvalidPurchaseExceptionIsThrownWhenRequestIsInvalid_NumberOfTicketsIsZero() {
    givenValidAccountId(123L);
    givenInvalidTicketTypeRequest(0);
    whenPurchaseTicketsIsCalled();
  }

  @Test(expected = InvalidPurchaseException.class)
  public void testInvalidPurchaseExceptionIsThrownWhenRequestIsInvalid_NumberOfTicketsIsNull() {
    givenValidAccountId(123L);
    whenPurchaseTicketsIsCalled();
  }

  @Test(expected = InvalidPurchaseException.class)
  public void testInvalidPurchaseExceptionIsThrownWhenRequestIsInvalid_InfantTicketButNoAdult() {
    givenValidAccountId(123L);
    givenInvalidTicketTypeRequest(Type.INFANT);
    whenPurchaseTicketsIsCalled();
  }

  @Test(expected = InvalidPurchaseException.class)
  public void testInvalidPurchaseExceptionIsThrownWhenRequestIsInvalid_ChildTicketButNoAdult() {
    givenValidAccountId(123L);
    givenInvalidTicketTypeRequest(Type.CHILD);
    whenPurchaseTicketsIsCalled();
  }

  private void givenValidTicketTypeRequest() {
    TicketTypeRequest ticketTypeRequestAdult = new TicketTypeRequest(Type.ADULT, 3);
    TicketTypeRequest ticketTypeRequestInfant = new TicketTypeRequest(Type.INFANT, 3);

    ticketTypeRequestsList = new TicketTypeRequest[]{ticketTypeRequestAdult, ticketTypeRequestInfant};
  }

  private void givenInvalidTicketTypeRequest(Integer numberOfTicketTypeRequests) {
    TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(Type.ADULT, numberOfTicketTypeRequests);

    ticketTypeRequestsList = new TicketTypeRequest[]{ticketTypeRequest1};
  }

  private void givenInvalidTicketTypeRequest(Type typeOfCustomer) {
    TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(typeOfCustomer, 1);

    ticketTypeRequestsList = new TicketTypeRequest[]{ticketTypeRequest1};
  }

  private void givenValidAccountId(Long accountId) {
    this.accountId = accountId;
  }

  private void givenInvalidAccountId(Long accountId) {
    this.accountId = accountId;
  }

  private void whenPurchaseTicketsIsCalled() {
    ticketService.purchaseTickets(accountId, ticketTypeRequestsList);
  }

  private void thenVerifySeatReservationServiceImplIsCalled() {
    verify(seatReservationService, Mockito.times(1)).reserveSeat(anyLong(), anyInt());
  }

  private void thenVerifyTicketPaymentServiceImplIsCalled() {
    verify(ticketPaymentService, Mockito.times(1)).makePayment(anyLong(), anyInt());
  }

  private void thenVerifyTicketPriceIsCalculatedCorrectly() {
    verify(ticketPaymentService).makePayment(captor.capture(), captor.capture());
    assertEquals(60, captor.getValue().longValue());
  }
}