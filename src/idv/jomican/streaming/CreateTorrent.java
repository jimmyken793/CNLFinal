package idv.jomican.streaming;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.aelitis.azureus.ui.console.MakeTorrent;

public class CreateTorrent {

	protected Shell shell;
	private Text videoPath;
	private Text torrentPath;
	private Text videoLength;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CreateTorrent window = new CreateTorrent();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(538, 450);
		shell.setText("SWT Application");

		final DateTime dateTime = new DateTime(shell, SWT.BORDER | SWT.CALENDAR);
		dateTime.setBounds(10, 10, 514, 290);

		final DateTime dateTime_1 = new DateTime(shell, SWT.BORDER | SWT.TIME);
		dateTime_1.setBounds(10, 306, 135, 29);
		videoPath = new Text(shell, SWT.BORDER);
		videoPath.setBounds(151, 306, 276, 27);

		Button btnBrowseVideo = new Button(shell, SWT.NONE);
		btnBrowseVideo.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
				String fileFilterPath = "";
				fileDialog.setFilterPath(fileFilterPath);
				fileDialog.setFilterExtensions(new String[] { "*.*" });
				fileDialog.setFilterNames(new String[] { "Any" });
				String firstFile = fileDialog.open();
				if (firstFile != null) {
					videoPath.setText(firstFile);
				}
			}
		});
		btnBrowseVideo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			}
		});
		btnBrowseVideo.setBounds(433, 306, 91, 29);
		btnBrowseVideo.setText("browse");
		torrentPath = new Text(shell, SWT.BORDER);
		torrentPath.setBounds(151, 341, 276, 27);
		Button btnBrowseTorrent = new Button(shell, SWT.NONE);
		btnBrowseTorrent.setText("browse");
		btnBrowseTorrent.setBounds(433, 339, 91, 29);
		btnBrowseTorrent.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
				String fileFilterPath = "";
				fileDialog.setFilterPath(fileFilterPath);
				fileDialog.setFilterExtensions(new String[] { "*.*" });
				fileDialog.setFilterNames(new String[] { "Any" });
				String firstFile = fileDialog.open();
				if (firstFile != null) {
					torrentPath.setText(firstFile);
				}
			}
		});

		Button btnGenerateTorrent = new Button(shell, SWT.NONE);
		btnGenerateTorrent.setBounds(10, 374, 516, 29);
		btnGenerateTorrent.setText("Generate Torrent");

		videoLength = new Text(shell, SWT.BORDER);
		videoLength.setBounds(10, 341, 135, 27);
		btnGenerateTorrent.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				File video = new File(videoPath.getText());
				try {
					if (video.exists() && torrentPath.getText() != "") {
						Map parameters = new HashMap();
						parameters.put("start", dateTime.getYear() + "/" + (dateTime.getMonth()+1) + "/" + dateTime.getDay() + "-" + dateTime_1.getHours() + ":" + dateTime_1.getMinutes() + ":" + dateTime_1.getSeconds());
						parameters.put("target", torrentPath.getText());
						parameters.put("length", videoLength.getText());
						System.out.println("start generating "+parameters.get("length"));
						new MakeTorrent(videoPath.getText(), new URL("https://jomican.csie.org/~jimmy/tracker/announce.php"), parameters);
						System.out.println("end generating");
					} else {
						System.out.println("jizz");
					}
				} catch (MalformedURLException e) {
				}
			}
		});

	}
}
