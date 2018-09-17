package nz.ac.auckland.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.*;


/**
 * DTO class to represent users. 
 * 
 * A UserDTO describes a user in terms of:
 * _username  the user's unique username.
 * _password  the user's password.
 * _firstname the user's first name.
 * _lastname  the user's family name.
 *
 */
@Entity
@Table(name = "USER")
public class User {

	@Id
	@Column(name = "USERNAME", nullable = false, unique = true)
	private String _username;

	@Column(name = "PASSWORD", nullable = false)
	private String _password;

	@Column(name = "FIRSTNAME", nullable = false)
	private String _firstname;

	@Column(name = "LASTNAME", nullable = false)
	private String _lastname;

	@Column(unique = true)
	private String _token;

	@ManyToOne(cascade = CascadeType.ALL)
	private CreditCard _creditCard;

	protected User() {}

	public User(String username, String password, String lastname, String firstname) {
		_username = username;
		_password = password;
		_lastname = lastname;
		_firstname = firstname;
		_token = null;
	}

	public User(String username, String password) {
		this(username, password, null, null);
	}
	
	public String getUsername() {
		return _username;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public String getFirstname() {
		return _firstname;
	}
	
	public String getLastname() {
		return _lastname;
	}

	public CreditCard getCreditCard() { return _creditCard; }

	public void setCreditCard(CreditCard card){ _creditCard = card; }

	public String getToken() { return _token; }

	public void setToken(String token){ _token = token; }

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User))
            return false;
        if (obj == this)
            return true;

        User rhs = (User) obj;
        return new EqualsBuilder().
            append(_username, rhs._username).
            append(_password, rhs._password).
            append(_firstname, rhs._firstname).
            append(_lastname, rhs._lastname).
            isEquals();
	}

}
