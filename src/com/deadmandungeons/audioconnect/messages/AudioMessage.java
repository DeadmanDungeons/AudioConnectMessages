package com.deadmandungeons.audioconnect.messages;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.deadmandungeons.connect.commons.Messenger.IdentifiableMessage;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;
import com.deadmandungeons.connect.commons.Result;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;


@MessageType("audio")
public class AudioMessage extends IdentifiableMessage {
	
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
			this.delayRange = (delayRange.min != 0 || delayRange.max != 0 ? delayRange : null);
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
	
	public static class AudioFile {
		
		private static final String FILE_NAME_SPECIAL_CHARS = "-_()[]!~+ ";
		private static final String FILE_PATH_SPECIAL_CHARS = ".-_~!$&'()*+,;=:@";
		
		private final String location;
		
		private AudioFile(String location) {
			this.location = location;
		}
		
		public String getLocation() {
			return location;
		}
		
		@Override
		public int hashCode() {
			return location.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AudioFile)) {
				return false;
			}
			return location.equals(((AudioFile) obj).location);
		}
		
		
		public static final Result<AudioFile> fromFilePath(String filePath) {
			String fileName;
			String path = (filePath != null ? filePath.trim() : "");
			String[] pathParts = path.split("/", -1);
			if (pathParts.length > 1) {
				Set<Character> invalidChars = new HashSet<>();
				for (int i = 0; i < pathParts.length - 1; i++) {
					String pathPart = pathParts[i];
					if (pathPart.isEmpty()) {
						if (i > 0) {
							return new Result<>("File path cannot have adjacent '/' (slash) characters");
						}
					} else {
						for (int n = 0; n < pathPart.length(); n++) {
							char currentChar = pathPart.charAt(n);
							if (!isAsciiAlphaNumeric(currentChar) && FILE_PATH_SPECIAL_CHARS.indexOf(currentChar) < 0) {
								invalidChars.add(currentChar);
							}
						}
					}
				}
				if (!invalidChars.isEmpty()) {
					String characters = Joiner.on("', '").join(invalidChars);
					return new Result<>("File path contains invalid characters '" + characters + "'");
				}
				fileName = pathParts[pathParts.length - 1];
			} else {
				fileName = path;
			}
			
			String fileNameError = validateFileName(fileName);
			if (fileNameError != null) {
				return new Result<>(fileNameError);
			}
			return new Result<>(new AudioFile(path));
		}
		
		public static final Result<AudioFile> fromFileName(String fileName) {
			String name = (fileName != null ? fileName.trim() : "");
			String fileNameError = validateFileName(name);
			if (fileNameError != null) {
				return new Result<>(fileNameError);
			}
			return new Result<>(new AudioFile(name));
		}
		
		private static final String validateFileName(String fileName) {
			if (fileName.isEmpty()) {
				return "File name cannot be empty, or blank";
			}
			int lastDotIndex = fileName.lastIndexOf('.');
			if (lastDotIndex <= 0 || fileName.length() - 1 == lastDotIndex) {
				return "File name must contain file extension";
			}
			if ((fileName.length() - 1) - lastDotIndex > 8) {
				return "File extension cannot be more than 8 characters";
			}
			
			char previousChar = 0;
			Set<Character> invalidChars = new HashSet<>();
			for (int i = 0; i < fileName.length(); i++) {
				char currentChar = fileName.charAt(i);
				if (currentChar == '.') {
					if (previousChar == '.') {
						return "File name cannot have adjacent '.' (dot) characters";
					}
					if (i == 0) {
						return "File name cannot start with '.' (dot) characters";
					}
				} else {
					boolean validChar;
					if (i > lastDotIndex) {
						if (i - 1 == lastDotIndex && !invalidChars.isEmpty()) {
							String characters = Joiner.on("', '").join(invalidChars);
							return "File name contains invalid characters '" + characters + "'";
						}
						validChar = isAsciiAlphaNumeric(currentChar);
					} else {
						validChar = isAsciiAlphaNumeric(currentChar) || FILE_NAME_SPECIAL_CHARS.indexOf(currentChar) >= 0;
					}
					if (!validChar) {
						invalidChars.add(currentChar);
					}
				}
				previousChar = currentChar;
			}
			if (!invalidChars.isEmpty()) {
				String characters = Joiner.on("', '").join(invalidChars);
				return "File extension contains invalid characters '" + characters + "'";
			}
			return null;
		}
		
		private static boolean isAsciiAlphaNumeric(char character) {
			return (character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z') || (character >= '0' && character <= '9');
		}
		
	}
	
}
