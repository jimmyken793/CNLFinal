package idv.jomican.streaming;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.util.SystemTime;

public class StreamingUtils {
	public static final int BUFFER_TIME = 360;

	public static boolean isPieceAvalable(long elapsed_time, long total_time, int piece, int total_piece) {
		if (total_piece * BUFFER_TIME > piece * total_time) {
			return true;
		}
		if (total_piece * (total_time - BUFFER_TIME) < piece * total_time) {
			return true;
		}
		long headpiece = (total_piece * BUFFER_TIME) / total_time;
		return (total_piece - headpiece) * (elapsed_time) < (piece - headpiece) * total_time && (total_piece - headpiece) * (elapsed_time + BUFFER_TIME) > (piece - headpiece) * total_time;
	}

	public static long getPieceIntervalStart(long elapsed_time, long total_time, int total_piece) {
		return total_piece * (elapsed_time) / total_time;
	}

	public static long getPieceIntervalEnd(long elapsed_time, long total_time, int total_piece) {
		return total_piece * (elapsed_time + BUFFER_TIME + BUFFER_TIME) / total_time;
	}

	public static boolean isPieceAvalable(TOTorrent torrent, int piece) {
		Long flag = torrent.getAdditionalLongProperty("jomican streaming");
		if (flag != null && flag == 1) {
			Long start_time = torrent.getAdditionalLongProperty("start time");
			Long video_length = torrent.getAdditionalLongProperty("video length");
			if (start_time != null && video_length != null) {
				long curr_time = SystemTime.getCurrentTime() / 1000;
				int total_piece = torrent.getNumberOfPieces();
				if (curr_time > start_time + video_length) {
					return true;
				}
				// Debug.out("start_time - curr_time / video_length = " +
				// (start_time - curr_time) + " / " + video_length);
				// Debug.out("number / total_piece = " + piece + " / " +
				// total_piece);
				if (StreamingUtils.isPieceAvalable(curr_time - start_time, video_length, piece, total_piece)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return true;

	}

	public static long getPieceIntervalStart(TOTorrent torrent) {
		Long flag = torrent.getAdditionalLongProperty("jomican streaming");
		if (flag != null && flag == 1) {
			Long start_time = torrent.getAdditionalLongProperty("start time");
			Long video_length = torrent.getAdditionalLongProperty("video length");
			if (start_time != null && video_length != null) {
				long curr_time = SystemTime.getCurrentTime() / 1000;
				int total_piece = torrent.getNumberOfPieces();
				if (curr_time > start_time + video_length) {
					return 0;
				}
				return getPieceIntervalStart(curr_time - start_time, video_length, total_piece);
			}
		}
		return 0;
	}

	public static long getPieceIntervalEnd(TOTorrent torrent) {
		Long flag = torrent.getAdditionalLongProperty("jomican streaming");
		if (flag != null && flag == 1) {
			Long start_time = torrent.getAdditionalLongProperty("start time");
			Long video_length = torrent.getAdditionalLongProperty("video length");
			if (start_time != null && video_length != null) {
				long curr_time = SystemTime.getCurrentTime() / 1000;
				int total_piece = torrent.getNumberOfPieces();
				if (curr_time > start_time + video_length) {
					return total_piece;
				}
				return getPieceIntervalEnd(curr_time - start_time, video_length, total_piece);
			}
		}
		return torrent.getNumberOfPieces();
	}
}
