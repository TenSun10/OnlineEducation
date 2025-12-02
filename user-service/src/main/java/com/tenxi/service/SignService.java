package com.tenxi.service;

import com.tenxi.utils.RestBean;

public interface SignService {
    RestBean<String> sign();

    RestBean<Boolean> getTodayStatus();

    RestBean<Integer> getCount();
}
