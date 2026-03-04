package de.splatgames.software.external.afbb.parver.security;

import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(@NotNull final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @NotNull
    public UserDetails loadUserByUsername(@NotNull final String username) throws UsernameNotFoundException {
        final UserEntity userEntity = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new User(
                userEntity.getUsername(),
                userEntity.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name()))
        );
    }
}
