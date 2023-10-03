package com.iams.User;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetUserDetailsRequest {
    private String email;
}
