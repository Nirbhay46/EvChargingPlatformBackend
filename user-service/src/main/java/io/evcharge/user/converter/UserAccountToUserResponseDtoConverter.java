package io.evcharge.user.converter;

import io.evcharge.user.api.dto.UserResponse;
import io.evcharge.user.domain.UserAccount;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserAccountToUserResponseDtoConverter implements Converter<UserAccount, UserResponse> {

    @Override
    public UserResponse convert(UserAccount u) {
        return new UserResponse(
                u.getId(), u.getEmail(), u.getFullName(),
                u.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                u.isEnabled()
        );
    }
}
