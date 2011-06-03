/*
 * File    : PeerInfoView.java
 * Created : Oct 2, 2005
 * By      : TuxPaper
 *
 * Copyright (C) 2005, 2006 Aelitis SAS, All rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * AELITIS, SAS au capital de 46,603.30 euros,
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 */

package org.gudy.azureus2.ui.swt.views.peer;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.disk.DiskManager;
import org.gudy.azureus2.core3.disk.DiskManagerPiece;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.logging.*;
import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPeerManager;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.pluginsimpl.local.PluginInitializer;
import org.gudy.azureus2.ui.swt.Messages;
import org.gudy.azureus2.ui.swt.Utils;
import org.gudy.azureus2.ui.swt.components.Legend;
import org.gudy.azureus2.ui.swt.debug.ObfusticateImage;
import org.gudy.azureus2.ui.swt.debug.UIDebugGenerator;
import org.gudy.azureus2.ui.swt.mainwindow.Colors;
import org.gudy.azureus2.ui.swt.plugins.UISWTView;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEvent;
import org.gudy.azureus2.ui.swt.pluginsimpl.UISWTViewCoreEventListener;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreRunningListener;
import com.aelitis.azureus.core.peermanager.piecepicker.util.BitFlags;

/**
 * Piece Map subview for Peers View.
 * Testing bed for SubView stuff.
 * 
 * @author TuxPaper
 * @created 2005/10/02
 * 
 * @todo on paint, paint cached image instead of recalc
 */
