package com.example.filoangler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishSectionedData {

    // Organize fish into sections (A, B, C, ...)
    public static Map<String, List<String>> getSectionedFishList(List<String> fishList) {
        Map<String, List<String>> sectionedFishMap = new HashMap<>();

        for (String fish : fishList) {
            String firstLetter = fish.substring(0, 1).toUpperCase();

            if (!sectionedFishMap.containsKey(firstLetter)) {
                sectionedFishMap.put(firstLetter, new ArrayList<>());
            }
            sectionedFishMap.get(firstLetter).add(fish);
        }

        return sectionedFishMap;
    }
}
