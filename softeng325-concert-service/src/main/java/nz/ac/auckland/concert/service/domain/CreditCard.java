package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;


import javax.persistence.*;

import java.time.LocalDate;

/**
 * DTO class to represent credit cards. 
 * 
 * A CreditCardDTO describes a credit card in terms of:
 * _type       type of credit card, Visa or Mastercard.
 * _name       the name of the person who owns the credit card.
 * _number     16-digit credit card number. 
 * _expiryDate the credit card's expiry date. 
 *
 */
@Entity
@Table(name = "CREDITCARDS")
public class CreditCard {

	@Column(name = "TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	private CreditCardDTO.Type _type;

	@Column(name = "NAME", nullable = false)
	private String _name;

	@Id
	@Column(name = "NUMBER", nullable = false, unique = true)
	private String _number;

	@Column(name = "EXPIRYDATE", nullable = false)
	private LocalDate _expiryDate;

	public CreditCard() {}

	public CreditCard(CreditCardDTO.Type type, String name, String number, LocalDate expiryDate) {
		_type = type;
		_name = name;
		_number = number;
		_expiryDate = expiryDate;
	}

	public CreditCard(CreditCardDTO dtoCard){
		_type = dtoCard.getType();
		_name = dtoCard.getName();
		_number = dtoCard.getNumber();
		_expiryDate = dtoCard.getExpiryDate();
	}
	
	public CreditCardDTO.Type getType() {
		return _type;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getNumber() {
		return _number;
	}

	public LocalDate getExpiryDate() {
		return _expiryDate;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CreditCard))
            return false;
        if (obj == this)
            return true;

        CreditCard rhs = (CreditCard) obj;
        return new EqualsBuilder().
            append(_type, rhs._type).
            append(_name, rhs._name).
            append(_number, rhs._number).
            append(_expiryDate, rhs._expiryDate).
            isEquals();
	}

}
