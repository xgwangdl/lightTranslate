package com.light.translate.communicate.dto;

import com.light.translate.communicate.data.FollowReadSentence;
import com.light.translate.communicate.utils.ListeningQuestionGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowReadSentenceDTO {
    private FollowReadSentence followReadSentence;
    private ListeningQuestionGenerator.ListeningQuestion listeningQuestion;
}
