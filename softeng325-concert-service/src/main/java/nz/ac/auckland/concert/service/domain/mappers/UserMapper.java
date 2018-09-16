package nz.ac.auckland.concert.service.domain.mappers;

import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.User;

public class UserMapper {
    public static UserDTO toDTO(User user){
        return new UserDTO(
                user.getUsername(),
                user.getPassword(),
                user.getFirstname(),
                user.getLastname()
        );
    }
}
