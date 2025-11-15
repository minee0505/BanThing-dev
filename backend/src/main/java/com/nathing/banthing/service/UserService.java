package com.nathing.banthing.service;

import com.nathing.banthing.dto.response.UserInfoResponse;
import com.nathing.banthing.dto.response.UserResponse;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UsersRepository usersRepository;

    /**
     * 주어진 providerId를 이용하여 사용자의 정보를 조회하고 반환합니다.
     *
     * @param providerId 사용자를 식별하는 providerId
     * @return 조회된 사용자의 정보를 담고 있는 UserInfoResponse 객체
     * @throws BusinessException 사용자가 존재하지 않을 경우 예외를 발생시킵니다.
     *
     * @author - 강관주
     * @since - 2025-09-15
     */
    @Transactional(readOnly = true)
    public UserInfoResponse findMeByProviderId(String providerId) {
        // 사용자 정보 조회
        User user = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.from(user);
    }


    /**

     * @param providerId
     * @return 로그인된 유저 id
     *
     * @author 송민재
     * @since 2025-09-16
     */
    @Transactional(readOnly = true)
    public Long getUserIdByProviderId(String providerId) {
        User user = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getUserId();
    }

    /**
     * 주어진 providerId를 기반으로 사용자를 조회하여 동의 상태를 업데이트하고,
     * 업데이트된 사용자 정보를 반환합니다.
     *
     * 더티 체킹(Dirty Checking):
     * '@Transactional' 안에서 영속성 컨텍스트에 의해 관리되는 엔티티의 상태가 변경되면
     * 별도의 'save' 메서드 호출 없이도 JPA가 알아서 변경된 내용을 데이터베이스에 동기화(synchronize)해 줍니다.
     *
     * @param providerId 사용자를 식별하기 위한 providerId
     * @return 업데이트된 사용자 정보를 포함하는 UserResponse 객체
     * @throws BusinessException 사용자가 존재하지 않을 경우 예외를 발생시킵니다.
     *
     * @author 강관주
     * @since 2025-09-21
     */
    public UserResponse updateUserAgreement(String providerId) {
        // 사용자 정보 조회
        User user = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 동의 상태 업데이트 편의 메서드
        user.updateAgreement();

        return UserResponse.from(user);
    }
}
