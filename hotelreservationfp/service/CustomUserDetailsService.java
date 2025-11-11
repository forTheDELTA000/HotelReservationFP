package com.example.hotelreservationfp.service; // Or a new package you create

import com.example.hotelreservationfp.entity.AdminUser;
import com.example.hotelreservationfp.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service // Tells Spring this is a managed service
public class CustomUserDetailsService implements UserDetailsService {

    // Inject your repository so we can find users
    @Autowired
    private AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 'username' is the email from the login form

        // 1. Find the user in your database
        AdminUser adminUser = adminUserRepository.findByEmail(username);

        // 2. If user not found, throw an error
        if (adminUser == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        // 3. Return a Spring Security User object
        // This gives Spring Security the user's email, their HASHED password,
        // and an empty list of roles.
        return new User(
                adminUser.getEmail(),
                adminUser.getPassword(),
                new ArrayList<>()
        );
    }
}