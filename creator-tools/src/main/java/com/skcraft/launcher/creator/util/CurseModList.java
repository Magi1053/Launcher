/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
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
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class CurseModList {

    @Getter
    private Map<String, ModEntry> mods = ImmutableMap.of();

    public void load(String version) throws IOException, InterruptedException {
        checkNotNull(version, "version");

        List<ModEntry> mods = HttpRequest.get(HttpRequest.url("https://staging-cursemeta.dries007.net/api/v3/direct/addon/search?gameId=432&sectionId=6&gameVersion=" + version))
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asJson(new TypeReference<List<ModEntry>>() {});

        Map<String, ModEntry> index = Maps.newHashMap();

        for (ModEntry entry : mods) {
            index.put(entry.getName(), entry);
        }

        this.mods = Collections.unmodifiableMap(index);
    }

    @Nullable
    public ModEntry get(String modName) {
        checkNotNull(modName, "modName");
        return mods.get(modName);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModEntry {

        @JsonProperty("id")
        private int id;
        @JsonProperty("name")
        private String name;
        @JsonProperty("slug")
        private String slug;

        @JsonProperty("primaryAuthorName")
        private String author;
        @JsonProperty("websiteUrl")
        private URL url;
        @JsonProperty("summary")
        private String summary;
        @JsonProperty("fullDescription")
        private String description;

        @JsonProperty("latestFiles")
        private List<GameFile> latestFiles = new ArrayList<>();

        // TODO a better way to do this
        public String getLatestVersion() {
            for (GameFile gamefile : getLatestFiles()) {
                if (gamefile.getReleaseType() == 1) {
                    return gamefile.getFileName();
                }
            }
            return null;
        }

        // TODO a smarter way to do this
        public String getLatestDevVersion() {
            for (GameFile gamefile : getLatestFiles()) {
                if (gamefile.getReleaseType() == 2) {
                    return gamefile.getFileName();
                }
            }
            for (GameFile gamefile : getLatestFiles()) {
                if (gamefile.getReleaseType() == 3) {
                    return gamefile.getFileName();
                }
            }
            return null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GameFile {
        @JsonProperty("id")
        private String id;
        @JsonProperty("fileName")
        private String fileName;
        @JsonProperty("fileDate")
        private Date fileDate;
        @JsonProperty("releaseType")
        private int releaseType;
        @JsonProperty("downloadUrl")
        private String url;

        @JsonProperty("dependencies")
        private List<Map<String, String>> dependencies = new ArrayList<>();

        @JsonProperty("isAvailable")
        private boolean isAvailable;

        @JsonProperty("packageFingerprint")
        private String fingerprint;

        @JsonProperty("gameVersion")
        private List<String> versions = new ArrayList<>();
    }

}
