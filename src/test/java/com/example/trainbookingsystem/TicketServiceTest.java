package com.example.trainbookingsystem;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.trainbookingsystem.entity.Ticket;
import com.example.trainbookingsystem.entity.Train;
import com.example.trainbookingsystem.entity.User;
import com.example.trainbookingsystem.repository.TicketRepository;
import com.example.trainbookingsystem.service.TicketService;
import com.example.trainbookingsystem.service.UserService;
import com.example.trainbookingsystem.service.TrainService;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;

    @Mock
    private TrainService trainService;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;
    private User user;
    private Train train;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        train = new Train();
        train.setId(1L);
        train.setName("Express A");
        train.setSource("City A");
        train.setDestination("City B");
        train.setBasePrice(15.0);
        train.setDiscountPercentage(10.0);

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setUser(user);
        ticket.setTrain(train);
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setFinalPrice(13.5); // example
    }

    @Test
    void getAllTickets_ShouldReturnList() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findAll()).thenReturn(tickets);

        List<Ticket> result = ticketService.getAllTickets();

        assertEquals(1, result.size());
        assertEquals(ticket.getUser().getName(), result.get(0).getUser().getName());
        verify(ticketRepository, times(1)).findAll();
    }

    @Test
    void getTicketById_WhenExists_ShouldReturn() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        Optional<Ticket> result = ticketService.getTicketById(1L);

        assertTrue(result.isPresent());
        assertEquals(ticket.getFinalPrice(), result.get().getFinalPrice());
        verify(ticketRepository, times(1)).findById(1L);
    }

    @Test
    void getTicketById_WhenNotExists_ShouldReturnEmpty() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Ticket> result = ticketService.getTicketById(1L);

        assertFalse(result.isPresent());
        verify(ticketRepository, times(1)).findById(1L);
    }

    @Test
    void createTicket_ShouldSaveAndReturn() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(trainService.getTrainById(1L)).thenReturn(Optional.of(train));

        Ticket result = ticketService.createTicket(1L, 1L);

        assertNotNull(result);
        assertEquals(ticket.getUser().getId(), result.getUser().getId());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void createTicket_WhenUserNotFound_ShouldThrow() {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> ticketService.createTicket(1L, 1L));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void createTicket_WhenTrainNotFound_ShouldThrow() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(trainService.getTrainById(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> ticketService.createTicket(1L, 1L));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void updateTicket_WhenExists_ShouldUpdate() {
        Ticket updated = new Ticket();
        updated.setUser(user);
        updated.setTrain(train);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.updateTicket(1L, updated);

        assertEquals(ticket.getUser().getId(), result.getUser().getId());
        verify(ticketRepository, times(1)).findById(1L);
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void updateTicket_WhenDetailsHaveNoUserOrTrain_ShouldOnlyUpdateBookingDate() {
        Ticket updated = new Ticket();
        updated.setUser(null);
        updated.setTrain(null);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        LocalDateTime before = ticket.getBookingDate();
        Ticket result = ticketService.updateTicket(1L, updated);`   
        assertEquals(ticket.getUser().getId(), result.getUser().getId());
        assertEquals(ticket.getTrain().getId(), result.getTrain().getId());
        assertTrue(before.isBefore(result.getBookingDate()) || before.isAfter(result.getBookingDate()), 
                  "Booking date should be updated");
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void updateTicket_WhenOnlyUserProvided_ShouldUpdateUserOnly() {
        User newUser = new User();
        newUser.setId(2L);
        newUser.setName("Alice");
        newUser.setEmail("alice@example.com");

        Ticket updated = new Ticket();
        updated.setUser(newUser);
        updated.setTrain(null);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.updateTicket(1L, updated);

        assertEquals(newUser.getId(), result.getUser().getId());
        assertEquals(ticket.getTrain().getId(), result.getTrain().getId());
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void updateTicket_WhenNotExists_ShouldThrow() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> ticketService.updateTicket(1L, ticket));
        verify(ticketRepository, times(1)).findById(1L);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void deleteTicket_ShouldDelete() {
        doNothing().when(ticketRepository).deleteById(1L);

        ticketService.deleteTicket(1L);

        verify(ticketRepository, times(1)).deleteById(1L);
    }

    @Test
    void calculateTicketPrice_ShouldComputeCorrectly() {
        double price = ticketService.calculateTicketPrice(100.0, 10.0);
        assertEquals(90.0, price);
    }
}