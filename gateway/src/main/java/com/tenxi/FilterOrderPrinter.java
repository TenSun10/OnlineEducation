package com.tenxi;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;

import java.util.List;

@Log
@Component
public class FilterOrderPrinter implements ApplicationRunner {
    @Autowired
    private List<GlobalFilter> globalFilters;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        globalFilters.forEach(filter -> log.info("Loaded GlobalFilter: " + filter.getClass().getName()));
    }
}
