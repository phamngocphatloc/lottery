package com.lottery.looterry.Controller;


import com.lottery.looterry.entity.Users;
import com.lottery.looterry.payload.request.ChangePasswordRequest;
import com.lottery.looterry.payload.request.LoginRequest;
import com.lottery.looterry.payload.request.UserInfoRequest;
import com.lottery.looterry.payload.request.UserRequest;
import com.lottery.looterry.payload.response.HttpResponse;
import com.lottery.looterry.payload.response.JwtResponse;
import com.lottery.looterry.payload.response.UserDetailsResponse;
import com.lottery.looterry.payload.response.roleRespone;
import com.lottery.looterry.service.EmailService;
import com.lottery.looterry.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;
    @PostMapping("login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = userService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(jwtResponse);
    }



    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody UserRequest userRequest) {
        userService.register(userRequest);
        return ResponseEntity.status(HttpStatus.OK).body("Register successfully");
    }

    @GetMapping("getUserByEmail/{email}")
    public ResponseEntity<UserDetailsResponse> findUserByEmail(@PathVariable String email){
        Users user = userService.findByEmail(email);
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse(user);
        return ResponseEntity.status(HttpStatus.OK).body(userDetailsResponse);
    }

    @PostMapping("forgetPassword/{email}")
    public ResponseEntity<String> forgetPassword(@PathVariable String email){
        Users users = userService.findByEmail(email);
        if(users != null){
            emailService.SendEmailTo(email, users.getPassword());
            return ResponseEntity.status(HttpStatus.OK).body("Password was sent to your email");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong email");
        }
    }

    @PostMapping("changePassword")
    public ResponseEntity<HttpResponse> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        Boolean changePassword = userService.changePassword(changePasswordRequest);
        if(changePassword){
            return ResponseEntity.status(HttpStatus.OK).body(new HttpResponse(HttpStatus.OK.value(),
                    "Change password successfully",null));
        }
        else {
            return ResponseEntity.status(HttpStatus.OK).body(new HttpResponse(HttpStatus.OK.value(),
                    "Wrong old password or the new password and confirm password do not match",null));
        }
    }

    @PutMapping("update/{id}")
    public ResponseEntity<HttpResponse> update(@PathVariable("id") int id, @RequestBody UserInfoRequest userInfoRequest){
        try{
            Users user = userService.findById(userInfoRequest.getUserId());

            if(user == null){
                return ResponseEntity.status(HttpStatus.OK).body(new HttpResponse(HttpStatus.OK.value(),
                        "User not found",null));
            }
            userService.update(userInfoRequest.getUserId(), userInfoRequest);
            return ResponseEntity.status(HttpStatus.OK).body(new HttpResponse(HttpStatus.OK.value(),
                    "Update successfully",null));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HttpResponse(HttpStatus.BAD_REQUEST.value(),
                    "Update failed",null));
        }
    }

    @GetMapping ("authorization")
    public ResponseEntity<roleRespone> getRoleUset (){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users principal = (Users) authentication.getPrincipal();
        Users userLogin = userService.findById(principal.getId());
        roleRespone role = new roleRespone(userLogin.getEmail(),userLogin.getRole().getRoleName());
        return ResponseEntity.status(HttpStatus.OK).body(role);
    }

}
