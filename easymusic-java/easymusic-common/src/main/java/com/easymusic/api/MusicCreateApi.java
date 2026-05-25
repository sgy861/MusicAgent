package com.easymusic.api;

import com.easymusic.entity.dto.MusicCreationResultDTO;

import java.util.ArrayList;
import java.util.List;

public interface MusicCreateApi {
    default List<String> createMusic(String model, String prompt, String lyrics) {
        return new ArrayList<>();
    }

    default MusicCreationResultDTO musicQuery(String itemId) {
        return null;
    }

    default List<String> createPureMusic(String model, String prompt) {
        return new ArrayList<>();
    }

    default MusicCreationResultDTO pureMusicQuery(String itemId) {
        return null;
    }

    default MusicCreationResultDTO createMusicNotify(Integer musicType, String responseBody) {
        return null;
    }

}
