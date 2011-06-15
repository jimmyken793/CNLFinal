package idv.jomican.streaming;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.SystemTime;

public class StreamingUtils {
	public static final int BUFFER_TIME = 360;

	public static boolean isPieceAvalable(long elapsed_time, long total_time, long piece, long total_piece) {
		return total_piece * (elapsed_time) < piece * total_time && total_piece * (elapsed_time + BUFFER_TIME) > piece * total_time;
	}

	public static long getPieceIntervalStart(long elapsed_time, long total_time, long total_piece) {
		return total_piece * (elapsed_time) / total_time;
	}

	public static long getPieceIntervalEnd(long elapsed_time, long total_time, long total_piece) {
		return total_piece * (elapsed_time + BUFFER_TIME) / total_time;
	}

	public static boolean isPieceAvalable(TOTorrent torrent, long piece) {
		Long flag = torrent.getAdditionalLongProperty("jomican streaming");
		if (flag != null && flag == 1) {
			Long start_time = torrent.getAdditionalLongProperty("start time");
			Long video_length = torrent.getAdditionalLongProperty("video length");
			if (start_time != null && video_length != null) {
				long curr_time = SystemTime.getCurrentTime() / 1000;
				long total_piece = torrent.getNumberOfPieces();
				Debug.out("start_time - curr_time / video_length = " + (start_time - curr_time) + " / " + video_length);
				Debug.out("number / total_piece = " + piece + " / " + total_piece);
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
				long total_piece = torrent.getNumberOfPieces();
				return getPieceIntervalStart(curr_time - start_time, video_length, total_piece);
			}
		}
		return -1;
	}

	public static long getPieceIntervalEnd(TOTorrent torrent) {
		Long flag = torrent.getAdditionalLongProperty("jomican streaming");
		if (flag != null && flag == 1) {
			Long start_time = torrent.getAdditionalLongProperty("start time");
			Long video_length = torrent.getAdditionalLongProperty("video length");
			if (start_time != null && video_length != null) {
				long curr_time = SystemTime.getCurrentTime() / 1000;
				long total_piece = torrent.getNumberOfPieces();
				return getPieceIntervalEnd(curr_time - start_time, video_length, total_piece);
			}
		}
		return -1;
	}
}
