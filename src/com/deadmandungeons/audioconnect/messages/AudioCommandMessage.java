package com.deadmandungeons.audioconnect.messages;

import com.deadmandungeons.connect.commons.Messenger.IdentifiableMessage;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * A message type that is used to send an {@link AudioCommand} for the subject identified by {@link #getId()}
 * @author Jon
 */
@MessageType("audio-command")
public class AudioCommandMessage extends IdentifiableMessage {

    public static final MessageCreator<AudioCommandMessage> CREATOR = new MessageCreator<AudioCommandMessage>(AudioCommandMessage.class) {

        @Override
        public AudioCommandMessage createInstance(Type type) {
            return new AudioCommandMessage(null, null);
        }
    };

    private final AudioCommand command;

    public AudioCommandMessage(UUID id, AudioCommand command) {
        super(id);
        this.command = command;
    }

    public AudioCommand getCommand() {
        return command;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && command != null;
    }

    public enum AudioCommand {
        @SerializedName("mute")
        MUTE,
        @SerializedName("unmute")
        UNMUTE
        // more may be added
    }

}
