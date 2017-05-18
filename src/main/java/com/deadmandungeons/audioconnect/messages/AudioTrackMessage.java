package com.deadmandungeons.audioconnect.messages;

import com.deadmandungeons.connect.commons.Messenger.IdentifiableMessage;
import com.deadmandungeons.connect.commons.Messenger.InvalidDataException;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;

import java.lang.reflect.Type;
import java.util.UUID;

@MessageType("audio-track")
public class AudioTrackMessage extends IdentifiableMessage {

    public static final MessageCreator<AudioTrackMessage> CREATOR = new MessageCreator<AudioTrackMessage>(AudioTrackMessage.class) {

        @Override
        public AudioTrackMessage createInstance(Type type) {
            return builder(null, null).build();
        }
    };


    private final String trackId;
    private final boolean defaultTrack;
    private final boolean repeating;
    private final boolean random;
    private final boolean fading;

    public static Builder builder(UUID id, String name) {
        return new Builder(id, name);
    }

    public static class Builder {

        private final UUID id;
        private final String trackId;
        private boolean defaultTrack;
        private boolean repeating;
        private boolean random;
        private boolean fading;

        protected Builder(UUID id, String trackId) {
            this.id = id;
            this.trackId = trackId;
        }

        public Builder defaultTrack() {
            defaultTrack = true;
            return this;
        }

        public Builder repeating() {
            repeating = true;
            return this;
        }

        public Builder random() {
            random = true;
            return this;
        }

        public Builder fading() {
            fading = true;
            return this;
        }

        public AudioTrackMessage build() {
            return new AudioTrackMessage(this);
        }

    }

    protected AudioTrackMessage(Builder builder) {
        super(builder.id);
        trackId = builder.trackId;
        defaultTrack = builder.defaultTrack;
        repeating = builder.repeating;
        random = builder.random;
        fading = builder.fading;
    }

    public String getTrackId() {
        return trackId;
    }

    public boolean isDefaultTrack() {
        return defaultTrack;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public boolean isRandom() {
        return random;
    }

    public boolean isFading() {
        return fading;
    }

    @Override
    public void validate() throws InvalidDataException {
        super.validate();
        AudioMessage.validateIdentifier(trackId);
    }

}
