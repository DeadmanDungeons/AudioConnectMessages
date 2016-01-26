package com.deadmandungeons.audioconnect.messages;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.deadmandungeons.connect.commons.ConnectUtils;
import com.deadmandungeons.connect.commons.Messenger.Message;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;


@MessageType("audio")
public class AudioMessage extends Message {
	
	public static final MessageCreator<AudioMessage> CREATOR = new MessageCreator<AudioMessage>(AudioMessage.class) {
		
		@Override
		public AudioMessage createInstance(Type type) {
			return builder(null).build();
		}
	};
	
	private final Set<String> audioFiles;
	private final Range transitionDelay;
	private final boolean primary;
	
	
	public static Builder builder(UUID id) {
		return new Builder(id);
	}
	
	public static class Builder {
		
		private final UUID id;
		private Set<String> audioFiles;
		private Range transitionDelay;
		private boolean primary;
		
		protected Builder(UUID id) {
			this.id = id;
		}
		
		
		public Builder audio(AudioFile audioFile) {
			if (audioFiles == null) {
				audioFiles = new HashSet<>();
			}
			audioFiles.add(audioFile.location);
			return this;
		}
		
		public Builder audio(AudioFile... audioFiles) {
			for (AudioFile audioFile : audioFiles) {
				audio(audioFile);
			}
			return this;
		}
		
		public Builder transitionDelay(int seconds) {
			transitionDelay = new Range(seconds, seconds);
			return this;
		}
		
		public Builder transitionDelay(int minSeconds, int maxSeconds) {
			transitionDelay = new Range(minSeconds, maxSeconds);
			return this;
		}
		
		public Builder primary() {
			primary = true;
			return this;
		}
		
		
		public AudioMessage build() {
			return new AudioMessage(this);
		}
		
	}
	
	protected AudioMessage(Builder builder) {
		super(builder.id);
		audioFiles = builder.audioFiles;
		transitionDelay = builder.transitionDelay;
		primary = builder.primary;
	}
	
	
	/**
	 * @return the audio file source location of the audio file to play, or null if no audio should be played
	 */
	@Override
	public Set<String> getData() {
		return audioFiles;
	}
	
	/**
	 * @return
	 */
	public Range getTransitionDelay() {
		return transitionDelay;
	}
	
	/**
	 * @return true if the target of this AudioMessage is for the primary audio track,
	 * and false if the target is for a complementary audio track
	 */
	public boolean isPrimary() {
		return primary;
	}
	
	
	@Override
	protected boolean isDataValid() {
		return true;
	}
	
	
	public static class Range {
		
		private final int min;
		private final int max;
		
		private Range(int min, int max) {
			this.min = min;
			this.max = max;
		}
		
		public int getMin() {
			return min;
		}
		
		public int getMax() {
			return max;
		}
		
	}
	
	public static class AudioFile {
		
		// Only accept secure sources
		private static final String[] EXTERNAL_URL_PROTOCOLS = { "https", "ftps" };
		private static final Pattern FILE_PATH_PATTERN = Pattern.compile("[/a-zA-Z0-9_-]+");
		private static final String FILE_NAME_CHARS = "a-zA-Z0-9-_"; // a-zA-Z0-9-_~!()+
		private static final String FILE_NAME_REGEX = "^[" + FILE_NAME_CHARS + "]+(\\.[" + FILE_NAME_CHARS + "]+)*(\\.[a-zA-z0-9]{1,8})$";
		private static final Pattern FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_REGEX);
		
		private final String location;
		
		private AudioFile(String location) {
			this.location = location;
		}
		
		
		/**
		 * @param audioFileUrl - The URL string locating a publicly accessible audio file using a secure protocol
		 * @return a new AudioFile instance representing a textually valid external web resource URL. <code>null</code
		 * will be returned if the audioFileUrl is not a valid URL with a secure protocol [https, ftps]
		 */
		public static AudioFile externalHost(String audioFileUrl) {
			URL url = ConnectUtils.parseUrl(audioFileUrl);
			if (url != null) {
				for (String allowedProtocol : EXTERNAL_URL_PROTOCOLS) {
					if (allowedProtocol.equalsIgnoreCase(url.getProtocol())) {
						return new AudioFile(url.toString());
					}
				}
			}
			return null;
		}
		
		/**
		 * @param audioFilePath - the file path locating an audio file that was uploaded and stored on the AudioConnect web-server endpoint
		 * @return a new AudioFile instance representing a textually valid local web resource file path. <code>null</code> will be returned if the
		 * path and/or file name is either not structured properly or contains invalid characters.
		 */
		public static AudioFile localHost(String audioFilePath) {
			if (audioFilePath != null) {
				File file = new File(audioFilePath);
				String fileName = file.getName();
				String path = file.getPath().substring(0, file.getPath().length() - fileName.length()).replaceAll("\\\\", "/");
				if (path.isEmpty() || FILE_PATH_PATTERN.matcher(path).matches()) {
					if (FILE_NAME_PATTERN.matcher(fileName).matches()) {
						return new AudioFile(audioFilePath);
					}
				}
			}
			return null;
		}
		
		/**
		 * @param audioFile - The location of either an external or local audio file
		 * @return a new AudioFile instance representing a textually valid web resource file
		 */
		public static AudioFile fromString(String audioFile) {
			AudioFile file = null;
			if (audioFile != null) {
				file = externalHost(audioFile);
				if (file == null) {
					file = localHost(audioFile);
				}
			}
			
			return file;
		}
		
	}
	
}
