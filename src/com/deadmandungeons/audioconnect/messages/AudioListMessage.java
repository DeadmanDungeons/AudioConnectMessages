package com.deadmandungeons.audioconnect.messages;

import com.deadmandungeons.connect.commons.Messenger.Message;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;

import java.lang.reflect.Type;
import java.util.Set;

@MessageType("audio-list")
public class AudioListMessage extends Message {

    public static final MessageCreator<AudioListMessage> CREATOR = new MessageCreator<AudioListMessage>(AudioListMessage.class) {

        @Override
        public AudioListMessage createInstance(Type type) {
            return new AudioListMessage(null, null);
        }
    };

    private final Set<String> audioIds;
    private final ListAction action;

    public AudioListMessage(Set<String> audioIds, ListAction action) {
        this.audioIds = audioIds;
        this.action = action;
    }


    public Set<String> getAudioIds() {
        return audioIds;
    }

    public ListAction getAction() {
        return action;
    }

    @Override
    public boolean isValid() {
        return audioIds != null && !audioIds.isEmpty() && action != null;
    }


    public enum ListAction {
        ADD,
        REMOVE
    }

}
