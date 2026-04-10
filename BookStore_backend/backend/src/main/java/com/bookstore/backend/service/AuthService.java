package com.bookstore.backend.service;

import java.util.List;

import com.bookstore.backend.dto.request.RegistrationRequest;import com.bookstore.backend.entity.Role;import com.bookstore.backend.repository.RoleRepository;import jakarta.transaction.Transactional;import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bookstore.backend.dto.request.RegistrationRequest;
import com.bookstore.backend.dto.response.JwtTokenResponse;
import com.bookstore.backend.entity.AppUser;
import com.bookstore.backend.repository.AppUserRepository;
import com.bookstore.backend.repository.AppUserRepository;
import com.bookstore.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    //bs BE-AUTH-01
    private final RoleRepository roleRepository;
    //bs
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    //bs BE-AUTH-01
    @Transactional
    public void register(RegistrationRequest request){
        if(appUserRepository.existsByUsername(request.username())){
            throw new RuntimeException("Username đã tồn tại!");
        }
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role CUSTOMER"));
        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(customerRole);
        appUserRepository.save(user);

    }

//    public AuthService(
//            AppUserRepository appUserRepository,
//            PasswordEncoder passwordEncoder,
//            JwtService jwtService
//    ) {
//        this.appUserRepository = appUserRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.jwtService = jwtService;
//    }

    public JwtTokenResponse login(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        System.out.println("INPUT password: " + password);
        System.out.println("DB password: " + user.getPassword());
        System.out.println("MATCH: " + passwordEncoder.matches(password, user.getPassword()));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return jwtService.generateToken(user.getUsername(), List.of(user.getRole().getName()));
    }
}
