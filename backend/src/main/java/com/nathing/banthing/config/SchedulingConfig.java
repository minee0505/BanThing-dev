package com.nathing.banthing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 기능을 활성화시키기 위한 Spring 설정 클래스입니다.
 *
 * 이 클래스는 애플리케이션에서 스케줄링 작업을 수행할 수 있도록 설정을 활성화합니다.
 * 또한, @EnableScheduling 어노테이션을 사용하여 Spring의 스케줄링 메커니즘을 설정합니다.
 *
 * 주요 역할:
 * - 일정 작업(task)을 정의하고 주기적으로 실행할 수 있는 환경을 설정합니다.
 * - 스프링 애플리케이션에서 @Scheduled 어노테이션을 사용하여 실행 가능한 메서드를 작성할 수 있습니다.
 *
 * 사용된 어노테이션:
 * - @Configuration: Spring의 설정 파일임을 나타내며, Bean을 정의하거나 설정을 담당하는 클래스임을 표시합니다.
 * - @EnableScheduling: 애플리케이션에서 스케줄링 기능을 활성화합니다.
 *
 * @author 고동현
 * @since 2025-09-15
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}