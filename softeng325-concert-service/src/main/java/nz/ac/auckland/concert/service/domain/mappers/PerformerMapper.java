package nz.ac.auckland.concert.service.domain.mappers;


import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.Concert;

import java.util.stream.Collectors;

public class PerformerMapper {
    public static PerformerDTO toDTO(Performer performer){
        return new PerformerDTO(
                performer.getId(),
                performer.getName(),
                performer.getImageName(),
                performer.getGenre(),
                performer.getConcerts().stream().map(Concert::getId).collect(Collectors.toSet())
        );
    }
}
