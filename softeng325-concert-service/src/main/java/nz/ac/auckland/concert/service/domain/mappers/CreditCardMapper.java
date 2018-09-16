package nz.ac.auckland.concert.service.domain.mappers;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.service.domain.CreditCard;

public class CreditCardMapper {
    public static CreditCardDTO toDTO(CreditCard creditCard){
        return new CreditCardDTO(
                creditCard.getType(),
                creditCard.getName(),
                creditCard.getNumber(),
                creditCard.getExpiryDate()
        );
    }
}
