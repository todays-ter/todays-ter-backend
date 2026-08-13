package com.umc.todayter.domain.notifications.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class NotificationSchedulingConfig {
}
