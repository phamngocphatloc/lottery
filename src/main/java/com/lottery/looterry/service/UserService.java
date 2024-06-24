package com.lottery.looterry.service;

import com.lottery.looterry.entity.Users;
import com.lottery.looterry.payload.request.ChangePasswordRequest;
import com.lottery.looterry.payload.request.LoginRequest;
import com.lottery.looterry.payload.request.UserInfoRequest;
import com.lottery.looterry.payload.request.UserRequest;
import com.lottery.looterry.payload.response.JwtResponse;

public interface UserService {
    JwtResponse login(LoginRequest loginRequest);

    void register(UserRequest userRequest);

    Users findByEmail(String email);

    Users findById (int id);
    Boolean changePassword(ChangePasswordRequest changePasswordRequest);

    void update(int id, UserInfoRequest userInfoRequest);
}
