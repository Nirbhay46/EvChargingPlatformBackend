package io.evcharge.user.service;

import io.evcharge.user.api.dto.UserResponse;
import io.evcharge.user.converter.UserAccountToUserResponseDtoConverter;
import io.evcharge.user.domain.UserAccount;
import io.evcharge.user.exception.ApiException;
import io.evcharge.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAccountToUserResponseDtoConverter converter;

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        UserAccount u = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return converter.convert(u);
    }

    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        UserAccount u = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return converter.convert(u);
    }
}
