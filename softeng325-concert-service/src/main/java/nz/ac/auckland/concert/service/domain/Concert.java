package nz.ac.auckland.concert.service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import nz.ac.auckland.concert.common.types.PriceBand;

import nz.ac.auckland.concert.service.domain.Performer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * DTO class to represent concerts. 
 * 
 * A ConcertDTO describes a concert in terms of:
 * _id           the unique identifier for a concert.
 * _title        the concert's title.
 * _dates        the concert's scheduled dates and times (represented as a 
 *               Set of LocalDateTime instances).
 * _tariff       concert pricing - the cost of a ticket for each price band 
 *               (A, B and C) is set individually for each concert. 
 * _performerIds identification of each performer playing at a concert 
 *               (represented as a set of performer identifiers).
 *
 */
@Entity
@Table(name = "CONCERTS")
public class Concert{

	@Id
	@GeneratedValue
	@Column(name = "CONCERT_ID", nullable = false, unique = true)
	private Long _id;

	@Column(name = "CONCERT_TITLE", nullable = false)
	private String _title;

	@ElementCollection
	@CollectionTable(name = "CONCERT_DATES", joinColumns = @JoinColumn(name ="CONCERT_ID"))
	@Column(name = "CONCERT_DATE", nullable = false)
	private Set<LocalDateTime> _dates;

	@ElementCollection
	@CollectionTable(name = "CONCERT_TARIFS", joinColumns = @JoinColumn(name = "CONCERT_ID"))
	@MapKeyEnumerated(EnumType.STRING)
	@MapKeyColumn(name = "CONCERT_TARIFF")
	private Map<PriceBand, BigDecimal> _tariff;

	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable(
			name="CONCERT_PERFORMER",
			joinColumns= @JoinColumn(name = "PERFORMER_ID"),
			inverseJoinColumns = @JoinColumn(name = "CONCERT_ID"))
	@Column(name = "PERFORMER", nullable = false)
	private Set<Performer> _performers;

	public Concert() {
	}

	public Concert(Long id, String title, Set<LocalDateTime> dates,
                   Map<PriceBand, BigDecimal> ticketPrices, Set<Performer> performers) {
		_id = id;
		_title = title;
		_dates = new HashSet<>(dates);
		_tariff = new HashMap<>(ticketPrices);
		_performers = new HashSet<>(performers);
	}

	public Long getId() {
		return _id;
	}

	public String getTitle() {
		return _title;
	}

	public Set<LocalDateTime> getDates() {
		return Collections.unmodifiableSet(_dates);
	}

	public Map<PriceBand, BigDecimal> getTariff() {
		return _tariff;
	}

	public void setTariff(Map<PriceBand, BigDecimal> tariff) {
		_tariff = new HashMap<>(tariff);
	}

	public BigDecimal getTicketPrice(PriceBand seatType) {
		return _tariff.get(seatType);
	}

	public Set<Performer> getPerformers() {
		return Collections.unmodifiableSet(_performers);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
            append(_title, rhs._title).
            append(_dates, rhs._dates).
            append(_tariff, rhs._tariff).
            append(_performers, rhs._performers).
            isEquals();
	}
	
	/*@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_title).
	            append(_dates).
	            append(_tariff).
	            append(_performers).
	            hashCode();
	}*/
}
