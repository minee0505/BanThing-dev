package com.nathing.banthing.controller;

import com.nathing.banthing.service.FeedbackService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 @author 송민재
 @since 25. 9. 15. */
@RestController
@RequestMapping("/feedback")
public class FeedbackConrtoller {
    private FeedbackService feedbackService;
}
