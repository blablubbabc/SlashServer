package com.minecraftdimensions.slashserver.configlibrary;

import java.util.Map;

public interface ConfigurationSerializable {
    public Map<String, Object> serialize();
}