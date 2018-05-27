package com.litemall.distributed_two.service;

import org.linlinjava.litemall.db.domain.UserToken;
import org.linlinjava.litemall.db.util.CharUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserTokenManager {

//    @Autowired
//    private static RedisTemplate<String, Object> tokenRedisTemplate;
//
//    @Autowired
//    private static RedisTemplate<String, Object> idRedisTemplate;

//    private static Map<String, UserToken> tokenMap = new HashMap<>();
//    private static Map<Integer, UserToken> idMap = new HashMap<>();
//    @Resource
//    private TokenRedis tokenRedis;

    @Autowired
    private RedisTemplate<String,Object> tokenRedisTemplate;

//    private static UserTokenManager factory;
//
//    @PostConstruct
//    public void init() {
//        factory = this;
//    }

    public  Integer getUserId(String token) {
        UserToken userToken = (UserToken) tokenRedisTemplate.opsForValue().get(token);
        if(userToken == null){
            return null;
        }

        if(userToken.getExpireTime().isBefore(LocalDateTime.now())){
            tokenRedisTemplate.delete(token);
            return null;
        }

        return userToken.getUserId();
    }


    public  UserToken generateToken(Integer id){
        UserToken userToken = null;
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

        userToken = new UserToken();
        userToken.setToken(token);
        userToken.setUpdateTime(update);
        userToken.setExpireTime(expire);
        userToken.setUserId(id);
        tokenRedisTemplate.opsForValue().set(token,userToken);

        return userToken;
    }
}
