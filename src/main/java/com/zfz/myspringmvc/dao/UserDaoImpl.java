package com.zfz.myspringmvc.dao;

import com.zfz.myspringmvc.annotation.Repository;

/**
 * Created by zl on 2018-08-11.
 */
@Repository("userDaoImpl")
public class UserDaoImpl implements UserDao {

    public void insert(){
        System.out.println("execute UserdaoImpl.insert()");
    }
}
