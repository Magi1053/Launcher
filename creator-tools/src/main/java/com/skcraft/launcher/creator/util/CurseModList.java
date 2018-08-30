/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Data;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class CurseModList {

    @Getter
    private Map<String, ModEntry> mods = new HashMap<>();

    public void load(String version) throws IOException, InterruptedException {
        checkNotNull(version, "version");

        CurseJSON mods = HttpRequest.get(HttpRequest.url("https://clientupdate-v6.cursecdn.com/feed/addons/432/v10/complete.json.bz2")) // TODO don't hardcode the url
                .execute()
                .expectResponseCode(200)
                .returnBZip2Content()
                .asJson(new TypeReference<CurseJSON>() {});

        Map<String, ModEntry> index = Maps.newHashMap();

        for (ModEntry entry : mods.getModEntries()) {
            index.put(entry.getName(), entry);
        }

        this.mods = Collections.unmodifiableMap(index);
    }

    @Nullable
    public ModEntry get(String modId) {
        checkNotNull(modId, "modId");
        return mods.get(modId);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurseJSON {
        @JsonProperty("timestamp")
        private long timestamp;

        @JsonProperty("data")
        private List<ModEntry> modEntries = new ArrayList<ModEntry>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModEntry {

        @JsonProperty("Id")
        private String id;
        @JsonProperty("Name")
        private String name;
        @JsonProperty("PrimaryAuthorName")
        private String author;
        @JsonProperty("WebSiteURL")
        private URL url;
        @JsonProperty("Summary")
        private String summary;

        @JsonProperty("LatestFiles")
        private List<GameFile> latestFiles = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GameFile {
        private String id;
        @JsonProperty("FileName")
        private String fileName;
        @JsonProperty("FileDate")
        private Date fileDate;
        @JsonProperty("ReleaseType")
        private int releaseType;
        @JsonProperty("DownloadURL")
        private String url;

        @JsonProperty("Dependencies")
        private List<Map<String, String>> dependencies = new ArrayList<>();

        @JsonProperty("IsAvailable")
        private boolean isAvailable;

        @JsonProperty("PackageFingerprint")
        private String fingerprint;

        @JsonProperty("GameVersion")
        private List<String> versions = new ArrayList<>();
    }

}
