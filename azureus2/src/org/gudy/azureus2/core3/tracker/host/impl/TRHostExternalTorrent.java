/*
 * File    : TRHostExternalTorrent.java
 * Created : 19-Nov-2003
 * By      : parg
 * 
 * Azureus - a Java Bittorrent client
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
 */

package org.gudy.azureus2.core3.tracker.host.impl;

/**
 * @author parg
 *
 */

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gudy.azureus2.core3.internat.LocaleTorrentUtil;
import org.gudy.azureus2.core3.internat.LocaleUtilEncodingException;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLGroup;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLSet;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.torrent.TOTorrentListener;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.ByteFormatter;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.HashWrapper;

public class 
TRHostExternalTorrent 
	implements TOTorrent
{
	private byte[]		name;
	private byte[]		hash;
	private HashWrapper	hash_wrapper;
	private URL			announce_url;
	
	protected Map		additional_properties = new HashMap();
	
	protected AEMonitor this_mon 	= new AEMonitor( "TRHostExternalTorrent" );

	protected
	TRHostExternalTorrent(
		byte[]	_hash,
		URL		_announce_url  )
	{
		hash			= _hash;
		hash_wrapper	= new HashWrapper( hash );
		announce_url	= _announce_url;
		
		name = ByteFormatter.nicePrint( hash, true ).getBytes();
		
		try{
			LocaleTorrentUtil.setDefaultTorrentEncoding( this );
			
		}catch( LocaleUtilEncodingException e ){
			
			Debug.printStackTrace( e );
		}
	}
	
	@Override
	public byte[]
	getName()
	{
		return( name );
	}
	
	@Override
	public String getUTF8Name() {
		return null;
	}

	
	@Override
	public boolean
	isSimpleTorrent()
	{
		return( true );
	}
	

	@Override
	public byte[]
	getComment()
	{
		return( null );
	}

	@Override
	public void
	setComment(
		String		comment )
	{		
	}
	
	
	@Override
	public long
	getCreationDate()
	{
		return(0);
	}
	
	@Override
	public void
	setCreationDate(
		long		date )
	{
	}
	
	@Override
	public byte[]
	getCreatedBy()
	{
		return( null );
	}
	
 	@Override
	public void
	setCreatedBy(
		byte[]		cb )
   	{
   	}
 	
	@Override
	public boolean
	isCreated()
	{
		return( false );
	}
	
	@Override
	public URL
	getAnnounceURL()
	{
		return( announce_url );
	}

	@Override
	public boolean
	setAnnounceURL(
		URL		url )
	{
		return false;
	}
		
	@Override
	public TOTorrentAnnounceURLGroup
	getAnnounceURLGroup()
	{
		return( 
			new TOTorrentAnnounceURLGroup()
			{
				@Override
				public TOTorrentAnnounceURLSet[]
               	getAnnounceURLSets()
				{
					return( new TOTorrentAnnounceURLSet[0] );
				}
 
               	@Override
				public void
               	setAnnounceURLSets(
               		TOTorrentAnnounceURLSet[]	sets )
				{
				}
               		
               	@Override
				public TOTorrentAnnounceURLSet
               	createAnnounceURLSet(
               		URL[]	urls )
				{
					return( new TOTorrentAnnounceURLSet()
							{
								@Override
								public URL[]
						       	getAnnounceURLs()
								{
									return( new URL[0]);
								}
						       	
						       	@Override
								public void
						       	setAnnounceURLs(
						       		URL[]		urls )
								{
								}
							});
				}
			});
	}
  
	public void
	addTorrentAnnounceURLSet( 
		URL[] urls )
	{
	}
  
	
	@Override
	public byte[][]
	getPieces()
	{
		return( new byte[0][] );
	}

	@Override
	public void
	setPieces(
		byte[][]	b )
	{
	}
	
	@Override
	public int
	getNumberOfPieces()
	{
		return( 0);
	}

	@Override
	public long
	getPieceLength()
	{
		return( -1 );
	}

	@Override
	public long
	getSize()
	{
		return( -1 );
	}
	

	@Override
	public TOTorrentFile[]
	getFiles()
	{
		return( new TOTorrentFile[0]);
	}
	 
	@Override
	public byte[]
	getHash()
	
		throws TOTorrentException
	{
		return( hash );
	}
	
	@Override
	public HashWrapper
	getHashWrapper()

		throws TOTorrentException
	{
		return( hash_wrapper );
	}
	
   	@Override
	public void 
	setHashOverride(
		byte[] hash ) 
	
		throws TOTorrentException 
	{
		throw( new TOTorrentException( "Not supported", TOTorrentException.RT_HASH_FAILS ));
	}
   	
	@Override
	public boolean
	getPrivate()
	{
		return( false );
	}
	
	@Override
	public void
	setPrivate(
		boolean	_private )
	
		throws TOTorrentException
	{
	}
	
	@Override
	public boolean
	hasSameHashAs(
		TOTorrent		other )
	{
		try{
			byte[]	other_hash = other.getHash();
			
			return( Arrays.equals( hash, other_hash ));
			
		}catch( TOTorrentException e ){
			
			Debug.printStackTrace( e );
			
			return( false );
		}
	}
	
	@Override
	public void
	setAdditionalStringProperty(
		String		name,
		String		value )
	{
		try{
		
			additional_properties.put(name,value.getBytes(Constants.DEFAULT_ENCODING));
			
		}catch( Throwable e ){
			
			Debug.printStackTrace( e );
		}
	}
		
	@Override
	public String
	getAdditionalStringProperty(
		String		name )
	{
		try{
			Object	obj = additional_properties.get(name);

			if ( obj == null ){
				
				return( null );
			}
			
			if ( !( obj instanceof byte[] )){
				
				Debug.out( "property '" + name + "' is not a byte[]: " + obj );
				
				return( null );
			}
			
			return( new String((byte[])obj,Constants.DEFAULT_ENCODING));
			
		}catch( Throwable e ){
			
			Debug.printStackTrace( e );
			
			return( null );
		}
	}
		
	@Override
	public void
	setAdditionalByteArrayProperty(
		String		name,
		byte[]		value )
	{
		additional_properties.put(name,value);
	}
		
	@Override
	public byte[]
	getAdditionalByteArrayProperty(
		String		name )
	{
		return( (byte[])additional_properties.get(name));
	}
	
	@Override
	public void
	setAdditionalLongProperty(
		String		name,
		Long		value )
	{
		additional_properties.put(name,value);
	}
		
	@Override
	public void
	setAdditionalProperty(
		String		name,
		Object		value )
	{
		if ( value instanceof String ){
			
			setAdditionalStringProperty(name,(String)value);
			
		}else{
		
			additional_properties.put( name, value );
		}
	}
	
	@Override
	public Long
	getAdditionalLongProperty(
		String		name )
	{
		return((Long)additional_properties.get(name));
	}
		
	
	@Override
	public void
	setAdditionalListProperty(
		String		name,
		List		value )
	{
		additional_properties.put(name,value);
	}
		
	@Override
	public List
	getAdditionalListProperty(
		String		name )
	{
		return((List)additional_properties.get(name));
	}
		
	@Override
	public void
	setAdditionalMapProperty(
		String	name,
		Map		value )
	{
		additional_properties.put(name,value);
	}
		
	@Override
	public Map
	getAdditionalMapProperty(
		String		name )
	{
		return( (Map)additional_properties.get(name));
	}
		
	@Override
	public Object
	getAdditionalProperty(
		String		name )
	{
		return( additional_properties.get(name));
	}
	
	@Override
	public void
	removeAdditionalProperty(
		String name )
	{
		additional_properties.remove( name );
	}

	@Override
	public void
	removeAdditionalProperties()
	{
		additional_properties.clear();
	}
	
	@Override
	public void
	serialiseToBEncodedFile(
		File		file )
		  
		throws TOTorrentException
	{
		throw( new TOTorrentException("External Torrent", TOTorrentException.RT_WRITE_FAILS ));
	}


	@Override
	public Map
	serialiseToMap()
		  
		throws TOTorrentException
	{
		throw( new TOTorrentException("External Torrent", TOTorrentException.RT_WRITE_FAILS ));
	}

   @Override
public void
   serialiseToXMLFile(
	   File		file )
		  
	   throws TOTorrentException
	{
		throw( new TOTorrentException("External Torrent", TOTorrentException.RT_WRITE_FAILS ));
	}

 	@Override
	public void
	addListener(
		TOTorrentListener		l )
	{
	}

	@Override
	public void
	removeListener(
		TOTorrentListener		l )
	{
	}
	
   	@Override
	public AEMonitor
	getMonitor()
   	{
   		return( this_mon );
   	}
   	
	@Override
	public void
	print()
	{
	}

	@Override
	public void setStartTime(long _start_date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getStartTime() {
		// TODO Auto-generated method stub
		return 0;
	}
}
