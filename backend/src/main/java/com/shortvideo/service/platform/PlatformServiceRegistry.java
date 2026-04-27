package com.shortvideo.service.platform;

import com.shortvideo.enums.PlatformType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PlatformServiceRegistry {
    private final Map<PlatformType, VideoPlatformService> serviceMap = new EnumMap<>(PlatformType.class);

    public PlatformServiceRegistry(List<VideoPlatformService> services) {
        for (VideoPlatformService s : services) {
            serviceMap.put(s.platformType(), s);
        }
    }

    public VideoPlatformService get(PlatformType type) {
        VideoPlatformService s = serviceMap.get(type);
        if (s == null) {
            throw new IllegalArgumentException("platform not supported: " + type);
        }
        return s;
    }
}

