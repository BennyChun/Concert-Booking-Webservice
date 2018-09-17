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

	@Column(name = "CONCERT_DATE")
	private LocalDateTime _date;

	@Column(name = "EXPIRY")
	private LocalDateTime _expiryDate;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(nullable = false)
	private User _user;

	@Column(name = "CONFIRMED", nullable = false)
	private boolean _confirmed;

	public Reservation() {}

	public Reservation(Long id, Set<Seat> seats) {
		_id = id;
		_seats = new HashSet<Seat>(seats);
	}

	public Reservation(Set<Seat> _seats, PriceBand _priceBand, Concert _concert, LocalDateTime _date, LocalDateTime _expiryDate, User _user, boolean _confirmed) {
		this._seats = _seats;
		this._priceBand = _priceBand;
		this._concert = _concert;
		this._date = _date;
		this._expiryDate = _expiryDate;
		this._user = _user;
		this._confirmed = _confirmed;
	}

	public Long getId() {
		return _id;
	}
	
	public Set<Seat> getSeats() {
		return Collections.unmodifiableSet(_seats);
	}

	public PriceBand get_priceBand() {
		return _priceBand;
	}

	public void set_priceBand(PriceBand _priceBand) {
		this._priceBand = _priceBand;
	}

	public Concert get_concert() {
		return _concert;
	}

	public void set_concert(Concert _concert) {
		this._concert = _concert;
	}

	public LocalDateTime get_date() {
		return _date;
	}

	public void set_date(LocalDateTime _date) {
		this._date = _date;
	}

	public LocalDateTime get_expiryDate() {
		return _expiryDate;
	}

	public void set_expiryDate(LocalDateTime _expiryDate) {
		this._expiryDate = _expiryDate;
	}

	public User get_user() {
		return _user;
	}

	public void set_user(User _user) {
		this._user = _user;
	}

	public boolean is_confirmed() {
		return _confirmed;
	}

	public void set_confirmed(boolean _confirmed) {
		this._confirmed = _confirmed;
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
