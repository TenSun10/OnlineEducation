package com.tenxi.service;

import com.tenxi.utils.RestBean;

public interface SignService {
    String sign();

    RestBean<Boolean> getTodayStatus();

    RestBean<Integer> getCount();
}
