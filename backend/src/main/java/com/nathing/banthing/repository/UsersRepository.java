package com.nathing.banthing.repository;

import com.nathing.banthing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<User, Long> {

    /**
     * 주어진 OAuth 제공자에서 제공한 사용자 ID를 이용하여 사용자 정보를 검색합니다.
     *
     * @param providerId OAuth 제공자로부터 부여된 사용자 ID
     * @return 주어진 providerId에 해당하는 사용자를 가진 Optional 객체.
     *         사용자 정보가 없으면 비어 있는 Optional 반환.
     * @author 강관주
     * @since 2025.09.13
     */
    Optional<User> findByProviderId(String providerId);

    /**
     * 주어진 OAuth 제공자와 해당 제공자에서 부여된 사용자 ID를 기준으로 사용자 정보를 검색합니다.
     *
     * @param provider OAuth 제공자의 이름 (예: Google, Kakao 등)
     * @param providerId OAuth 제공자로부터 부여된 사용자 ID
     * @return 주어진 provider와 providerId에 해당하는 사용자 정보를 가진 Optional 객체.
     *         사용자 정보가 없으면 비어 있는 Optional 반환.
     * @author 강관주
     * @since 2025.09.13
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Optional<User> findByNickname(String identifier);
}
