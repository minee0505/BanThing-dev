package com.nathing.banthing.service;

import com.nathing.banthing.dto.response.MartResponse;
import com.nathing.banthing.repository.MartsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MartService {

    private final MartsRepository martsRepository;

    public List<MartResponse> findAllMarts() {
        return martsRepository.findAll().stream()
                .map(MartResponse::new)
                .collect(Collectors.toList());
    }
}