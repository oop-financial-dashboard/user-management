package com.iams.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetUserDetailsResponse {
    private Integer id;
    private String firstname;
    private String lastname;
    private Role role;
}
