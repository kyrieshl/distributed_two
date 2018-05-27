package com.litemall.distributed_two.service;

import org.linlinjava.litemall.db.domain.AdminToken;
import org.linlinjava.litemall.db.util.CharUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminTokenManager {

    @Autowired
    private RedisTemplate<String, Object> tokenRedisTemplate;

    public Integer getUserId(String token) {

        AdminToken userToken = (AdminToken) tokenRedisTemplate.opsForValue().get(token);
        if(userToken == null){
            return null;
        }

        if(userToken.getExpireTime().isBefore(LocalDateTime.now())){
            tokenRedisTemplate.delete(token);
            return null;
        }

        return userToken.getUserId();
    }


    public AdminToken generateToken(Integer id){
        AdminToken userToken = null;

//        userToken = idMap.get(id);
//        if(userToken != null) {
//            tokenMap.remove(userToken.getToken());
//            idMap.remove(id);
//        }

        String token = CharUtil.getRandomString(32);
        while (tokenRedisTemplate.hasKey(token)) {
            token = CharUtil.getRandomString(32);
        }

        LocalDateTime update = LocalDateTime.now();
        LocalDateTime expire = update.plusDays(1);

        userToken = new AdminToken();
        userToken.setToken(token);
        userToken.setUpdateTime(update);
        userToken.setExpireTime(expire);
        userToken.setUserId(id);
        tokenRedisTemplate.opsForValue().set(token, userToken);

        return userToken;
    }
}
