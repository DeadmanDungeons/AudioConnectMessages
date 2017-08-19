package com.deadmandungeons.audioconnect.messages;

import com.deadmandungeons.connect.commons.messenger.exceptions.InvalidMessageException;
import com.deadmandungeons.connect.commons.messenger.messages.IdentifiableMessage;
import com.deadmandungeons.connect.commons.messenger.messages.MessageType;

import java.util.UUID;

/**
 * A message type that is used to send an {@link AudioCommand} for the subject identified by {@link #getId()}
 * @author Jon
 */
@MessageType("audio-command")
public class AudioCommandMessage extends IdentifiableMessage {

    private final AudioCommand command;

    public AudioCommandMessage(UUID id, AudioCommand command) {
        super(id);
        this.command = command;
    }

    public AudioCommand getCommand() {
        return command;
    }

    @Override
    public void validate() throws InvalidMessageException {
        super.validate();
        if (command == null) {
            throw new InvalidMessageException("command cannot be null");
        }
    }

    public enum AudioCommand {
        MUTE,
        UNMUTE
        // more may be added
    }

}
