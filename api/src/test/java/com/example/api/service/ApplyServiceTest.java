package com.example.api.service;

import com.example.api.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void 한번만응모() {
        // given when
        applyService.apply(1L);

        long count = couponRepository.count();

        // then
        assertThat(count).isEqualTo(1);
    }
    
    @Test
    void 여러명응모() throws InterruptedException {
        // given
        int threadCount = 1000;
        // 병렬 작업을 쉽게 할 수 있게 도와주는 자바 API
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
               try {
                   applyService.apply(userId);
               } finally {
                   latch.countDown();
               }
            });
        }

        latch.await();

        Thread.sleep(10000);

        long count = couponRepository.count();

        // then
        assertThat(count).isEqualTo(100);
    }

    @Test
    void 한명당_한개의쿠폰만_발급() throws InterruptedException {
        // given
        int threadCount = 1000;
        // 병렬 작업을 쉽게 할 수 있게 도와주는 자바 API
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    applyService.apply(1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Thread.sleep(10000);

        long count = couponRepository.count();

        // then
        assertThat(count).isEqualTo(1);
    }
}