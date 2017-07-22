package com.deadmandungeons.audioconnect.messages;

import com.deadmandungeons.connect.commons.Messenger.InvalidDataException;
import com.deadmandungeons.connect.commons.Messenger.Message;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@MessageType("audio-list")
public class AudioListMessage extends Message {

    public static final MessageCreator<AudioListMessage> CREATOR = new MessageCreator<AudioListMessage>(AudioListMessage.class) {

        @Override
        public AudioListMessage createInstance(Type type) {
            return new AudioListMessage(null, (Set<String>) null);
        }
    };

    private final ListAction action;
    private final Set<String> audioIds;

    public AudioListMessage(ListAction action, String... audioIds) {
        this.action = action;
        this.audioIds = new LinkedHashSet<>();
        Collections.addAll(this.audioIds, audioIds);
    }

    public AudioListMessage(ListAction action, Set<String> audioIds) {
        this.action = action;
        this.audioIds = audioIds;
    }

    /**
     * @return a Set of the audio IDs to perform the {@link ListAction} with
     */
    public Set<String> getAudioIds() {
        return audioIds;
    }

    /**
     * @return the {@link ListAction} to perform using the audio IDs in {@link #getAudioIds()}
     */
    public ListAction getAction() {
        return action;
    }

    @Override
    public void validate() throws InvalidDataException {
        if (audioIds == null) {
            throw new InvalidDataException("audioIds cannot be null");
        }
        if (action == null) {
            throw new InvalidDataException("action cannot be null");
        }
        if (audioIds.size() < action.min) {
            throw new InvalidDataException("audioIds size cannot be less than " + action.min + " for action " + action);
        }
        if (audioIds.size() > action.max) {
            throw new InvalidDataException("audioIds size cannot be greater than " + action.max + " for action " + action);
        }
    }


    /**
     * <p>An action describing a modification to the underlying list of known audio IDs
     * <li>{@link #ADD}
     * <li>{@link #REMOVE}
     * <li>{@link #DELETE}
     * <li>{@link #REPLACE}
     */
    public enum ListAction {
        /**
         * Add all of the audio IDs to the list.
         * <p>{@link #getAudioIds()} contains at least 1
         */
        ADD(1, Integer.MAX_VALUE),
        /**
         * Remove all of the audio IDs from the list
         * <p>{@link #getAudioIds()} contains at least 1
         */
        REMOVE(1, Integer.MAX_VALUE),
        /**
         * <li>{@link #REMOVE} all of the audio IDs from the list
         * <li>Delete all occurrences of the audio IDs
         * <p>{@link #getAudioIds()} contains at least 1
         */
        DELETE(1, Integer.MAX_VALUE),
        /**
         * <li>{@link #REMOVE} the first audio ID from the list
         * <li>{@link #ADD} the second audio ID to the list
         * <li>Replace all occurrences of the first audio ID with the second audio ID
         * <p>{@link #getAudioIds()} contains only 2
         */
        REPLACE(2, 2);

        private final int min;
        private final int max;

        ListAction(int min, int max) {
            this.min = min;
            this.max = max;
        }

    }

}
