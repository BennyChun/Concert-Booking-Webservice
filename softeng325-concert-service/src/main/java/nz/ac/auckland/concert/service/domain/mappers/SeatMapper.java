package nz.ac.auckland.concert.service.domain.mappers;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.Seat;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SeatMapper {
    public static SeatDTO toDTO(Seat seat){
        return new SeatDTO(
                seat.getRow(),
                seat.getNumber()
        );
    }

    public static Set<SeatDTO> toDTOSet(Set<Seat> seats) {
        Set<SeatDTO> dtoSeats = new HashSet<>();
        dtoSeats.addAll(seats.stream().map(SeatMapper::toDTO).collect(Collectors.toSet()));
        return dtoSeats;
    }
}
