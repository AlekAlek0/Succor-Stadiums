package net.alek.succorstadiums.network.arena;

import java.util.ArrayList;
import java.util.List;

// Client-side copy of the server's advancement IDs, filled by AdvancementListPayload and read by RewardScreen
public class AdvancementIdCache {

    private static volatile List<String> ids = new ArrayList<>();

    public static void set(List<String> newIds) {
        ids = new ArrayList<>(newIds);
    }

    public static List<String> get() {
        return ids;
    }
}