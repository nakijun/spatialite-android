package com.spatialite.test;

import jsqlite.Stmt;
import android.os.Environment;
import android.test.AndroidTestCase;
import android.util.Log;

public class SpatialiteGeoFunctionTest extends AndroidTestCase {
	private static final String TAG = "SpatialiteGeoFunctionTest";
	jsqlite.Database db = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dbSetUp();
	}

	@Override
	protected void tearDown() throws Exception {
		dbTeardown();
		super.tearDown();
	}

	private void dbTeardown() throws Exception {
		db.close();
	}

	private void dbSetUp() throws Exception {
		db = new jsqlite.Database();
		db.open(Environment.getExternalStorageDirectory()
				+ "/Download/test-2.3.sqlite",
				jsqlite.Constants.SQLITE_OPEN_READONLY);
	}
	
	public void testVersions() throws Exception {
		Stmt stmt01 = db.prepare("SELECT spatialite_version();");
		if (stmt01.step()) {
			assertEquals("3.0.1",stmt01.column_string(0));
		}
		
		stmt01 = db.prepare("SELECT proj4_version();");
		if (stmt01.step()) {
			assertEquals("Rel. 4.7.1, 23 September 2009",stmt01.column_string(0));
		}
		
		stmt01 = db.prepare("SELECT geos_version();");
		if (stmt01.step()) {
			assertEquals("3.2.2-CAPI-1.6.2",stmt01.column_string(0));
		}
		stmt01.close();
	}
	
	public void testGeometryRepresentation01() throws Exception {
		Stmt stmt01 = db.prepare("SELECT name, peoples, HEX(Geometry) from Towns where peoples > 350000 order by peoples DESC;");
		if (stmt01.step()) {
			assertEquals("Roma",stmt01.column_string(0));
			assertEquals("2546804",stmt01.column_string(1));
			assertEquals("0001787F00003D0AD723BF11284133333313C1B851413D0AD723BF11284133333313C1B851417C010000003D0AD723BF11284133333313C1B85141FE",stmt01.column_string(2));
		} else {
			fail("Query failed");
		}
		stmt01.close();
		
		Stmt stmt02 = db.prepare("SELECT name, peoples, AsText(Geometry) from Towns where peoples > 350000 order by peoples DESC;");
		if (stmt02.step()) {
			assertEquals("Roma",stmt02.column_string(0));
			assertEquals("2546804",stmt02.column_string(1));
			assertEquals("POINT(788703.57 4645636.3)",stmt02.column_string(2));
		} else {
			fail("Query failed");
		}
		stmt02.close();
		
		Stmt stmt03 = db.prepare("SELECT name, X(Geometry), Y(Geometry) from Towns where peoples > 350000 order by peoples DESC;");
		if (stmt03.step()) {
			assertEquals("Roma",stmt03.column_string(0));
			assertEquals("788703.57",stmt03.column_string(1));
			assertEquals("4645636.3",stmt03.column_string(2));
		} else {
			fail("Query failed");
		}
		stmt03.close();
	}
	
	public void testGeometryRepresentation02() throws Exception {
		Stmt stmt01 = db.prepare("SELECT HEX(GeomFromText('POINT(10 20)'));");
		if(stmt01.step()) {
			assertEquals("0001FFFFFFFF00000000000024400000000000003440000000000000244000000000000034407C0100000000000000000024400000000000003440FE", stmt01.column_string(0));
		} else {
			fail("Query failed");
		}
		stmt01.close();
		
		Stmt stmt02 = db.prepare("SELECT HEX(AsBinary(GeomFromText('POINT(10 20)')));");
		if(stmt02.step()) {
			assertEquals("010100000000000000000024400000000000003440", stmt02.column_string(0));
		} else {
			fail("Query failed");
		}
		stmt02.close();
		
		Stmt stmt03 = db.prepare("SELECT AsText(GeomFromWKB(X'010100000000000000000024400000000000003440'));");
		if(stmt03.step()) {
			assertEquals("POINT(10 20)", stmt03.column_string(0));
		} else {
			fail("Query failed");
		}
		stmt03.close();
	}
	
	public void testGeometryRepresentation03() throws Exception {
		Stmt stmt01 = db.prepare("SELECT AsKml(Geometry) FROM Highways WHERE PK_UID = 2;");
		if(stmt01.step()) {
			assertEquals("<LineString><coordinates>11.13099600000592,43.820771999992324 11.131468000005926,43.820664999992324</coordinates></LineString>", stmt01.column_string(0));
		} else {
			fail("Query failed");
		}
		stmt01.close();
		
		Stmt stmt02 = db.prepare("SELECT AsGeoJSON(Geometry, 2) FROM Highways WHERE PK_UID = 2;");
		if(stmt02.step()) {
			assertEquals("{\"type\":\"LineString\",\"coordinates\":[[671365.87,4854173.77],[671404.13,4854162.86]]}", stmt02.column_string(0));
		} else {
			fail("Query failed");
		}
		stmt02.close();
	}
	
	public void testGeometryClass() throws Exception {
		Stmt stmt01 = db.prepare("SELECT PK_UID, AsText(Geometry) FROM HighWays WHERE PK_UID = 2;");
		if(stmt01.step()) {
			assertEquals("2",stmt01.column_string(0));
			assertEquals("LINESTRING(671365.867442 4854173.770802, 671404.13073 4854162.864623)", stmt01.column_string(1));
		} else {
			fail("Query failed");
		}
		stmt01.close();
		
		Stmt stmt02 = db.prepare("SELECT PK_UID, NumPoints(Geometry), GLength(Geometry), Dimension(Geometry), GeometryType(Geometry) FROM HighWays ORDER BY NumPoints(Geometry) DESC LIMIT 1;");
		if(stmt02.step()) {
			assertEquals("774",stmt02.column_string(0));
			assertEquals("6758",stmt02.column_string(1));
			assertEquals("94997.8721344157",stmt02.column_string(2));
			assertEquals("1",stmt02.column_string(3));
			assertEquals("LINESTRING",stmt02.column_string(4));
		} else {
			fail("Query failed");
		}
		stmt02.close();
		
		Stmt stmt03 = db.prepare("SELECT name, AsText(Geometry) FROM Regions WHERE PK_UID = 52;");
		if(stmt03.step()) {
			assertEquals("EMILIA-ROMAGNA",stmt03.column_string(0));
		} else {
			fail("Query failed");
		}
		stmt03.close();
		
		Stmt stmt04 = db.prepare("SELECT PK_UID, NumInteriorRings(Geometry), NumPoints(ExteriorRing(Geometry)), NumPoints(InteriorRingN(Geometry, 1)) FROM regions ORDER BY NumInteriorRings(Geometry) DESC LIMIT 5;");
		if(stmt04.step()) {
			assertEquals("55",stmt04.column_string(0));
			assertEquals("1",stmt04.column_string(1));
			assertEquals("602",stmt04.column_string(2));
			assertEquals("9",stmt04.column_string(3));
		} else {
			fail("Query failed");
		}
		stmt04.close();
		
		Stmt stmt05 = db.prepare("SELECT Round(GLength(Geometry)), IsClosed(Geometry), NumPoints(Geometry) FROM Highways WHERE PK_UID = 2;");
		if(stmt05.step()) {
			assertEquals("40.0", stmt05.column_string(0));
			assertEquals("0", stmt05.column_string(1));
			assertEquals("2", stmt05.column_string(2));
		} else {
			fail("Query failed");
		}
		stmt05.close();
	}
	
	public void testGeometryEnvelope() throws Exception {
		Stmt stmt01 = db.prepare("SELECT Name, AsText(Envelope(Geometry)) FROM Regions LIMIT 5;");
		if(stmt01.step()) {
			assertEquals("VENETO",stmt01.column_string(0));
			assertEquals("POLYGON((752912.250297 5027429.54477, 753828.826422 5027429.54477, 753828.826422 5028928.677375, 752912.250297 5028928.677375, 752912.250297 5027429.54477))",stmt01.column_string(1));
		} else {
			fail("Query failed");
		}
		stmt01.close();
	}
}