package nz.ac.auckland.concert.service.domain.mappers;

import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.service.domain.Reservation;

public class ReservationMapper {
    public static ReservationDTO toDto(Reservation res){
        return new ReservationDTO(
                res.getId(),
                new ReservationRequestDTO(
                    res.getSeats().size(),
                        res.get_priceBand(),
                        res.get_concert().getId(),
                        res.get_date()
                ),
                SeatMapper.toDTOSet(res.getSeats())
        );
    }
}
