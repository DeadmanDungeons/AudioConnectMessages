package com.deadmandungeons.audioconnect.messages;

import com.deadmandungeons.connect.commons.messenger.exceptions.IdentifierSyntaxException;
import com.deadmandungeons.connect.commons.messenger.exceptions.InvalidMessageException;
import com.deadmandungeons.connect.commons.messenger.messages.IdentifiableMessage;
import com.deadmandungeons.connect.commons.messenger.messages.MessageType;
import com.google.common.primitives.Ints;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


@MessageType("audio")
public class AudioMessage extends IdentifiableMessage {

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
                AudioConnectUtils.validateIdentifier(identifier);
            } catch (IdentifierSyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }

    }

    // Used by Messenger for deserialization
    @SuppressWarnings("unused")
    private AudioMessage() {
        this(builder(null));
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
    public void validate() throws InvalidMessageException {
        if (!validated) {
            if (trackId != null) {
                AudioConnectUtils.validateIdentifier(trackId);
            }
            if (audioIds != null) {
                for (String audioId : audioIds) {
                    AudioConnectUtils.validateIdentifier(audioId);
                }
            }
            validated = true;
        }
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

}
