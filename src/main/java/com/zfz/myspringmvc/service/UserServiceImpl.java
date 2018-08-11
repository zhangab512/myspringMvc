package com.zfz.myspringmvc.service;

import com.zfz.myspringmvc.annotation.Qualifier;
import com.zfz.myspringmvc.annotation.Service;
import com.zfz.myspringmvc.dao.UserDao;

/**
 * Created by zl on 2018-08-11.
 */
@Service("userServiceImpl")
public class UserServiceImpl implements UserService {

    @Qualifier("userDaoImpl")
    private UserDao userDao;

    public void insert() {
        System.out.println("UserServiceImpl.insert() start");
        userDao.insert();
        System.out.println("UserServiceImpl.insert() end");


    }
}
