package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.Genre;
import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO class to represent performers. 
 * 
 * A PerformerDTO describes a performer in terms of:
 * _id         the unique identifier for a performer.
 * _name       the performer's name.
 * _imageName  the name of an image file for the performer.
 * _genre      the performer's genre.
 * _concertIds identification of each concert in which the performer is 
 *             playing. 
 *             
 */
@Entity
@Table(name = "PERFORMERS")
public class Performer {

	@Id
	@GeneratedValue
	@Column(name = "PERFORMER_ID", nullable = false, unique = true)
	private Long _id;

	@Column(name = "PERFORMER_NAME", nullable = false)
	private String _name;

	@Column(name = "PERFORMER_IMAGE", nullable = false)
	private String _imageName;

	@Column(name = "PERFORMER_GENRE", nullable = false)
	@Enumerated(EnumType.STRING)
	private Genre _genre;

	@ManyToMany(mappedBy = "_performers")
	@Column(name = "CONCERT")
	private Set<Concert> _concerts;

	public Performer() {}

	public Performer(Long id, String name, String imageName, Genre genre, Set<Concert> concerts) {
		_id = id;
		_name = name;
		_imageName = imageName;
		_genre = genre;
		_concerts = new HashSet<>(concerts);
	}
	
	public Long getId() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getImageName() {
		return _imageName;
	}

	public Genre getGenre() { return _genre; }
	
	public Set<Concert> getConcerts() {
		return Collections.unmodifiableSet(_concerts);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Performer))
            return false;
        if (obj == this)
            return true;

        Performer rhs = (Performer) obj;
        return new EqualsBuilder().
            append(_name, rhs._name).
            append(_imageName, rhs._imageName).
            append(_genre, rhs._genre).
            append(_concerts, rhs._concerts).
            isEquals();
	}
	
	/*@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_name).
	            append(_imageName).
	            append(_genre).
	            hashCode();
	}*/
}
