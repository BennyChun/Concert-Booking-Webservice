package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * DTO class to represent seats at the concert venue. 
 * 
 * A SeatDTO describes a seat in terms of:
 * _row    the row of the seat.
 * _number the number of the seat.
 *
 */
@Entity
@Table(name = "SEAT")
public class Seat {

	@Version
	@Column(nullable = false)
	private Long _version = 0L;

	@Id
	@GeneratedValue
	@Column(name = "SEAT_ID", nullable = false, unique = true)
	private Long _id;

	@Column(name = "ROW", nullable = false)
	@Enumerated(EnumType.STRING)
	private SeatRow _row;

	@Column(name = "SEAT_NUMBER", nullable = false)
	@Convert(converter = SeatNumberConverter.class)
	private SeatNumber _number;

	@Column(name = "PRICE_BAND")
	@Enumerated(EnumType.STRING)
	private PriceBand _price;

	@Column(name = "DATE")
	private LocalDateTime _date;

	@Column(name = "TIMESTAMP")
	private LocalDateTime _timestamp;

	@ManyToOne(cascade = CascadeType.ALL)
	private Reservation _reservation;

	public Seat() {}

	public Seat(SeatRow row, SeatNumber number) {
		_row = row;
		_number = number;
	}
	
	public SeatRow getRow() {
		return _row;
	}
	
	public SeatNumber getNumber() {
		return _number;
	}

	public Long get_id() {
		return _id;
	}

	public PriceBand get_price() {
		return _price;
	}

	public void set_price(PriceBand _price) {
		this._price = _price;
	}

	public LocalDateTime get_date() {
		return _date;
	}

	public void set_date(LocalDateTime _date) {
		this._date = _date;
	}

	public LocalDateTime get_timestamp() {
		return _timestamp;
	}

	public void set_timestamp(LocalDateTime _timestamp) {
		this._timestamp = _timestamp;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Seat))
            return false;
        if (obj == this)
            return true;

        Seat rhs = (Seat) obj;
        return new EqualsBuilder().
            append(_row, rhs._row).
            append(_number, rhs._number).
            isEquals();
	}
	
	@Override
	public String toString() {
		return _row + _number.toString();
	}
}
