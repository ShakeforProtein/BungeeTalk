package me.shakeforprotein.bungeetalk.Enums;

import java.util.LinkedHashMap;
import java.util.Map;

public enum ProtocolVersion {
    MINECRAFT_1_16_1(736),
    MINECRAFT_1_16(735),
    MINECRAFT_1_15_2(578),
    MINECRAFT_1_15_1(575),
    MINECRAFT_1_15(573),
    MINECRAFT_1_14_4(498),
    MINECRAFT_1_14_3(490),
    MINECRAFT_1_14_2(485),
    MINECRAFT_1_14_1(480),
    MINECRAFT_1_14(477),
    MINECRAFT_1_13_2(404),
    MINECRAFT_1_13_1(401),
    MINECRAFT_1_13(393),
    MINECRAFT_1_12_2(340),
    MINECRAFT_1_12_1(338),
    MINECRAFT_1_12(335),
    MINECRAFT_1_11_2(316),
    MINECRAFT_1_11_1(316),
    MINECRAFT_1_11(315),
    MINECRAFT_1_10(210),
    MINECRAFT_1_9_4(110),
    MINECRAFT_1_9_2(109),
    MINECRAFT_1_9_1(108),
    MINECRAFT_1_9(107),
    MINECRAFT_1_8(47),
    MINECRAFT_1_7_6(5),
    MINECRAFT_1_7_2(4),
    UNKNOWN(0);

    private final int number;
    private static Map<Integer, ProtocolVersion> numbers;

    static {
        numbers = new LinkedHashMap<>();
        for(ProtocolVersion version : values()) {
            numbers.put(version.number, version);
        }
    }

    ProtocolVersion(int versionNumber) {
        this.number = versionNumber;
    }

    public static ProtocolVersion getVersion(int versionNumber) {
        ProtocolVersion protocolVersion = numbers.get(versionNumber);
        if (protocolVersion != null) {
            return protocolVersion;
        }
        return UNKNOWN;
    }

    public static ProtocolVersion matchVersion(int versionNumber) {
        ProtocolVersion protocolVersion = getVersion(versionNumber);
        if (protocolVersion == UNKNOWN) {
            for (ProtocolVersion version : values()) {
                if (version.toInt() <= versionNumber) {
                    return version;
                }
            }
        }
        return protocolVersion;
    }

    public int toInt() {
        return number;
    }

    public String toString() {
        return name().replace("MINECRAFT_", "").replace("_", ".");
    }
}
