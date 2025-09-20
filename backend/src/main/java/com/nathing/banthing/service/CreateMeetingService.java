package com.nathing.banthing.service;

import com.nathing.banthing.dto.request.MeetingCreateRequest;
import com.nathing.banthing.entity.Mart;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.MeetingParticipant;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.MartsRepository;
import com.nathing.banthing.repository.MeetingParticipantsRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CreateMeetingService {

    private final MartsRepository martsRepository;
    private final UsersRepository usersRepository;
    private final MeetingsRepository meetingsRepository;
    private final MeetingParticipantsRepository meetingParticipantsRepository;
    private static final String DEFAULT_THUMBNAIL_IMAGE_URL = "/images/meeting-default-img.svg";

    // application.yml의 값을 주입받기 위한 어노테이션
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-url}")
    private String uploadUrl;

    /**
     * 모임 생성 비즈니스 로직 (파일 업로드 기능 추가)
     * @param request 모임 생성 데이터
     * @param imageFile 업로드된 이미지 파일 (선택)
     * @param providerId 사용자 ID
     * @return 생성된 Meeting 엔티티
     */
    public Meeting createMeeting(MeetingCreateRequest request, MultipartFile imageFile, String providerId) {

        User hostUser = usersRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Mart mart = martsRepository.findById(request.getMartId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MART_NOT_FOUND));

        String thumbnailUrl;

        // 이미지 파일이 업로드되었다면, 파일을 저장하고 URL을 덮어씁니다.
        if (imageFile != null && !imageFile.isEmpty()) {
            thumbnailUrl = storeFile(imageFile);
        }else {
            thumbnailUrl = DEFAULT_THUMBNAIL_IMAGE_URL;
        }

        Meeting newMeeting = Meeting.builder()
                .hostUser(hostUser)
                .mart(mart)
                .title(request.getTitle())
                .description(request.getDescription())
                .meetingDate(request.getMeetingDate())
                .maxParticipants(5)
                .thumbnailImageUrl(thumbnailUrl)
                .currentParticipants(1)
                .status(Meeting.MeetingStatus.RECRUITING)
                .build();

        Meeting savedMeeting = meetingsRepository.save(newMeeting);

        MeetingParticipant hostParticipant = MeetingParticipant.builder()
                .meeting(savedMeeting)
                .user(hostUser)
                .participantType(MeetingParticipant.ParticipantType.HOST)
                .applicationStatus(MeetingParticipant.ApplicationStatus.APPROVED)
                .build();
        meetingParticipantsRepository.save(hostParticipant);

        log.info("새로운 모임이 생성되었습니다. meetingId: {}, hostId: {}", savedMeeting.getMeetingId(), hostUser.getUserId());
        return savedMeeting;
    }

    /**
     * MultipartFile을 서버에 저장하고 접근 가능한 URL을 반환하는 헬퍼 메서드
     */
    private String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String storeFilename = UUID.randomUUID().toString() + "." + originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            Files.copy(file.getInputStream(), uploadPath.resolve(storeFilename));

            return uploadUrl + storeFilename;

        } catch (IOException e) {
            log.error("파일 저장에 실패했습니다.", e);
            throw new BusinessException("파일 저장에 실패했습니다.");
        }
    }
}