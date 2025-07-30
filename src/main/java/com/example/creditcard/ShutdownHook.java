/*package com.example.creditcard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.creditcard.dao.DatabaseInitializer;

import jakarta.annotation.PreDestroy;

@Component
public class ShutdownHook {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @PreDestroy
    public void onShutdown() {
        databaseInitializer.dropTables();
        logger.info("Application is shutting down. Dropped tables.");
    }
}*/
