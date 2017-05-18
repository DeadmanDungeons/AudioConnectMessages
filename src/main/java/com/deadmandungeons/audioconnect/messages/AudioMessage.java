package com.deadmandungeons.audioconnect.messages;

import com.deadmandungeons.connect.commons.Messenger.IdentifiableMessage;
import com.deadmandungeons.connect.commons.Messenger.InvalidDataException;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;
import com.google.common.primitives.Ints;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


@MessageType("audio")
public class AudioMessage extends IdentifiableMessage {

    public static final MessageCreator<AudioMessage> CREATOR = new MessageCreator<AudioMessage>(AudioMessage.class) {

        @Override
        public AudioMessage createInstance(Type type) {
            return new AudioMessage(new Builder(null));
        }
    };

    private final Set<String> audioIds;
    private final String trackId;
    private final Range delayRange;

    private transient boolean validated;

    public static Builder builder(UUID id) {
        return new Builder(id);
    }

    public static class Builder {

        private final UUID id;
        private Set<String> audioIds;
        private String trackId;
        private Range delayRange;

        protected Builder(UUID id) {
            this.id = id;
        }


        public Builder audio(String audioId) {
            validateIdentifier(audioId);

            if (audioIds == null) {
                audioIds = new HashSet<>();
            }
            audioIds.add(audioId);
            return this;
        }

        public Builder audio(String... audioIds) {
            for (String audioId : audioIds) {
                audio(audioId);
            }
            return this;
        }

        public Builder track(String trackId) {
            validateIdentifier(trackId);
            this.trackId = trackId;
            return this;
        }

        public Builder delay(int seconds) {
            return delayRange(seconds, seconds);
        }

        public Builder delayRange(int minSeconds, int maxSeconds) {
            return delayRange(new Range(minSeconds, maxSeconds));
        }

        public Builder delayRange(Range delayRange) {
            this.delayRange = (delayRange.min != 0 || delayRange.max != 0 ? delayRange : null);
            return this;
        }


        public AudioMessage build() {
            AudioMessage audioMessage = new AudioMessage(this);
            audioMessage.validated = true;
            return audioMessage;
        }

        private static void validateIdentifier(String identifier) {
            try {
                AudioMessage.validateIdentifier(identifier);
            } catch (IdentifierSyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }

    }

    protected AudioMessage(Builder builder) {
        super(builder.id);
        audioIds = builder.audioIds;
        trackId = builder.trackId;
        delayRange = builder.delayRange;
    }


    /**
     * Identifiers of the audio source.
     * @return the set of identifiers for the audio that should be played, or <code>null</code> if no audio should be played
     */
    public Set<String> getAudioIds() {
        return audioIds;
    }

    /**
     * Identifies the audio destination.<br><br>
     * <b>note:</b> An audio track with this ID needs to be previously defined by an {@link AudioTrackMessage}
     * @return the identifier of the audio track to use as the destination for the audio source,
     * or <code>null</code> if the default audio track should be used
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * @return an integer range that specify the minimum and maximum amount of seconds that the delay between
     * audio transitions should be. <code>null</code> will be returned if there should be no transition delay.
     */
    public Range getDelayRange() {
        return delayRange;
    }

    @Override
    public void validate() throws InvalidDataException {
        if (!validated) {
            if (trackId != null) {
                validateIdentifier(trackId);
            }
            if (audioIds != null) {
                for (String audioId : audioIds) {
                    validateIdentifier(audioId);
                }
            }
            validated = true;
        }
    }

    /**
     * Validates that the given identifier is not empty, between 3 to 50 characters,
     * and contains only ASCII alpha-numeric or dash characters
     * @param identifier the identifier to validate
     * @throws IdentifierSyntaxException if the given identifier has invalid syntax
     */
    public static void validateIdentifier(String identifier) throws IdentifierSyntaxException {
        if (identifier == null || identifier.isEmpty()) {
            throw new IdentifierSyntaxException(SyntaxError.EMPTY);
        }
        if (identifier.length() < 3) {
            throw new IdentifierSyntaxException(SyntaxError.MIN_LENGTH, 3);
        }
        if (identifier.length() > 50) {
            throw new IdentifierSyntaxException(SyntaxError.MAX_LENGTH, 50);
        }
        for (int i = 0; i < identifier.length(); i++) {
            char character = identifier.charAt(i);
            if (!isAsciiAlphaNumeric(character) && character != '-' && character != '_') {
                throw new IdentifierSyntaxException(SyntaxError.INVALID_CHAR, character);
            }
        }
    }

    private static boolean isAsciiAlphaNumeric(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }


    public static class Range {

        private final int min;
        private final int max;

        public Range(int min, int max) {
            if (min > max) {
                throw new IllegalArgumentException("min cannot be greater than max");
            }
            if (min < 0 || max < 0) {
                throw new IllegalArgumentException("min or max cannot be less than 0");
            }

            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public int hashCode() {
            return Objects.hash(min, max);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Range)) {
                return false;
            }
            Range other = (Range) obj;
            return min == other.min && max == other.max;
        }

        @Override
        public String toString() {
            return min + "-" + max;
        }

        public static Range parse(String rangeStr) {
            String[] rangeArray = rangeStr.trim().split("-", -1);
            if (rangeArray.length == 1) {
                Integer minMax = Ints.tryParse(rangeArray[0].trim());
                if (minMax != null && minMax >= 0) {
                    return new Range(minMax, minMax);
                }
            } else if (rangeArray.length == 2) {
                Integer min = Ints.tryParse(rangeArray[0].trim());
                Integer max = Ints.tryParse(rangeArray[1].trim());
                if (min != null && max != null && min <= max) {
                    return new Range(min, max);
                }
            }
            return null;
        }

    }

    public static class IdentifierSyntaxException extends InvalidDataException {

        private static final long serialVersionUID = 8567778573045685621L;

        private final SyntaxError error;
        private final Object[] errorVars;

        public IdentifierSyntaxException(SyntaxError error, Object... errorVars) {
            super(String.format(error.message, errorVars));
            this.error = error;
            this.errorVars = errorVars;
        }

        public SyntaxError getError() {
            return error;
        }

        public Object[] getErrorVars() {
            return Arrays.copyOf(errorVars, errorVars.length);
        }

    }

    public enum SyntaxError {
        EMPTY("Identifier cannot be empty"),
        MIN_LENGTH("Identifier cannot be less than %s characters"),
        MAX_LENGTH("Identifier cannot be greater than %s characters"),
        INVALID_CHAR("Identifier contains invalid character '%s'");

        private final String message;

        SyntaxError(String message) {
            this.message = message;
        }
    }

}
