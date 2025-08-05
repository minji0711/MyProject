package com.example.mealkit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import com.example.mealkit.domain.*;
import com.example.mealkit.repository.*;

@Service
@RequiredArgsConstructor
public class CancelReturnService {

    private final OrdersRepository ordersRepository;
    private final MpointRepository mpointRepository;
    private final McouponRepository mcouponRepository;
    private final CancelReturnRepository cancelReturnRepository;

    @Transactional
    public void processCancel(Long orderId, Member loginUser) {

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문정보가 존재하지 않습니다."));

        // 기존 본인주문, 배송완료 여부 체크 등 생략 (지금 이미 잘 작성된 부분)

        // 포인트 반환
        if (order.getPoint() > 0) {
            Mpoint mpoint = new Mpoint();
            mpoint.setMember(loginUser);
            mpoint.setPoint(order.getPoint());
            mpoint.setDescription("구매취소 포인트 반환");
            mpoint.setOrders(order);
            mpointRepository.save(mpoint);
        }

        // 쿠폰 반환
        if (order.getMcoupon() != null) {
            Mcoupon mcoupon = mcouponRepository.findById(order.getMcoupon().getId())
                .orElseThrow(() -> new RuntimeException("쿠폰정보가 존재하지 않습니다."));
            mcoupon.setUseYn("N");
        }

        // 취소내역 저장
        CancelReturn cancel = new CancelReturn();
        cancel.setOrders(order);
        cancel.setMember(loginUser);
        cancelReturnRepository.save(cancel);
    }
}
