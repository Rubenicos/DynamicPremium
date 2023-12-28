package me.gatogamer.dynamicpremium.commons.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.function.Function;

public class TinyWebhook {

    private final String url;
    private final String defaultUsername;
    private final String defaultAvatarUrl;
    private final boolean defaultTts;

    public TinyWebhook(@Nullable String url, @Nullable String defaultUsername, @Nullable String defaultAvatarUrl, boolean defaultTts) {
        this.url = url;
        this.defaultUsername = defaultUsername;
        this.defaultAvatarUrl = defaultAvatarUrl;
        this.defaultTts = defaultTts;
    }

    @NotNull
    public Message msg(@NotNull String content) {
        return new Message(content);
    }

    public void send(@NotNull TinyWebhook.Message message) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        joiner.add("\"content\": \"" + message.getContent() + '"');
        if (message.getUsername() != null) {
            joiner.add("\"username\": \"" + message.getUsername() + '"');
        }
        if (message.getAvatarUrl() != null) {
            joiner.add("\"avatar_url\": \"" + message.getAvatarUrl() + '"');
        }
        joiner.add("\"tts\": \"" + message.isTts() + '"');

        try {
            URL url = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "ProxyBroadcast-Plugin");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            OutputStream stream = connection.getOutputStream();
            stream.write(joiner.toString().getBytes(StandardCharsets.UTF_8));
            stream.flush();
            stream.close();

            connection.getInputStream().close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class Message {
        private final String content;
        private String username;
        private String avatarUrl;
        private Boolean tts;

        public Message(@NotNull String content) {
            this.content = content;
        }

        @NotNull
        public Message username(@Nullable String username) {
            this.username = username;
            return this;
        }

        @NotNull
        public Message avatarUrl(@Nullable String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        @NotNull
        public Message tts(@Nullable Boolean tts) {
            this.tts = tts;
            return this;
        }

        @NotNull
        public String getContent() {
            return content;
        }

        @Nullable
        public String getUsername() {
            return username != null ? username : defaultUsername;
        }

        @Nullable
        public String getAvatarUrl() {
            return avatarUrl != null ? avatarUrl : defaultAvatarUrl;
        }

        public boolean isTts() {
            return tts != null ? tts : defaultTts;
        }

        public void send() {
            TinyWebhook.this.send(this);
        }

        public void send(@NotNull Function<String, String> parser) {
            TinyWebhook.this.send(new Message(parser.apply(content)).username(username).avatarUrl(avatarUrl).tts(tts));
        }
    }
}