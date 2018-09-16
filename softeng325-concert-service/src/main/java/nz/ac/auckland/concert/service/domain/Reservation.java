package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO class to represent reservations. 
 * 
 * A ReservationDTO describes a reservation in terms of:
 * _id                 the unique identifier for a reservation.
 * _reservationRequest details of the corresponding reservation request, 
 *                     including the number of seats and their type, concert
 *                     identity, and the date/time of the concert for which a 
 *                     reservation was requested.
 * _seats              the seats that have been reserved (represented as a Set
 *                     of SeatDTO objects).
 *
 */
@Entity
@Table(name = "RESERVATIONS")
public class Reservation {

	@Version
	@Column(nullable = false)
	private Long _version = 0L;

	@Id
	@GeneratedValue
	@Column(name = "RES_ID", nullable = false)
	private Long _id;

	@OneToMany(mappedBy = "_reservation", cascade = CascadeType.ALL)
	@Column(nullable = false)
	private Set<Seat> _seats;

	@Column(name = "PRICE_BAND", nullable = false)
	@Enumerated(EnumType.STRING)
	private PriceBand _priceBand;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn
	private Concert _concert;

	@Column(name = "DATE")
	private LocalDateTime _date;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(nullable = false)
	private User _user;

	public Reservation() {}

	public Reservation(Long id, ReservationRequestDTO request, Set<Seat> seats) {
		_id = id;
		_seats = new HashSet<Seat>(seats);
	}
	
	public Long getId() {
		return _id;
	}
	
	public Set<Seat> getSeats() {
		return Collections.unmodifiableSet(_seats);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Reservation))
            return false;
        if (obj == this)
            return true;

        Reservation rhs = (Reservation) obj;
        return new EqualsBuilder().
            append(_seats, rhs._seats).
            isEquals();
	}

}
