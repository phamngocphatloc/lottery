package com.lottery.looterry.service;

import com.lottery.looterry.entity.RefreshToken;
import com.lottery.looterry.entity.Users;


public interface RefreshTokenService {

    RefreshToken save(String token, Users user);

    RefreshToken processRefreshToken(String email);

    RefreshToken handleGetNewRefreshToken(String token);

    void provokeToken(Integer token);
}