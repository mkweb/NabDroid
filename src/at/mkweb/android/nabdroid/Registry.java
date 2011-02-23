/**
 * at.mkweb.android.nabdroid.Registry
 * 
 * LICENSE:
 *
 * This file is part of NabDroi, an Android app for communicating with your Nabaztag (http://android.mk-web.at/app/nabdroid.html).
 *
 * NabDroid is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * NabDroi is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with software.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Mario Klug <mario.klug@mk-web.at>
 * @package at.mkweb.android.nabdroid
 * 
 * @license http://www.gnu.org/licenses/gpl.html
 */

package at.mkweb.android.nabdroid;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Registry {

	private static SQLiteDatabase db = null;
	private static Nabaztag nab = null;
	
	public static void setDb(SQLiteDatabase d) {
		
		db = d;
	}
	
	public static void setNabaztag(Nabaztag n) {
		
		nab = n;
	}
	
	public static Nabaztag getNabaztag() {
		
		if(nab == null) {
			
			nab = new Nabaztag(db);
		}
		
		return nab;
	}
	
	public static SQLiteDatabase getDb() {
		
		if(db == null) {
			
			Log.e("db", "DB not initialized!");
		}
		
		return db;
	}
}