public class PeerInfoView
	implements ObfusticateImage, UISWTViewCoreEventListener
{
	private final static int BLOCK_FILLSIZE = 14;

	private final static int BLOCK_SPACING = 2;

	private final static int BLOCK_SIZE = BLOCK_FILLSIZE + BLOCK_SPACING;

	private final static int BLOCKCOLOR_AVAIL_HAVE = 0;

	private final static int BLOCKCOLOR_AVAIL_NOHAVE = 1;

	private final static int BLOCKCOLOR_NOAVAIL_HAVE = 2;

	private final static int BLOCKCOLOR_NOAVAIL_NOHAVE = 3;

	private final static int BLOCKCOLOR_TRANSFER = 4;

	private final static int BLOCKCOLOR_NEXT = 5;

	private final static int BLOCKCOLOR_AVAILCOUNT = 6;

	private Composite peerInfoComposite;

	private ScrolledComposite sc;

	protected Canvas peerInfoCanvas;

	private Color[] blockColors;

	private Label topLabel;

	private Label imageLabel;

	// More delay for this view because of high workload
	private int graphicsUpdate = COConfigurationManager
			.getIntParameter("Graphics Update") * 2;

	private int loopFactor = 0;

	private PEPeer peer;

	private Plugin countryLocator = null;

	private String sCountryImagesDir;

	private Font font = null;

	Image img = null;

	protected boolean refreshInfoCanvasQueued;

	private UISWTView swtView;

	/**
	 * Initialize
	 *
	 */
	public PeerInfoView() {
		blockColors = new Color[] { Colors.blues[Colors.BLUES_DARKEST],
				Colors.blues[Colors.BLUES_MIDLIGHT], Colors.fadedGreen, Colors.white,
				Colors.red, Colors.fadedRed, Colors.black };

		AzureusCoreFactory.addCoreRunningListener(new AzureusCoreRunningListener() {
			public void azureusCoreRunning(AzureusCore core) {
				initCountryPlugin();
			}
		});
	}
	
	private void initCountryPlugin() {
		// Pull in Country Information if the plugin exists
		/**
		 * If this view was a real plugin view, we could attach the CountryLocator.jar
		 * to our project, cast countryLocator as CountryLocator (instead of Plugin),
		 * and then directly call the functions.
		 * 
		 * Since we are in core, and we don't want to add a dependency on the
		 * CountryLocator.jar, we invoke the methods via the Class object.
		 */
		try {
			PluginInterface pi = PluginInitializer.getDefaultInterface()
					.getPluginManager().getPluginInterfaceByID("CountryLocator");
			if (pi != null) {
				countryLocator = pi.getPlugin();
				if (!pi.getPluginState().isOperational()
						|| pi.getUtilities().compareVersions(pi.getPluginVersion(), "1.6") < 0)
					countryLocator = null;

				if (countryLocator != null) {
					sCountryImagesDir = (String) countryLocator.getClass().getMethod(
							"getImageLocation", new Class[] { Integer.TYPE }).invoke(
							countryLocator, new Object[] { new Integer(0) });
				}
			}
		} catch (Throwable t) {

		}
	}

	private void dataSourceChanged(Object newDataSource) {
		if (newDataSource instanceof Object[]) {
			peer = (PEPeer) ((Object[]) newDataSource)[0];
		} else if (newDataSource instanceof PEPeer) {
			peer = (PEPeer) newDataSource;
		} else {
			peer = null;
		}

		Utils.execSWTThreadLater(0, new AERunnable() {
			public void runSupport() {
				swt_fillPeerInfoSection();
			}
		});
	}

	private String getFullTitle() {
		return MessageText.getString("PeersView.BlockView.title");
	}

	private void initialize(Composite composite) {
		if (peerInfoComposite != null && !peerInfoComposite.isDisposed()) {
			Logger.log(new LogEvent(LogIDs.GUI, LogEvent.LT_ERROR,
					"PeerInfoView already initialized! Stack: "
							+ Debug.getStackTrace(true, false)));
			delete();
		}
		createPeerInfoPanel(composite);
	}

	private Composite createPeerInfoPanel(Composite parent) {
		GridLayout layout;
		GridData gridData;

		// Peer Info section contains
		// - Peer's Block display
		// - Peer's Datarate
		peerInfoComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		peerInfoComposite.setLayout(layout);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		peerInfoComposite.setLayoutData(gridData);

		imageLabel = new Label(peerInfoComposite, SWT.NULL);
		gridData = new GridData();
		if (countryLocator != null)
			gridData.widthHint = 28;
		imageLabel.setLayoutData(gridData);

		topLabel = new Label(peerInfoComposite, SWT.NULL);
		gridData = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
		topLabel.setLayoutData(gridData);

		sc = new ScrolledComposite(peerInfoComposite, SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		sc.setLayout(layout);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
		sc.setLayoutData(gridData);
		sc.getVerticalBar().setIncrement(BLOCK_SIZE);

		peerInfoCanvas = new Canvas(sc, SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);
		gridData = new GridData(GridData.FILL, SWT.DEFAULT, true, false);
		peerInfoCanvas.setLayoutData(gridData);
		peerInfoCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (e.width <= 0 || e.height <= 0)
					return;
				try {
					Rectangle bounds = (img == null) ? null : img.getBounds();
					if (bounds == null) {
						e.gc.fillRectangle(e.x, e.y, e.width, e.height);
					} else {
						if (e.x + e.width > bounds.width)
							e.gc.fillRectangle(bounds.width, e.y, e.x + e.width
									- bounds.width + 1, e.height);
						if (e.y + e.height > bounds.height)
							e.gc.fillRectangle(e.x, bounds.height, e.width, e.y + e.height
									- bounds.height + 1);

						int width = Math.min(e.width, bounds.width - e.x);
						int height = Math.min(e.height, bounds.height - e.y);
						e.gc.drawImage(img, e.x, e.y, width, height, e.x, e.y, width,
								height);
					}
				} catch (Exception ex) {
				}
			}
		});
		Listener doNothingListener = new Listener() {
			public void handleEvent(Event event) {
			}
		};
		peerInfoCanvas.addListener(SWT.KeyDown, doNothingListener);

		peerInfoCanvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				if (refreshInfoCanvasQueued || !peerInfoCanvas.isVisible()) {
					return;
				}
				
				// wrap in asyncexec because sc.setMinWidth (called later) doesn't work
				// too well inside a resize (the canvas won't size isn't always updated)
				Utils.execSWTThreadLater(100, new AERunnable() {
					public void runSupport() {
						if (refreshInfoCanvasQueued) {
							return;
						}
						refreshInfoCanvasQueued = true;

						if (img != null) {
							int iOldColCount = img.getBounds().width / BLOCK_SIZE;
							int iNewColCount = peerInfoCanvas.getClientArea().width / BLOCK_SIZE;
							if (iOldColCount != iNewColCount)
								refreshInfoCanvas();
						}
					}
				});
			}
		});

		sc.setContent(peerInfoCanvas);

		Legend.createLegendComposite(peerInfoComposite,
				blockColors, new String[] { "PeersView.BlockView.Avail.Have",
						"PeersView.BlockView.Avail.NoHave",
						"PeersView.BlockView.NoAvail.Have",
						"PeersView.BlockView.NoAvail.NoHave",
						"PeersView.BlockView.Transfer", "PeersView.BlockView.NextRequest",
						"PeersView.BlockView.AvailCount" }, new GridData(SWT.FILL,
						SWT.DEFAULT, true, false, 2, 1));

		int iFontPixelsHeight = 10;
		int iFontPointHeight = (iFontPixelsHeight * 72)
				/ peerInfoCanvas.getDisplay().getDPI().y;
		Font f = peerInfoCanvas.getFont();
		FontData[] fontData = f.getFontData();
		fontData[0].setHeight(iFontPointHeight);
		font = new Font(peerInfoCanvas.getDisplay(), fontData);

		return peerInfoComposite;
	}

	private void swt_fillPeerInfoSection() {
		if (imageLabel.getImage() != null) {
			Image image = imageLabel.getImage();
			imageLabel.setImage(null);
			image.dispose();
		}

		if (peer == null) {
			topLabel.setText("");
		} else {
			String s = peer.getClient();
			if (s == null)
				s = "";
			if (s != "")
				s += "; ";

			s += peer.getIp()
					+ "; "
					+ DisplayFormatters.formatPercentFromThousands(peer
							.getPercentDoneInThousandNotation());
			topLabel.setText(s);

			if (countryLocator != null) {
				try {
					String sCountry = (String) countryLocator.getClass().getMethod(
							"getIPCountry", new Class[] { String.class, Locale.class })
							.invoke(countryLocator,
									new Object[] { peer.getIp(), Locale.getDefault() });

					String sCode = (String) countryLocator.getClass().getMethod(
							"getIPISO3166", new Class[] { String.class }).invoke(
							countryLocator, new Object[] { peer.getIp() });

					imageLabel.setToolTipText(sCode + "- " + sCountry);

					InputStream is = countryLocator.getClass().getClassLoader()
							.getResourceAsStream(
									sCountryImagesDir + "/" + sCode.toLowerCase() + ".png");
					if (is != null) {
						Image img = new Image(imageLabel.getDisplay(), is);
						img.setBackground(imageLabel.getBackground());
						imageLabel.setImage(img);
					}

				} catch (Exception e) {
					// ignore
				}
			}
		}
		refreshInfoCanvas();
	}

	private void refresh() {
		if (loopFactor++ % graphicsUpdate == 0) {
			refreshInfoCanvas();
		}
	}

	/**
	 * Constructs and image representing the download state of _all_
	 * the pieces in the torrent.  Particularily slow when there's lots of pieces,
	 * and also wasteful since only a fraction of them ever get painted at
	 * any given time.
	 * 
	 * TODO: Construct image for visible area only or something
	 */
	private void refreshInfoCanvas() {
		refreshInfoCanvasQueued = false;

		if (peerInfoComposite == null || !peerInfoComposite.isVisible()) {
			return;
		}

		peerInfoCanvas.layout(true);
		Rectangle bounds = peerInfoCanvas.getClientArea();
		if (bounds.width <= 0 || bounds.height <= 0)
			return;

		if (img != null && !img.isDisposed()) {
			img.dispose();
			img = null;
		}

		if (peer == null || peer.getPeerState() != PEPeer.TRANSFERING) {
			GC gc = new GC(peerInfoCanvas);
			gc.fillRectangle(bounds);
			gc.dispose();

			return;
		}

		BitFlags peerHavePieces = peer.getAvailable();
		if (peerHavePieces == null) {
			GC gc = new GC(peerInfoCanvas);
			gc.fillRectangle(bounds);
			gc.dispose();

			return;
		}

		DiskManagerPiece[] dm_pieces = null;

		PEPeerManager pm = peer.getManager();

		DiskManager dm = pm.getDiskManager();

		dm_pieces = dm.getPieces();

		int iNumCols = bounds.width / BLOCK_SIZE;
		int iNeededHeight = (((dm.getNbPieces() - 1) / iNumCols) + 1)
				* BLOCK_SIZE;
		if (sc.getMinHeight() != iNeededHeight) {
			sc.setMinHeight(iNeededHeight);
			sc.layout(true, true);
			bounds = peerInfoCanvas.getClientArea();
		}
		
		img = new Image(peerInfoCanvas.getDisplay(), bounds.width, iNeededHeight);
		GC gcImg = new GC(img);
		
		try {
			// use advanced capabilities for faster drawText
			gcImg.setAdvanced(true);
			
			gcImg.setBackground(peerInfoCanvas.getBackground());
			gcImg.fillRectangle(0, 0, bounds.width, iNeededHeight);

			int[] availability = pm.getAvailability();

			int iNextDLPieceID = -1;
			int iDLPieceID = -1;
			int[] ourRequestedPieces = peer.getOutgoingRequestedPieceNumbers();
			if (ourRequestedPieces != null) {
				if (!peer.isChokingMe()) {
					// !choking == downloading

					if (ourRequestedPieces.length > 0) {
						iDLPieceID = ourRequestedPieces[0];
						if (ourRequestedPieces.length > 1)
							iNextDLPieceID = ourRequestedPieces[1];
					}
				} else {
					if (ourRequestedPieces.length > 0)
						iNextDLPieceID = ourRequestedPieces[0];
				}

				//			if (iNextDLPieceID == -1) {
				//				iNextDLPieceID = peer.getNextPieceNumberGuess();
				//			}
			}

			int[] peerRequestedPieces = peer.getIncomingRequestedPieceNumbers();
			if (peerRequestedPieces == null)
				peerRequestedPieces = new int[0];

			int peerNextRequestedPiece = -1;
			if (peerRequestedPieces.length > 0)
				peerNextRequestedPiece = peerRequestedPieces[0];
			Arrays.sort(peerRequestedPieces);

			int iRow = 0;
			int iCol = 0;
			for (int i = 0; i < peerHavePieces.flags.length; i++) {
				int colorIndex;
				boolean done = (dm_pieces == null) ? false : dm_pieces[i].isDone();
				int iXPos = iCol * BLOCK_SIZE;
				int iYPos = iRow * BLOCK_SIZE;

				if (done) {
					if (peerHavePieces.flags[i])
						colorIndex = BLOCKCOLOR_AVAIL_HAVE;
					else
						colorIndex = BLOCKCOLOR_NOAVAIL_HAVE;

					gcImg.setBackground(blockColors[colorIndex]);
					gcImg.fillRectangle(iXPos, iYPos, BLOCK_FILLSIZE, BLOCK_FILLSIZE);
				} else {
					// !done
					boolean partiallyDone = (dm_pieces == null) ? false : dm_pieces[i]
							.getNbWritten() > 0;

					int x = iXPos;
					int width = BLOCK_FILLSIZE;
					if (partiallyDone) {
						if (peerHavePieces.flags[i])
							colorIndex = BLOCKCOLOR_AVAIL_HAVE;
						else
							colorIndex = BLOCKCOLOR_NOAVAIL_HAVE;

						gcImg.setBackground(blockColors[colorIndex]);

						@SuppressWarnings("null") // partiallyDone false when dm_pieces null
						int iNewWidth = (int) (((float) dm_pieces[i].getNbWritten() / dm_pieces[i]
								.getNbBlocks()) * width);
						if (iNewWidth >= width)
							iNewWidth = width - 1;
						else if (iNewWidth <= 0)
							iNewWidth = 1;

						gcImg.fillRectangle(x, iYPos, iNewWidth, BLOCK_FILLSIZE);
						width -= iNewWidth;
						x += iNewWidth;
					}

					if (peerHavePieces.flags[i])
						colorIndex = BLOCKCOLOR_AVAIL_NOHAVE;
					else
						colorIndex = BLOCKCOLOR_NOAVAIL_NOHAVE;

					gcImg.setBackground(blockColors[colorIndex]);
					gcImg.fillRectangle(x, iYPos, width, BLOCK_FILLSIZE);
				}

				// Down Arrow inside box for "dowloading" piece
				if (i == iDLPieceID) {
					gcImg.setBackground(blockColors[BLOCKCOLOR_TRANSFER]);
					gcImg.fillPolygon(new int[] { iXPos, iYPos, iXPos + BLOCK_FILLSIZE,
							iYPos, iXPos + (BLOCK_FILLSIZE / 2), iYPos + BLOCK_FILLSIZE });
				}

				// Small Down Arrow inside box for next download piece
				if (i == iNextDLPieceID) {
					gcImg.setBackground(blockColors[BLOCKCOLOR_NEXT]);
					gcImg.fillPolygon(new int[] { iXPos + 2, iYPos + 2,
							iXPos + BLOCK_FILLSIZE - 1, iYPos + 2,
							iXPos + (BLOCK_FILLSIZE / 2), iYPos + BLOCK_FILLSIZE - 1 });
				}

				// Up Arrow in uploading piece 
				if (i == peerNextRequestedPiece) {
					gcImg.setBackground(blockColors[BLOCKCOLOR_TRANSFER]);
					gcImg.fillPolygon(new int[] { iXPos, iYPos + BLOCK_FILLSIZE,
							iXPos + BLOCK_FILLSIZE, iYPos + BLOCK_FILLSIZE,
							iXPos + (BLOCK_FILLSIZE / 2), iYPos });
				} else if (Arrays.binarySearch(peerRequestedPieces, i) >= 0) {
					// Small Up Arrow each upload request
					gcImg.setBackground(blockColors[BLOCKCOLOR_NEXT]);
					gcImg.fillPolygon(new int[] { iXPos + 1, iYPos + BLOCK_FILLSIZE - 2,
							iXPos + BLOCK_FILLSIZE - 2, iYPos + BLOCK_FILLSIZE - 2,
							iXPos + (BLOCK_FILLSIZE / 2), iYPos + 2 });
				}

				if (availability != null && availability[i] < 10) {
					gcImg.setFont(font);
					String sNumber = String.valueOf(availability[i]);
					Point size = gcImg.stringExtent(sNumber);

					int x = iXPos + (BLOCK_FILLSIZE / 2) - (size.x / 2);
					int y = iYPos + (BLOCK_FILLSIZE / 2) - (size.y / 2);
					gcImg.setForeground(blockColors[BLOCKCOLOR_AVAILCOUNT]);
					gcImg.drawText(sNumber, x, y, true);
				}

				iCol++;
				if (iCol >= iNumCols) {
					iCol = 0;
					iRow++;
				}
			}
		} catch (Exception e) {
			Logger.log(new LogEvent(LogIDs.GUI, "drawing piece map", e));
		} finally {
			gcImg.dispose();
		}
		
		peerInfoCanvas.redraw();
	}

	private Composite getComposite() {
		return peerInfoComposite;
	}

	private void delete() {
		if (!imageLabel.isDisposed() && imageLabel.getImage() != null) {
			Image image = imageLabel.getImage();
			imageLabel.setImage(null);
			image.dispose();
		}

		if (img != null && !img.isDisposed()) {
			img.dispose();
			img = null;
		}

		if (font != null && !font.isDisposed()) {
			font.dispose();
			font = null;
		}
	}

	public Image obfusticatedImage(Image image) {
		UIDebugGenerator.obfusticateArea(image, topLabel, "");
		return image;
	}

	public boolean eventOccurred(UISWTViewEvent event) {
    switch (event.getType()) {
      case UISWTViewEvent.TYPE_CREATE:
      	swtView = (UISWTView)event.getData();
      	swtView.setTitle(getFullTitle());
        break;

      case UISWTViewEvent.TYPE_DESTROY:
        delete();
        break;

      case UISWTViewEvent.TYPE_INITIALIZE:
        initialize((Composite)event.getData());
        break;

      case UISWTViewEvent.TYPE_LANGUAGEUPDATE:
      	Messages.updateLanguageForControl(getComposite());
      	swtView.setTitle(getFullTitle());
        break;

      case UISWTViewEvent.TYPE_DATASOURCE_CHANGED:
      	dataSourceChanged(event.getData());
        break;
        
      case UISWTViewEvent.TYPE_FOCUSGAINED:
    		refreshInfoCanvas();
      	break;
        
      case UISWTViewEvent.TYPE_REFRESH:
        refresh();
        break;
    }

    return true;
  }
}
