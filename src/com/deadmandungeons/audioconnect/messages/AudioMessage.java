package com.deadmandungeons.audioconnect.messages;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.deadmandungeons.connect.commons.Messenger.Message;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;
import com.google.common.primitives.Ints;


@MessageType("audio")
public class AudioMessage extends Message {
	
	public static final MessageCreator<AudioMessage> CREATOR = new MessageCreator<AudioMessage>(AudioMessage.class) {
		
		@Override
		public AudioMessage createInstance(Type type) {
			return builder(null).build();
		}
	};
	
	private final Set<String> audioFiles;
	private final Range delayRange;
	private final boolean primary;
	
	
	public static Builder builder(UUID id) {
		return new Builder(id);
	}
	
	public static class Builder {
		
		private final UUID id;
		private Set<String> audioFiles;
		private Range delayRange;
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
		
		public Builder delay(int seconds) {
			return delayRange(seconds, seconds);
		}
		
		public Builder delayRange(int minSeconds, int maxSeconds) {
			return delayRange(new Range(minSeconds, maxSeconds));
		}
		
		public Builder delayRange(Range delayRange) {
			this.delayRange = delayRange;
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
		delayRange = builder.delayRange;
		primary = builder.primary;
	}
	
	
	/**
	 * @return the set of audio file source locations that should be played, or <code>null</code> if no audio should be played
	 */
	@Override
	public Set<String> getData() {
		return audioFiles;
	}
	
	/**
	 * @return a range of integers that specify the minimum and maximum amount of seconds that the delay between
	 * audio transitions should be. <code>null</code> will be returned if there should be no transition delay.
	 */
	public Range getDelayRange() {
		return delayRange;
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
		
		public Range(int min, int max) {
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
		public String toString() {
			return min + "-" + max;
		}
		
		public static Range parse(String rangeStr) {
			rangeStr = rangeStr.trim();
			Integer minMax = Ints.tryParse(rangeStr);
			if (minMax != null && minMax >= 0) {
				return new Range(minMax, minMax);
			} else {
				String[] rangeArray = rangeStr.split("-");
				if (rangeArray.length != 2) {
					return null;
				}
				Integer min = Ints.tryParse(rangeArray[0].trim());
				Integer max = Ints.tryParse(rangeArray[1].trim());
				if (min == null || max == null || min > max) {
					return null;
				}
				return new Range(min, max);
			}
		}
		
	}
	
	public static class AudioFile {
		
		private static final String FILE_NAME_CHARS = "a-zA-Z0-9-_"; // a-zA-Z0-9-_~!()+
		private static final String FILE_NAME_REGEX = "^[" + FILE_NAME_CHARS + "]+(\\.[" + FILE_NAME_CHARS + "]+)*(\\.[a-zA-z0-9]{1,8})$";
		private static final Pattern FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_REGEX);
		
		private final String location;
		
		private AudioFile(String location) {
			this.location = location;
		}
		
		/**
		 * @param audioFileName - The name of an audio file that was uploaded and stored on the AudioConnectWeb application
		 * @return a new AudioFile instance representing a textually valid audio file stored locally on the webserver.
		 * <code>null</code> will be returned if audioFileName is null or if it is invalid (See {@link #isValid(String)}).
		 */
		public static AudioFile create(String audioFileName) {
			if (audioFileName != null) {
				if (isValid(audioFileName)) {
					return new AudioFile(audioFileName);
				}
			}
			return null;
		}
		
		/**
		 * Valid file name characters are ASCII letters, digits, underscores, hyphens, and periods. A period character can
		 * only be used between any other valid character, and is needed for separating the file extension suffix from the
		 * name part. The file extension suffix is required, and must contain 1 to 8 ASCII letters or digits only.<br><br>
		 * <table border>
		 * <tr><td>Valid file name Examples</td><td>Invalid file name Examples</td></tr>
		 * <tr><td>filename.mp3</td><td>filename</td></tr>
		 * <tr><td>file-name.ogg</td><td>file-name.</td></tr>
		 * <tr><td>_file__name_.mp3</td><td>file_+_name.mp3</td></tr>
		 * <tr><td>file.name.wav</td><td>file..name.wav</td></tr>
		 * </table>
		 * @param audioFileName - The name of an audio file that was uploaded and stored on the AudioConnectWeb application
		 * @return <code>true</code> if the file name is valid and <code>false</code> if the file name is either improperly
		 * structured, or contains illegal characters.
		 */
		public static boolean isValid(String audioFileName) {
			return FILE_NAME_PATTERN.matcher(audioFileName).matches();
		}
		
	}
	
}
